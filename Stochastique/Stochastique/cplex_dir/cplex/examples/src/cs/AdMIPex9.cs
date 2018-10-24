// --------------------------------------------------------------------------
// File: AdMIPex9.cs
// Version 12.8.0
// --------------------------------------------------------------------------
// Licensed Materials - Property of IBM
// 5725-A06 5725-A29 5724-Y48 5724-Y49 5724-Y54 5724-Y55 5655-Y21
// Copyright IBM Corporation 2001, 2017. All Rights Reserved.
//
// US Government Users Restricted Rights - Use, duplication or
// disclosure restricted by GSA ADP Schedule Contract with
// IBM Corp.
// --------------------------------------------------------------------------
//
// AdMIPex9.cs - Use the heuristic callback for optimizing a MIP problem
//
// To run this example, command line arguments are required.
// i.e.,   AdMIPex9   filename
// Note that the code assumes that all variables in the model are binary
// (see the roundDown() function).
//
// Example:
//     AdMIPex9  example.mps
//
using System;
using System.Collections;
using System.Collections.Generic;
using ILOG.Concert;
using ILOG.CPLEX;

public class AdMIPex9 {
   internal class HeuristicCallback : Cplex.Callback.Function {
      // All variables in the model.
      private readonly INumVar[] _vars;
      // Dense objective vector (aligned with _obj).
      private readonly double[] _obj;

      public HeuristicCallback(INumVar[] vars, IObjective obj) {
         _vars = vars;

         // Generate the objective as a double array for easy look up
         ILinearNumExpr objexp = (ILinearNumExpr)obj.Expr;

         // First create a map with all variables that have non-zero
         // coefficients in the objective.
         Dictionary<INumVar,double> objmap = new Dictionary<INumVar,double>();
         for (ILinearNumExprEnumerator it = objexp.GetLinearEnumerator();
              it.MoveNext();) {
            objmap[it.NumVar] = it.Value;
         }

         // Now turn the map into a dense array.
         int cols = _vars.Length;
         _obj = new double[cols];
         for (int j = 0; j < cols; j++) {
            if (objmap.ContainsKey(_vars[j]))
               _obj[j] = objmap[_vars[j]];
         }
      }

      private void roundDown(Cplex.Callback.Context context) {
         double[] x = context.GetRelaxationPoint(_vars);
         int cols = _vars.Length;
         double objrel = context.GetRelaxationObjective();

         // Heuristic motivated by knapsack constrained problems.
         // Rounding down all fractional values will give an integer
         // solution that is feasible, since all constraints are <=
         // with positive coefficients

         for (int j = 0; j < cols; j++) {
            // Set the fractional variable to zero
            // Note that we assume all variables to be binary. If the model
            // contains non-binary variable then a different update would
            // be required.
            if (x[j] > 0.0) {
               double frac = Math.Abs(Math.Round(x[j]) - x[j]);

               if (frac > 1.0e-6) {
                  objrel -= x[j]*_obj[j];
                  x[j] = 0.0;
               }
            }
         }

         // Post the rounded solution
         context.PostHeuristicSolution(
            _vars, x, 0, cols, objrel,
            Cplex.Callback.Context.SolutionStrategy.CheckFeasible);
      }

      // This is the function that we have to implement and that CPLEX will call
      // during the solution process at the places that we asked for.
      public void Invoke (Cplex.Callback.Context context) {
         if (context.InRelaxation) {
            roundDown(context);
         }
      }
   }

   public static void Main(string[] args) {
      if (args.Length != 1) {
         Console.WriteLine("Usage: AdMIPex9 filename");
         Console.WriteLine("   where filename is a file with extension ");
         Console.WriteLine("      MPS, SAV, or LP (lower case is allowed)");
         Console.WriteLine(" Exiting...");
         Environment.Exit(-1);
      }

      using (Cplex cplex = new Cplex()) {
         cplex.ImportModel(args[0]);
         IEnumerator matrixEnum = cplex.GetLPMatrixEnumerator();
         matrixEnum.MoveNext();
         ILPMatrix lp = (ILPMatrix)matrixEnum.Current;
         INumVar[] vars = lp.NumVars;

         // Now we get to setting up the callback.
         // We instanciate a HeuristicCallback and set the wherefrom parameter.
         HeuristicCallback cb = new HeuristicCallback(vars,
                                                      cplex.GetObjective());
         long wherefrom = 0;
         wherefrom |= Cplex.Callback.Context.Id.Relaxation;

         // We add the callback.
         cplex.Use(cb, wherefrom);

         // Disable heuristics so that our callback has a chance to make a
         // difference.
         cplex.SetParam(Cplex.Param.MIP.Strategy.HeuristicFreq, -1);
         if (cplex.Solve()) {
            Console.WriteLine("Solution status = " + cplex.GetStatus());
            Console.WriteLine("Solution value  = " + cplex.GetObjValue());
         }
      }
   }
}
