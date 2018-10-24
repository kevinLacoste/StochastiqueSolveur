// --------------------------------------------------------------------------
// File: AdMIPex5.cs
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

using System;
using System.Collections.Generic;
using System.IO;
using ILOG.Concert;
using ILOG.CPLEX;

/// <summary>
/// Solve a facility location problem with cut callbacks or lazy constraints.
///
/// Given a set of locations J and a set of clients C, the following model is
/// solved:
///
///  Minimize
///   sum(j in J) fixedCost[j]*used[j] +
///   sum(j in J)sum(c in C) cost[c][j]*supply[c][j]
///  Subject to
///   sum(j in J) supply[c][j] == 1                    for all c in C
///   sum(c in C) supply[c][j] <= (|C| - 1) * used[j]  for all j in J
///               supply[c][j] in {0, 1}               for all c in C, j in J
///                    used[j] in {0, 1}               for all j in J
///
/// In addition to the constraints stated above, the code also separates
/// a disaggregated version of the capacity constraints (see comments for the
/// cut callback) to improve performance.
///
/// Optionally, the capacity constraints can be separated from a lazy constraint
/// callback instead of being stated as part of the initial model.
///
/// See the usage message for how to switch between these options.
/// </summary>
public class AdMIPex5 {
   /// <summary>Epsilon used for violation of cuts.</summary>
   internal static double EPS = 1.0e-6;

   ///<summary>
   /// User cut callback to separate the disaggregated capacity constraints.
   ///
   /// In the model we have for each location j the constraint
   ///    sum(c in clients) supply[c][j] <= (nbClients-1) * used[j]
   /// Clearly, a client can only be serviced from a location that is used,
   /// so we also have a constraint
   ///    supply[c][j] <= used[j]
   /// that must be satisfied by every feasible solution. These constraints tend
   /// to be violated in LP relaxation. In this callback we separate them.
   ///</summary>
   public class Disaggregated : Cplex.UserCutCallback {
      internal readonly IModeler modeler;
      internal readonly INumVar[] used;
      internal readonly INumVar[][] supply;

      public Disaggregated(IModeler modeler, INumVar[] used, INumVar[][] supply) {
         this.modeler = modeler;
         this.used = used;
         this.supply = supply;
      }

      /// <summary>
      /// Separate cuts.
      ///
      /// CPLEX invokes this callback when separating cuts at search tree
      /// nodes (including the root node). The current fractional solution
      /// can be obtained via <see cref="GetValue"/> and
      /// <see cref="GetValues"/>. Separated cuts are added
      /// via <see cref="Add"/> or <see cref="AddLocal"/>.
      /// </summary>
      public override void Main() {
         int nbLocations = used.Length;
         int nbClients = supply.Length;

         // For each j and c check whether in the current solution (obtained by
         // calls to getValue()) we have supply[c][j]>used[j]. If so, then we have
         // found a violated constraint and add it as a cut.
         for (int j = 0; j < nbLocations; ++j) {
            for (int c = 0; c < nbClients; ++c) {
               double s = GetValue(supply[c][j]);
               double o = GetValue(used[j]);
               if ( s > o + EPS) {
                  System.Console.WriteLine("Adding: {0} <= {1} [{2} > {3}]",
                                           supply[c][j].Name, used[j].Name,
                                           s, o);
                  Add(modeler.Le(modeler.Diff(supply[c][j], used[j]), 0.0),
                      Cplex.CutManagement.UseCutPurge);
               }
            }
         }
      }
   }

   // <summary>
   /// Variant of the Disaggregated callback that does not look for violated
   /// cuts dynamically. Instead it uses a static table of cuts and scans this
   /// table for violated cuts.
   /// </summary>
   public class CutsFromTable : Cplex.UserCutCallback {
      internal readonly List<IRange> cuts;

      public CutsFromTable(List<IRange> cuts) { this.cuts = cuts; }

      public override void Main() {
         foreach (IRange cut in cuts) {
            double lhs = GetValue(cut.Expr);
            if ( lhs < cut.LB - EPS || lhs > cut.UB + EPS) {
               System.Console.WriteLine("Adding {0} [lhs={1}]",
                                        cut.ToString(), lhs);
            }
         }
      }
   }

   /// <summary>
   /// Lazy constraint callback to enforce the capacity constraints.
   ///
   /// If used then the callback is invoked for every integer feasible solution
   /// CPLEX finds. For each location j it checks whether constraint
   ///    sum(c in C) supply[c][j] <= (|C| - 1) * used[j]
   /// is satisfied. If not then it adds the violated constraint as lazy
   /// constraint.
   /// </summary>
   public class LazyCallback : Cplex.LazyConstraintCallback {
      private readonly IModeler modeler;
      private readonly INumVar[] used;
      private readonly INumVar[][] supply;

