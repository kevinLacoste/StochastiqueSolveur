// --------------------------------------------------------------------------
// File: AdMIPex8.cs
// Version 12.8.0  
// --------------------------------------------------------------------------
// Licensed Materials - Property of IBM
// 5725-A06 5725-A29 5724-Y48 5724-Y49 5724-Y54 5724-Y55 5655-Y21
// Copyright IBM Corporation 2017. All Rights Reserved.
//
// US Government Users Restricted Rights - Use, duplication or
// disclosure restricted by GSA ADP Schedule Contract with
// IBM Corp.
// --------------------------------------------------------------------------

using System;
using System.Collections.Generic;
using System.IO;
using ILOG.Concert;
using ILOG.CPLEX;

/// <summary>
/// Solve a facility location problem with cut or lazy constraint using the
/// new callback api.
///
/// Given a set of locations J and a set of clients C, the following model is
/// solved:
///
///  Minimize
///   sum(j in J) fixedCost[j]*opened[j] +
///   sum(j in J)sum(c in C) cost[c][j]*supply[c][j]
///  Subject to
///   sum(j in J) supply[c][j] == 1                      for all c in C
///   sum(c in C) supply[c][j] <= (|C| - 1) * opened[j]  for all j in J
///               supply[c][j] in {0, 1}                 for all c in C, j in J
///                    opened[j] in {0, 1}               for all j in J
///
/// In addition to the constraints stated above, the code also separates
/// a disaggregated version of the capacity constraints (see comments for the
/// cut callback) to improve performance.
///
/// Optionally, the capacity constraints can be separated from a lazy
/// constraint callback instead of being stated as part of the initial model.
///
/// See the usage message for how to switch between these options.
/// </summary>
public class AdMIPex8 {

   /// <summary>Epsilon used for violation of cuts. </summary>
   internal static double EPS = 1e-6;

   /// <summary>
   /// This is the class implementing the callback for facility location.
   ///
   /// It has three main functions:
   ///    - disaggregate: add disagrregated constraints linking clients and
   ///      location.
   ///    - fromTable: do the same using a cut table.
   ///    - lazyCapacity: adds the capacity constraint as a lazy constrain.
   /// </summary>
   public class FacilityCallback : Cplex.Callback.Function {
      internal readonly INumVar[] opened;
      internal readonly INumVar[][] supply;
      internal readonly List<IRange> cuts;
      public FacilityCallback(INumVar[] opened, INumVar[][] supply) {
         this.opened = opened;
         this.supply = supply;
         this.cuts = new List<IRange>();
      }

      /// <summary>
      /// Separate the disaggregated capacity constraints.
      /// In the model we have for each location j the constraint
      ///    sum(c in clients) supply[c][j] <= (nbClients-1)// opened[j]
      /// Clearly, a client can only be serviced from a location that is opened,
      /// so we also have a constraint
      ///    supply[c][j] <= opened[j]
      /// that must be satisfied by every feasible solution. These constraints tend
      /// to be violated in LP relaxation. In this callback we separate them.
      /// </summary>
      private void Disaggregate (Cplex.Callback.Context context) {
         int nbLocations = opened.Length;
         int nbClients = supply.Length;
         CplexModeler m = context.GetCplex();
      
         // For each j and c check whether in the current solution (obtained by
         // calls to getValue()) we have supply[c][j]>opened[j]. If so, then we have
         // found a violated constraint and add it as a cut.
         for (int j = 0; j < nbLocations; ++j) {
            for (int c = 0; c < nbClients; ++c) {
               double s = context.GetRelaxationPoint(supply[c][j]);
               double o = context.GetRelaxationPoint(opened[j]);
               if ( s > o + EPS) {
                  Console.WriteLine("Adding: " + supply[c][j].Name + " <= " +
                                    opened[j].Name + " [" + s + " > " + o + "]");
                  context.AddUserCut(m.Le(m.Diff(supply[c][j], opened[j]), 0.0),
                                     Cplex.CutManagement.UseCutPurge, false);
               }
            }
         }
      }
      
      /// <summary>
      /// Variant of disaggregate(ICplex.Callback.Context) that does
      /// not look for violated cuts dynamically.
      /// Instead it uses a static table of cuts and scans this table for violated cuts.
      /// </summary>
      private void CutsFromTable (Cplex.Callback.Context context) {
         foreach (IRange cut in cuts) {
            double lhs = context.GetRelaxationValue(cut.Expr);
            if (lhs < cut.LB - EPS || lhs > cut.UB + EPS ) {
               Console.WriteLine("Adding: " + cut + " [lhs = " + lhs + "]");
               context.AddUserCut(cut, Cplex.CutManagement.UseCutPurge, false);
            }
         }
      }
      
