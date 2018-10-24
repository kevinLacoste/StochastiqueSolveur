// --------------------------------------------------------------------------
// File: BendersATSP2.cs
// Version 12.8.0
// --------------------------------------------------------------------------
// Licensed Materials - Property of IBM
// 5725-A06 5725-A29 5724-Y48 5724-Y49 5724-Y54 5724-Y55 5655-Y21
// Copyright IBM Corporation 2000, 2017. All Rights Reserved.
//
// US Government Users Restricted Rights - Use, duplication or
// disclosure restricted by GSA ADP Schedule Contract with
// IBM Corp.
// --------------------------------------------------------------------------
//
//
// Example BendersATSP2.cs solves a flow MILP model for an
// Asymmetric Traveling Salesman Problem (ATSP) instance
// through Benders decomposition.
//
// The arc costs of an ATSP instance are read from an input file.
// The flow MILP model is decomposed into a master ILP and a worker LP.
//
// The master ILP is then solved by adding Benders' cuts via the new generic
// callback function benders_callback during the branch-and-cut process.
//
// The callback benders_callback adds to the master ILP violated Benders'
// cuts that are found by solving the worker LP.
//
// The example allows the user to decide if Benders' cuts have to be separated
// just as lazy constraints or also as user cuts. In particular:
//
// a) Only to separate integer infeasible solutions.
// In this case, benders_callback is called with
// contextid=CPX_CALLBACKCONTEXT_CANDIDATE. The current candidate integer
// solution can be queried with CPXXcallbackgetcandidatepoint, and it can be rejected
// by the user, optionally providing a list of lazy constraints, with the
// function CPXXcallbackrejectcandidate.
//
// b) Also to separate fractional infeasible solutions.
// In this case, benders_callback is called with
// contextid=CPX_CALLBACKCONTEXT_RELAXATION. The current fractional solution
// can be queried with CPXXcallbackgetrelaxationpoint. Cutting planes can then
// be added via CPXXcallbackaddusercuts.
//
// The example shows how to properly support deterministic parallel search
// with a user callback (there a significant departure here frome the legacy
// control callbacks):
//
// a) To avoid race conditions (as the callback is called simultaneously by
// multiple threads), each thread has its own working copy of the data
// structures needed to separate cutting planes. Access to global data
// is read-only.
//
// b) Thread-local data for all threads is created on THREAD_UP
// and destroyed on THREAD_DOWN. This guarantees determinism.
//
// To run this example, command line arguments are required:
//     BendersATSP2.exe {0|1} [filename]
// where
//     0         Indicates that Benders' cuts are only used as lazy constraints,
//               to separate integer infeasible solutions.
//     1         Indicates that Benders' cuts are also used as user cuts,
//               to separate fractional infeasible solutions.
//
//     filename  Is the name of the file containing the ATSP instance (arc costs).
//               If filename is not specified, the instance
//               ../../../../examples/data/atsp.dat is read
//
//
// ATSP instance defined on a directed graph G = (V, A)
// - V = {0, ..., n-1}, V0 = V \ {0}
// - A = {(i,j) : i in V, j in V, i != j }
// - forall i in V: delta+(i) = {(i,j) in A : j in V}
// - forall i in V: delta-(i) = {(j,i) in A : j in V}
// - c(i,j) = traveling cost associated with (i,j) in A
//
// Flow MILP model
//
// Modeling variables:
// forall (i,j) in A:
//    x(i,j) = 1, if arc (i,j) is selected
//           = 0, otherwise
// forall k in V0, forall (i,j) in A:
//    y(k,i,j) = flow of the commodity k through arc (i,j)
//
// Objective:
// minimize sum((i,j) in A) c(i,j) * x(i,j)
//
// Degree constraints:
// forall i in V: sum((i,j) in delta+(i)) x(i,j) = 1
// forall i in V: sum((j,i) in delta-(i)) x(j,i) = 1
//
// Binary constraints on arc variables:
// forall (i,j) in A: x(i,j) in {0, 1}
//
// Flow constraints:
// forall k in V0, forall i in V:
//    sum((i,j) in delta+(i)) y(k,i,j) - sum((j,i) in delta-(i)) y(k,j,i) = q(k,i)
//    where q(k,i) =  1, if i = 0
//                 = -1, if k == i
//                 =  0, otherwise
//
// Capacity constraints:
// forall k in V0, for all (i,j) in A: y(k,i,j) <= x(i,j)
//
// Nonnegativity of flow variables:
// forall k in V0, for all (i,j) in A: y(k,i,j) >= 0

