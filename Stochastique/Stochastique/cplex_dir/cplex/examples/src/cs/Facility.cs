// --------------------------------------------------------------------------
// File: Facility.cs
// Version 12.8.0  
// --------------------------------------------------------------------------
// Licensed Materials - Property of IBM
// 5725-A06 5725-A29 5724-Y48 5724-Y49 5724-Y54 5724-Y55 5655-Y21
// Copyright IBM Corporation 2003, 2017. All Rights Reserved.
//
// US Government Users Restricted Rights - Use, duplication or
// disclosure restricted by GSA ADP Schedule Contract with
// IBM Corp.
// --------------------------------------------------------------------------

using ILOG.Concert;
using ILOG.CPLEX;

/// <summary>
/// Solve a capacitated facility location problem, potentially using Benders
/// decomposition.
/// <para>
/// The model solved here is
/// <code>
///   minimize
///       sum(j in locations) fixedCost[j]// open[j] +
///       sum(j in locations) sum(i in clients) cost[i][j] * supply[i][j]
///   subject to
///       sum(j in locations) supply[i][j] == 1                    for each
///                                                                client i
///       sum(i in clients) supply[i][j] <= capacity[j] * open[j]  for each
///                                                                location j
///       supply[i][j] in [0,1]
///       open[j] in {0, 1}
/// </code>
/// For further details see the <see cref="usage()"/> function.
/// </para>
/// </summary>
public class Facility {
   internal static double[]   capacity;
   internal static double[]   fixedCost;
   internal static double[][] cost;

   internal static int nbLocations;
   internal static int nbClients;

   /// <summary>
   /// Dump a usage message and exit with error.
   /// </summary>
   internal static void Usage() {
      System.Console.WriteLine("Usage: java Facility [options] [inputfile]");
      System.Console.WriteLine(" where");
      System.Console.WriteLine("   inputfile describes a capacitated facility location instance as in");
      System.Console.WriteLine("   ../../../../examples/data/facility.dat. If no input file");
      System.Console.WriteLine("   is specified read the file in example/data directory.");
      System.Console.WriteLine("   Options are:");
      System.Console.WriteLine("   -a solve problem with Benders letting CPLEX do the decomposition");
      System.Console.WriteLine("   -b solve problem with Benders specifying a decomposition");
      System.Console.WriteLine("   -d solve problem without using decomposition (default)");
      System.Console.WriteLine(" Exiting...");
      System.Environment.Exit(-1);
   }

   /// <summary>
   /// Read data from <code>fileName</code> and store it in this class's
   /// <see cref="capacity"/>, <see cref="fixedCost"/>, <see cref="cost">,
   /// <see cref="nbLocations"/>, and <see cref="nbClients"/> fields.
   /// </summary>
   /// <param name="filename">Name of the file to read.</param>
   internal static void ReadData(string fileName) {
      System.Console.WriteLine("Reading data from " + fileName);
      InputDataReader reader = new InputDataReader(fileName);
    
      fixedCost = reader.ReadDoubleArray();
      cost      = reader.ReadDoubleArrayArray();
      capacity  = reader.ReadDoubleArray();
    
      nbLocations = capacity.Length;
      nbClients   = cost.Length;
    
      // Check consistency of data.
      for(int i = 0; i < nbClients; i++)
         if ( cost[i].Length != nbLocations )
           throw new System.ArgumentException("inconsistent data in file " + fileName);
   }

   /// <summary>
   /// Benders decomposition used for solving the model.
   /// </summary>
   internal enum BendersType { NO_BENDERS, AUTO_BENDERS, ANNO_BENDERS };