      public LazyCallback(IModeler modeler, INumVar[] used, INumVar[][] supply) {
         this.modeler = modeler;
         this.used = used;
         this.supply = supply;
      }

      public override void Main() {
         int nbLocations = used.Length;
         int nbClients = supply.Length;
         for (int j = 0; j < nbLocations; ++j) {
            double isused = GetValue(used[j]);
            double served = 0.0; // Number of clients currently served from j
            for (int c = 0; c < nbClients; ++c)
               served += GetValue(supply[c][j]);
            if ( served > (nbClients - 1.0) * isused + EPS ) {
               ILinearNumExpr sum = modeler.LinearNumExpr();
               for (int c = 0; c < nbClients; ++c)
                  sum.AddTerm(1.0, supply[c][j]);
               sum.AddTerm(-(nbClients - 1), used[j]);
               System.Console.WriteLine("Adding lazy capacity constraint {0} <= 0",
                                        sum.ToString());
               Add(modeler.Le(sum, 0.0));
            }
         }
      }
   }

   internal static void Usage() {
      System.Console.WriteLine("Usage: AdMIPex5 [options...]");
      System.Console.WriteLine(" By default, a user cut callback is used to dynamically");
      System.Console.WriteLine(" separate constraints.");
      System.Console.WriteLine();
      System.Console.WriteLine(" Supported options are:");
      System.Console.WriteLine("  -table       Instead of the default behavior, use a");
      System.Console.WriteLine("               static table that holds all cuts and" );
      System.Console.WriteLine("               scan that table for violated cuts."         );
      System.Console.WriteLine("  -no-cuts     Do not separate any cuts."                  );
      System.Console.WriteLine("  -lazy        Do not include capacity constraints in the"   );
      System.Console.WriteLine("               model. Instead, separate them from a lazy"   );
      System.Console.WriteLine("               constraint callback."                       );
      System.Console.WriteLine("  -data=<dir>  Specify the directory in which the data"    );
      System.Console.WriteLine("               file facility.dat is located."              );
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
            System.Console.WriteLine("Unknown argument " + arg);
            Usage();
         }
      }

      // Setup input file name and used the file.
      InputDataReader reader = new InputDataReader(new FileInfo(Path.Combine(datadir, "facility.dat")).FullName);
      double[] fixedCost = reader.ReadDoubleArray();
      double[][] cost = reader.ReadDoubleArrayArray();
      int nbLocations = fixedCost.Length;
      int nbClients   = cost.Length;

      using (Cplex cplex = new Cplex())
      {
         // Create variables.
         // - used[j]      If location j is used.
         // - supply[c][j] Amount shipped from location j to client c. This is a
         //                number in [0,1] and specifies the percentage of c's
         //                demand that is served from location i.
         INumVar[] used = cplex.BoolVarArray(nbLocations);
         for (int j = 0; j < nbLocations; ++j)
            used[j].Name = "used(" + j + ")";
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
               cplex.AddLe(v, cplex.Prod(nbClients - 1, used[j]));
            }
         }

         // Objective function. We have the fixed cost for useding a location
         // and the cost proportional to the amount that is shipped from a
         // location.
         ILinearNumExpr obj = cplex.ScalProd(fixedCost, used);
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

         if ( useCallback ) {
            if ( fromTable ) {
               // Generate all disaggregated constraints and put them into a
               // table that is scanned by the callback.
               List<IRange> cuts = new List<IRange>();
               for (int j = 0; j < nbLocations; ++j)
                  for (int c = 0; c < nbClients; ++c)
                     cuts.Add(cplex.Le(cplex.Diff(supply[c][j], used[j]), 0.0));
               cplex.Use(new CutsFromTable(cuts));
            }
            else {
               cplex.Use(new Disaggregated(cplex, used, supply));
            }
         }
         if ( lazy )
            cplex.Use(new LazyCallback(cplex, used, supply));

         if ( !cplex.Solve() )
            throw new SystemException("No feasible solution found");
	
         System.Console.WriteLine("Solution status:                   " +
                                  cplex.GetStatus());
         System.Console.WriteLine("Nodes processed:                   " +
                                  cplex.Nnodes);
         System.Console.WriteLine("Active user cuts/lazy constraints: " +
                                  cplex.GetNcuts(Cplex.CutType.User));
         double tolerance = cplex.GetParam(Cplex.Param.MIP.Tolerances.Integrality);
         System.Console.WriteLine("Optimal value:                     " +
                                  cplex.GetObjValue());
         for (int j = 0; j < nbLocations; j++) {
            if (cplex.GetValue(used[j]) >= 1 - tolerance) {
               System.Console.Write("Facility " + j + " is used, it serves clients");
               for (int i = 0; i < nbClients; i++) {
                  if (cplex.GetValue(supply[i][j]) >= 1 - tolerance)
                     System.Console.Write(" " + i);
               }
               System.Console.WriteLine();
            }
         }
      }
   }
}