using ILOG.Concert;
using ILOG.CPLEX;

using System.Collections.Generic;

public class BendersATSP2 {

   // The BendersATSP thread-local class.
   private class Worker {

      internal readonly int numNodes;
      internal readonly int numArcs;
      internal readonly int vNumVars;
      internal readonly int uNumVars;
      internal readonly Cplex cplex;
      internal readonly INumVar[] v;
      internal readonly INumVar[] u;
      internal readonly IDictionary<INumVar,int> varMap = new Dictionary<INumVar,int>();
      internal IObjective obj;

      // The constructor sets up the Cplex algorithm to solve the worker LP, and
      // creates the worker LP (i.e., the dual of flow constraints and
      // capacity constraints of the flow MILP)
      //
      // Modeling variables:
      // forall k in V0, i in V:
      //    u(k,i) = dual variable associated with flow constraint (k,i)
      //
      // forall k in V0, forall (i,j) in A:
      //    v(k,i,j) = dual variable associated with capacity constraint (k,i,j)
      //
      // Objective:
      // minimize sum(k in V0) sum((i,j) in A) x(i,j) * v(k,i,j)
      //          - sum(k in V0) u(k,0) + sum(k in V0) u(k,k)
      //
      // Constraints:
      // forall k in V0, forall (i,j) in A: u(k,i) - u(k,j) <= v(k,i,j)
      //
      // Nonnegativity on variables v(k,i,j)
      // forall k in V0, forall (i,j) in A: v(k,i,j) >= 0
      public Worker(int numNodes) {
         this.numNodes = numNodes;
         this.numArcs = numNodes * numNodes;
         this.vNumVars = (numNodes-1) * numArcs;
         this.uNumVars = (numNodes-1) * numNodes;
         this.cplex = new Cplex();
         this.v = cplex.NumVarArray(vNumVars, 0.0,
                                    System.Double.PositiveInfinity,
                                    NumVarType.Float);
         this.u = cplex.NumVarArray(uNumVars,
                                    System.Double.NegativeInfinity,
                                    System.Double.PositiveInfinity,
                                    NumVarType.Float);
         this.obj = cplex.Minimize();

         // Set up Cplex algorithm to solve the worker LP
         cplex.SetOut(null);

         // Turn off the presolve reductions and set the CPLEX optimizer
         // to solve the worker LP with primal simplex method.
         cplex.SetParam(Cplex.Param.Preprocessing.Reduce, 0);
         cplex.SetParam(Cplex.Param.RootAlgorithm, Cplex.Algorithm.Primal);

         // Create variables v(k,i,j) forall k in V0, (i,j) in A
         // For simplicity, also dummy variables v(k,i,i) are created.
         // Those variables are fixed to 0 and do not partecipate to
         // the constraints.
         for (int k = 1; k < numNodes; ++k) {
            for (int i = 0; i < numNodes; ++i) {
               v[(k-1)*numArcs + i *numNodes + i].UB = 0.0;
            }
         }
         cplex.Add(v);

         // Set names for variables v(k,i,j)
         for (int k = 1; k < numNodes; ++k) {
            for (int i = 0; i < numNodes; ++i) {
               for (int j = 0; j < numNodes; ++j) {
                  v[(k-1)*numArcs + i*numNodes + j].Name = string.Format("v.%d.%d.%d", k, i, j);
               }
            }
         }

         // Associate indices to variables v(k,i,j)
         for (int j = 0; j < vNumVars; ++j)
            varMap[v[j]] = j;

         // Create variables u(k,i) forall k in V0, i in V
         cplex.Add(u);

         // Set names for variables u(k,i)
         for (int k = 1; k < numNodes; ++k) {
            for(int i = 0; i < numNodes; ++i) {
               u[(k-1)*numNodes + i].Name = string.Format("u.%d.%d", k, i);
            }
         }

         // Associate indices to variables u(k,i)
         for (int j = 0; j < uNumVars; ++j)
            varMap[u[j]] = vNumVars + j;

         // Initial objective function is empty
         cplex.Add(obj);

         // Add constraints:
         // forall k in V0, forall (i,j) in A: u(k,i) - u(k,j) <= v(k,i,j)
         for (int k = 1; k < numNodes; ++k) {
            for(int i = 0; i < numNodes; ++i) {
               for(int j = 0; j < numNodes; ++j) {
                  if ( i != j ) {
                     ILinearNumExpr expr = cplex.LinearNumExpr();
                     expr.AddTerm(-1.0, v[(k-1)*numArcs + i*(numNodes) + j]);
                     expr.AddTerm( 1.0, u[(k-1)*numNodes + i]);
                     expr.AddTerm(-1.0, u[(k-1)*numNodes + j]);
                     cplex.AddLe(expr, 0.0);
                  }
               }
            }
         }
      }