   /// <summary>
   /// Solve capacitated facility location problem.
   /// </summary>
   public static void Main( string[] args ) {
      try {
         // Parse command line
         string filename  = "../../../../examples/data/facility.dat";
         BendersType benders = BendersType.NO_BENDERS;
         foreach (string arg in args) {
            if (arg.StartsWith("-")) {
               if (arg.Equals("-a")) benders = BendersType.AUTO_BENDERS;
               else if (arg.Equals("-b")) benders = BendersType.ANNO_BENDERS;
               else if (arg.Equals("-d")) benders = BendersType.NO_BENDERS;
               else Usage();
            }
            else
               filename = arg;
         }

         // Read data.
         ReadData(filename);
       
         // Create the modeler/solver.
         Cplex cplex = new Cplex();

         // Create variables. We have variables
         // open[j]        if location j is open.
         // supply[i][j]]  how much client i is supplied from location j
         INumVar[] open = cplex.BoolVarArray(nbLocations);
         INumVar[][] supply = new INumVar[nbClients][];
         for(int i = 0; i < nbClients; i++)
            supply[i] = cplex.NumVarArray(nbLocations, 0.0, 1.0);

         // Constraint: Each client i must be assigned to exactly one location:
         //   sum(j in nbLocations) supply[i][j] == 1  for each i in nbClients
         for(int i = 0; i < nbClients; i++)
            cplex.AddEq(cplex.Sum(supply[i]), 1);
         // Constraint: For each location j, the capacity of the location must
         //             be respected:
         //   sum(i in nbClients) supply[i][j] <= capacity[j] * open[j]       
         for(int j = 0; j < nbLocations; j++) {
            ILinearNumExpr v = cplex.LinearNumExpr();
            for(int i = 0; i < nbClients; i++)
               v.AddTerm(1.0, supply[i][j]);
            cplex.AddLe(v, cplex.Prod(capacity[j], open[j]));
         }

         // Objective: Minimize the sum of fixed costs for using a location
         //            and the costs for serving a client from a specific location.       
         ILinearNumExpr obj = cplex.ScalProd(fixedCost, open);
         for(int i = 0; i < nbClients; i++)
            obj.Add(cplex.ScalProd(cost[i], supply[i]));       
         cplex.AddMinimize(obj);

         // Setup Benders decomposition if required.
         switch (benders) {
            case BendersType.ANNO_BENDERS:
               // We specify the structure for doing a Benders decomposition by
               // telling CPLEX which variables are in the master problem using
               // annotations. By default variables are assigned value
               // CPX_BENDERS_MASTERVALUE+1 and thus go into the workers.
               // Variables open[j] should go into the master and therefore
               // we assign them value CPX_BENDERS_MASTER_VALUE.
               Cplex.LongAnnotation decomp =
                  cplex.NewLongAnnotation(Cplex.CPX_BENDERS_ANNOTATION,
                                          Cplex.CPX_BENDERS_MASTERVALUE + 1);
               for (int j = 0; j < nbLocations; ++j)
                  cplex.SetAnnotation(decomp, open[j], Cplex.CPX_BENDERS_MASTERVALUE);
               System.Console.WriteLine("Solving with explicit Benders decomposition.");
               break;
            case BendersType.AUTO_BENDERS:
               // Let CPLEX automatically decompose the problem.  In the case of
               // a capacitated facility location problem the variables of the
               // master problem should be the integer variables.  By setting the
               // Benders strategy parameter to Full, CPLEX will put all integer
               // variables into the master, all continuous varibles into a
               // subproblem, and further decompose that subproblem, if possible.
               cplex.SetParam(Cplex.Param.Benders.Strategy,
                              Cplex.BendersStrategy.Full);
               System.Console.WriteLine("Solving with automatic Benders decomposition.");
               break;
            case BendersType.NO_BENDERS:
               System.Console.WriteLine("Solving without Benders decomposition.");
               break;
         }

         // Solve and display solution
         if (cplex.Solve()) {
            System.Console.WriteLine("Solution status = " + cplex.GetStatus());
            double tolerance = cplex.GetParam(Cplex.Param.MIP.Tolerances.Integrality);
            System.Console.WriteLine("Optimal value: " + cplex.ObjValue);
            for(int j = 0; j < nbLocations; j++) {
               if (cplex.GetValue(open[j]) >= 1 - tolerance) {
                  System.Console.Write("Facility " + j +" is open, it serves clients");
                  for(int i = 0; i < nbClients; i++)
                     if (cplex.GetValue(supply[i][j]) >= 1 - tolerance)
                        System.Console.Write(" " + i);
                  System.Console.WriteLine(); 
               }
            }
         }
         cplex.End();
      }
      catch(ILOG.Concert.Exception exc) {
         System.Console.WriteLine("Concert exception '" + exc + "' caught");
         System.Environment.Exit(-1);
      }
      catch (System.IO.IOException exc) {
         System.Console.WriteLine("Error reading file " + args[0] + ": " + exc);
         System.Environment.Exit(-1);
      }
      catch (InputDataReader.InputDataReaderException exc) {
         System.Console.WriteLine(exc);
         System.Environment.Exit(-1);
      }
   }
}