      /// <summary>
      /// Function to populate the cut table used by cutsFromTable.
      /// </summary>
      public void PopulateCutTable (CplexModeler cplex) {
         int nbLocations = opened.Length;
         int nbClients = supply.Length;
         // Generate all disaggregated constraints and put them into a
         // table that is scanned by the callback.
         cuts.Clear();
         for (int j = 0; j < nbLocations; ++j)
            for (int c = 0; c < nbClients; ++c)
               cuts.Add(cplex.Le(cplex.Diff(supply[c][j], opened[j]), 0.0));
      }

      /// <summary>
      /// Lazy constraint callback to enforce the capacity constraints.
      /// If opened then the callback is invoked for every integer feasible solution
      /// CPLEX finds. For each location j it checks whether constraint
      ///    sum(c in C) supply[c][j] <= (|C| - 1)// opened[j]
      /// is satisfied. If not then it adds the violated constraint as lazy constraint.
      /// </summary>
      private void LazyCapacity (Cplex.Callback.Context context)  {
         int nbLocations = opened.Length;
         int nbClients = supply.Length;
         CplexModeler m = context.GetCplex();
         if ( !context.IsCandidatePoint() )
            throw new SystemException("Unbounded solution");
         for (int j = 0; j < nbLocations; ++j) {
            double isUsed = context.GetCandidatePoint(opened[j]);
            double served = 0.0; // Number of clients currently served from j
            for (int c = 0; c < nbClients; ++c)
               served += context.GetCandidatePoint(supply[c][j]);
            if ( served > (nbClients - 1.0) * isUsed + EPS ) {
               ILinearNumExpr sum = m.LinearNumExpr();
               for (int c = 0; c < nbClients; ++c)
                  sum.AddTerm(1.0, supply[c][j]);
               sum.AddTerm(-(nbClients - 1), opened[j]);
               Console.WriteLine("Adding lazy capacity constraint " + sum + " <= 0");
               context.RejectCandidate(m.Le(sum, 0.0));
            }
         }
      }

      /// <summary>
      /// This is the function that we have to implement and that CPLEX will call 
      /// during the solution process at the places that we asked for.
      /// </summary>
      public void Invoke (Cplex.Callback.Context context) {
         if ( context.InRelaxation ) {
            if ( cuts.Count > 0 ) {
               CutsFromTable(context);
            }
            else {
               Disaggregate(context);
            }
         }

         if ( context.InCandidate ) {
            LazyCapacity (context);
         }
      }
   }

   private static void Usage() {
      Console.WriteLine("Usage: AdMIPex8 [options...]");
      Console.WriteLine(" By default, a user cut callback is used to dynamically");
      Console.WriteLine(" separate constraints.");
      Console.WriteLine();
      Console.WriteLine(" Supported options are:");
      Console.WriteLine("  -table       Instead of the default behavior, use a"     );
      Console.WriteLine("               static table that holds all cuts and"       );
      Console.WriteLine("               scan that table for violated cuts."         );
      Console.WriteLine("  -no-cuts     Do not separate any cuts."                  );
      Console.WriteLine("  -lazy        Do not include capacity constraints in the" );
      Console.WriteLine("               model. Instead, separate them from a lazy"  );
      Console.WriteLine("               constraint callback."                       );
      Console.WriteLine("  -data=<dir>  Specify the directory in which the data"    );
      Console.WriteLine("               file facility.dat is located."              );
      Environment.Exit(2);
   }