      // This routine separates Benders' cuts violated by the current x solution.
      // Violated cuts are found by solving the worker LP.
      // If a violated cut is found then that cut is returned, otherwise
      // <code>null</code> is returned.
      public IRange Separate(INumVar[][] x, double[][] xSol) {
         IRange cut = null;

         // Update the objective function in the worker LP:
         // minimize sum(k in V0) sum((i,j) in A) x(i,j) * v(k,i,j)
         //          - sum(k in V0) u(k,0) + sum(k in V0) u(k,k)
         cplex.Remove(obj);
         ILinearNumExpr objExpr = cplex.LinearNumExpr();
         for (int k = 1; k < numNodes; ++k) {
            for (int i = 0; i < numNodes; ++i) {
               for (int j = 0; j < numNodes; ++j) {
                  objExpr.AddTerm(xSol[i][j], v[(k-1)*numArcs + i*numNodes + j]);
               }
            }
         }
         for (int k = 1; k < numNodes; ++k) {
            objExpr.AddTerm( 1.0, u[(k-1)*numNodes + k]);
            objExpr.AddTerm(-1.0, u[(k-1)*numNodes]);
         }
         obj = cplex.Minimize(objExpr);
         cplex.Add(obj);

         // Solve the worker LP
         cplex.Solve();

         // A violated cut is available iff the solution status is Unbounded
         if ( cplex.GetStatus() == Cplex.Status.Unbounded ) {
            // Get the violated cut as an unbounded ray of the worker LP
            ILinearNumExpr ray = cplex.GetRay();

            // Compute the cut from the unbounded ray. The cut is:
            // sum((i,j) in A) (sum(k in V0) v(k,i,j)) * x(i,j) >=
            // sum(k in V0) u(k,0) - u(k,k)
            ILinearNumExpr cutLhs = cplex.LinearNumExpr();
            double cutRhs = 0.0;

            for (ILinearNumExprEnumerator e = ray.GetLinearEnumerator();
                 e.MoveNext();
                 )
            {
               INumVar var = e.NumVar;
               double val = e.Value;
               int index = varMap[var];

               if ( index >= vNumVars ) {
                  index -= vNumVars;
                  int k = index / numNodes + 1;
                  int i = index - (k-1)*numNodes;
                  if ( i == 0 )
                     cutRhs += val;
                  else if ( i == k )
                     cutRhs -= val;
               }
               else {
                  int k = index / numArcs + 1;
                  int i = (index - (k-1)*numArcs) / numNodes;
                  int j = index - (k-1)*numArcs - i*numNodes;
                  cutLhs.AddTerm(val, x[i][j]);
               }
            }
            cut = cplex.Ge(cutLhs, cutRhs);
         }

         return cut;
      } // END separate
   } // END worker

   private class BendersATSPCallback : Cplex.Callback.Function {
      internal readonly INumVar[][] x;
      internal readonly Worker[] workers;