   public static void Main(string[] args) {
      // Set default arguments and parse command line.
      string datadir = "../../../../examples/data";
      bool fromTable = false;
      bool lazy = false;
      bool useCallback = true;

      foreach (string arg in args) {
         if ( arg.StartsWith("-data=") )
            datadir = arg.Substring(6);
         else if ( arg.Equals("-table") )
            fromTable = true;
         else if ( arg.Equals("-lazy") )
            lazy = true;
         else if ( arg.Equals("-no-cuts") )
            useCallback = false;
         else {
            Console.WriteLine("Unknown argument " + arg);
            Usage();
         }
      }

      // Setup input file name and use the file.
      InputDataReader reader = new InputDataReader(new FileInfo(Path.Combine(datadir, "facility.dat")).FullName);
      double[] fixedCost = reader.ReadDoubleArray();
      double[][] cost = reader.ReadDoubleArrayArray();
      int nbLocations = fixedCost.Length;
      int nbClients   = cost.Length;

      using (Cplex cplex = new Cplex()) {
         // Create variables.
         // - opened[j]    If location j is used.
         // - supply[c][j] Amount shipped from location j to client c. This is a
         //                number in [0,1] and specifies the percentage of c's
         //                demand that is served from location i.
         INumVar[] opened = cplex.BoolVarArray(nbLocations);
         for (int j = 0; j < nbLocations; ++j)
            opened[j].Name = "used(" + j + ")";
         INumVar[][] supply = new INumVar[nbClients][];
         for (int c = 0; c < nbClients; c++) {
            supply[c] = cplex.BoolVarArray(nbLocations);
            for (int j = 0; j < nbLocations; ++j)
               supply[c][j].Name = "supply(" + c + ")(" + j + ")";
         }

         // The supply for each client must sum to 1, i.e., the demand of each
         // client must be met.
         for (int c = 0; c < nbClients; c++)
            cplex.AddEq(cplex.Sum(supply[c], 0, supply[c].Length), 1);

         // Capacity constraint for each location. We just require that a single
         // location cannot serve all clients, that is, the capacity of each
         // location is nbClients-1. This makes the model a little harder to
         // solve and allows us to separate more cuts.
         if ( !lazy ) {
            for (int j = 0; j < nbLocations; j++) {
               ILinearNumExpr v = cplex.LinearNumExpr();
               for (int c = 0; c < nbClients; c++)
                  v.AddTerm(1.0, supply[c][j]);
               cplex.AddLe(v, cplex.Prod(nbClients - 1, opened[j]));
            }
         }

         // Objective function. We have the fixed cost for useding a location
         // and the cost proportional to the amount that is shipped from a
         // location.
         ILinearNumExpr obj = cplex.ScalProd(fixedCost, opened);
         for (int c = 0; c < nbClients; c++) {
            obj.Add(cplex.ScalProd(cost[c], supply[c]));
         }
         cplex.AddMinimize(obj);

         // Tweak some CPLEX parameters so that CPLEX has a harder time to
         // solve the model and our cut separators can actually kick in.
         cplex.SetParam(Cplex.Param.Threads, 1);
         cplex.SetParam(Cplex.Param.MIP.Strategy.HeuristicFreq, -1);
         cplex.SetParam(Cplex.Param.MIP.Cuts.MIRCut, -1);
         cplex.SetParam(Cplex.Param.MIP.Cuts.Implied, -1);
         cplex.SetParam(Cplex.Param.MIP.Cuts.Gomory, -1);
         cplex.SetParam(Cplex.Param.MIP.Cuts.FlowCovers, -1);
         cplex.SetParam(Cplex.Param.MIP.Cuts.PathCut, -1);
         cplex.SetParam(Cplex.Param.MIP.Cuts.LiftProj, -1);
         cplex.SetParam(Cplex.Param.MIP.Cuts.ZeroHalfCut, -1);
         cplex.SetParam(Cplex.Param.MIP.Cuts.Cliques, -1);
         cplex.SetParam(Cplex.Param.MIP.Cuts.Covers, -1);

         // Now we get to setting up the callback.
         // We instanciate a FacilityCallback and set the wherefrom parameter.
         FacilityCallback fcCallback = new FacilityCallback(opened, supply);
         long wherefrom = 0;
         if ( useCallback ) {
            wherefrom |= Cplex.Callback.Context.Id.Relaxation;
            if ( fromTable ) {
               fcCallback.PopulateCutTable(cplex);
            }
         }

         if ( lazy )
            wherefrom |= Cplex.Callback.Context.Id.Candidate;

         // If wherefrom is not zero we add the callback.
         if ( wherefrom != 0 )
            cplex.Use(fcCallback, wherefrom);

         if ( !cplex.Solve() )
            throw new SystemException("No feasible solution found");
	
         Console.WriteLine("Solution status:                   " +
                           cplex.GetStatus());
         Console.WriteLine("Nodes processed:                   " +
                           cplex.Nnodes);
         Console.WriteLine("Active user cuts/lazy constraints: " +
                           cplex.GetNcuts(Cplex.CutType.User));
         double tolerance = cplex.GetParam(Cplex.Param.MIP.Tolerances.Integrality);
         Console.WriteLine("Optimal value:                     " +
                           cplex.GetObjValue());
         for (int j = 0; j < nbLocations; j++) {
            if (cplex.GetValue(opened[j]) >= 1 - tolerance) {
               Console.Write("Facility " + j + " is used, it serves clients");
               for (int i = 0; i < nbClients; i++) {
                  if (cplex.GetValue(supply[i][j]) >= 1 - tolerance)
                     Console.Write(" " + i);
               }
               Console.WriteLine();
            }
         }
      }
   }
}