      public BendersATSPCallback(INumVar[][] x, int numWorkers) {
         this.x = x;
         this.workers = new Worker[numWorkers];
      }

      public void Invoke(Cplex.Callback.Context context) {
         int threadNo = context.GetIntInfo(Cplex.Callback.Context.Info.ThreadId);
         int numNodes = x.Length;

         // setup
         if (context.InThreadUp) {
            workers[threadNo] = new Worker(numNodes);
            return;
         }

         // teardown
         if (context.InThreadDown) {
            workers[threadNo] = null;
            return;
         }

         double[][] xSol = new double[numNodes][];

         // Get the current x solution
         if ( context.InCandidate ) {
            if ( !context.IsCandidatePoint() ) // The model is always bounded
               throw new ILOG.Concert.Exception("Unbounded solution");
            for (int i = 0; i < numNodes; ++i) {
               xSol[i] = context.GetCandidatePoint(x[i]);
            }
         }
         else if ( context.InRelaxation ) {
            for (int i = 0; i < numNodes; ++i) {
               xSol[i] = context.GetRelaxationPoint(x[i]);
            }
         }
         else {
            throw new ILOG.Concert.Exception("Unexpected contextID");
         }

         // Get the right worker
         Worker worker = workers[threadNo];

         // Separate cut
         IRange violated = worker.Separate(x, xSol);

         if (violated != null) {
            // Add the cut
            if ( context.InCandidate )
               context.RejectCandidate(violated);
            else if ( context.InRelaxation )
               context.AddUserCut(violated,
                                  Cplex.CutManagement.UseCutPurge,
                                  false);
            else
               throw new ILOG.Concert.Exception("Unexpected contextID");
            }
      }
   } // END BendersATSPCallback

   public static void Main(string[] args) {
      string fileName = "../../../../examples/data/atsp.dat";

      // Check the command line arguments
      if ( args.Length != 1 && args.Length != 2) {
         Usage ();
         System.Environment.Exit(-1);
      }

      if ( !(args[0].Equals("0") || args[0].Equals("1")) ) {
         Usage ();
         System.Environment.Exit(-1);
      }

      bool separateFracSols = System.Int32.Parse(args[0]) != 0;

      using (Cplex masterCplex = new Cplex()) {
         masterCplex.Output().Write("Benders' cuts separated to cut off: ");
         if ( separateFracSols ) {
            masterCplex.Output().WriteLine("Integer and fractional infeasible solutions.");
         }
         else {
            masterCplex.Output().WriteLine("Only integer infeasible solutions.");
         }

         if ( args.Length == 2 )  fileName = args[1];

         // Read arc_costs from data file (9 city problem)
         InputDataReader reader = new InputDataReader(fileName);
         double[][] arcCost = reader.ReadDoubleArrayArray();

         // create master ILP
         int numNodes = arcCost.Length;
         INumVar[][] x = new INumVar[numNodes][];
         CreateMasterILP(masterCplex, x, arcCost);

         int numThreads = masterCplex.GetNumCores();

         // Set up the callback to be used for separating Benders' cuts
         BendersATSPCallback cb = new BendersATSPCallback(x, numThreads);
         long contextmask = Cplex.Callback.Context.Id.Candidate
            | Cplex.Callback.Context.Id.ThreadUp
            | Cplex.Callback.Context.Id.ThreadDown;
         if ( separateFracSols )
            contextmask |= Cplex.Callback.Context.Id.Relaxation;
         masterCplex.Use(cb, contextmask);

         // Solve the model and write out the solution
         if ( masterCplex.Solve() ) {
            Cplex.Status solStatus = masterCplex.GetStatus();
            masterCplex.Output().WriteLine("Solution status: " + solStatus);
            masterCplex.Output().WriteLine("Objective value: " + masterCplex.GetObjValue());

            if ( solStatus == Cplex.Status.Optimal ) {
               // Write out the optimal tour
               double[][] sol = new double[numNodes][];
               int[] succ = new int[numNodes];
               for (int j = 0; j < numNodes; ++j)
                  succ[j] = -1;

               for (int i = 0; i < numNodes; i++) {
                  sol[i] = masterCplex.GetValues(x[i]);
                  for (int j = 0; j < numNodes; j++) {
                     if ( sol[i][j] > 1e-03 ) succ[i] = j;
                  }
               }

               masterCplex.Output().WriteLine("Optimal tour:");
               int node = 0;
               while ( succ[node] != 0 ) {
                  masterCplex.Output().Write(node + ", ");
                  node = succ[node];
               }
               masterCplex.Output().WriteLine(node);
            }
            else {
               masterCplex.Output().WriteLine("Solution status is not Optimal");
            }
         }
         else {
            masterCplex.Output().WriteLine("No solution available");
         }
      }
   }

   // This routine creates the master ILP (arc variables x and degree constraints).
   //
   // Modeling variables:
   // forall (i,j) in A:
   //    x(i,j) = 1, if arc (i,j) is selected
   //           = 0, otherwise
   //
   // Objective:
   // minimize sum((i,j) in A) c(i,j) * x(i,j)
   //
   // Degree constraints:
   // forall i in V: sum((i,j) in delta+(i)) x(i,j) = 1
   // forall i in V: sum((j,i) in delta-(i)) x(j,i) = 1
   //
   // Binary constraints on arc variables:
   // forall (i,j) in A: x(i,j) in {0, 1}
   private static void CreateMasterILP(CplexModeler mod, INumVar[][] x, double[][] arcCost) {
      int numNodes = x.Length;

      // Create variables x(i,j) for (i,j) in A
      // For simplicity, also dummy variables x(i,i) are created.
      // Those variables are fixed to 0 and do not partecipate to
      // the constraints.
      for (int i = 0; i < numNodes; ++i) {
         x[i] = mod.IntVarArray(numNodes, 0, 1);
         x[i][i].UB = 0;
         for (int j = 0; j < numNodes; ++j) {
            x[i][j].Name = string.Format("x.%d.%d", i, j);
         }
         mod.Add(x[i]);
      }

      // Create objective function: minimize sum((i,j) in A ) c(i,j) * x(i,j)
      ILinearNumExpr obj = mod.LinearNumExpr();
      for (int i = 0; i < numNodes; ++i) {
         arcCost[i][i] = 0;
         obj.Add(mod.ScalProd(x[i], arcCost[i]));
      }
      mod.AddMinimize(obj);

      // Add the out degree constraints.
      // forall i in V: sum((i,j) in delta+(i)) x(i,j) = 1
      for (int i = 0; i < numNodes; ++i) {
         ILinearNumExpr expr = mod.LinearNumExpr();
         for (int j = 0;   j < i; ++j)
            expr.AddTerm(1.0, x[i][j]);
         for (int j = i+1; j < numNodes; ++j)
            expr.AddTerm(1.0, x[i][j]);
         mod.AddEq(expr, 1.0);
      }

      // Add the in degree constraints.
      // forall i in V: sum((j,i) in delta-(i)) x(j,i) = 1
      for (int i = 0; i < numNodes; i++) {
         ILinearNumExpr expr = mod.LinearNumExpr();
         for (int j = 0;   j < i; j++)
            expr.AddTerm(1.0, x[j][i]);
         for (int j = i+1; j < numNodes; j++)
            expr.AddTerm(1.0, x[j][i]);
         mod.AddEq(expr, 1.0);
      }
   }// END createMasterILP

   private static void Usage () {
      System.Console.WriteLine("Usage: BendersATSP2.exe {0|1} [filename]");
      System.Console.WriteLine(" 0:        Benders' cuts only used as lazy constraints,");
      System.Console.WriteLine("           to separate integer infeasible solutions.");
      System.Console.WriteLine(" 1:        Benders' cuts also used as user cuts,");
      System.Console.WriteLine("           to separate fractional infeasible solutions.");
      System.Console.WriteLine(" filename: ATSP instance file name.");
      System.Console.WriteLine("           File ../../../../examples/data/atsp.dat used if no name is provided.");
   } // END usage
}
