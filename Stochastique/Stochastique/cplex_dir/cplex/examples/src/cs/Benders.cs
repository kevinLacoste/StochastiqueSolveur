// --------------------------------------------------------------------------
// File: Benders.cs
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
// Read in a model from a file and solve it using Benders decomposition.
//
// If an annotation file is provided, use that annotation file.
// Otherwise, auto-decompose the problem and dump the annotation
// to the file 'benders.ann'.
//
// To run this example, command line arguments are required.
// i.e.,   Benders   filename   [annofile]
// where
//     filename is the name of the file, with .mps, .lp, or .sav extension
//     annofile is an optional .ann file with model annotations
//
// Example:
//     Benders  UFL_25_35_1.mps UFL_25_35_1.ann

using System;
using System.Collections;
using ILOG.Concert;
using ILOG.CPLEX;


public class Benders {
   private static void Usage() {
      System.Console.WriteLine("usage:  Benders filename [annofile]");
      System.Console.WriteLine("   where filename is a file with extension "); 
      System.Console.WriteLine("      MPS, SAV, or LP (lower case is allowed)");
      System.Console.WriteLine("   and annofile is an optional .ann file with model annotations");
      System.Console.WriteLine("      If \"create\" is used, the annotation is computed.");
      System.Console.WriteLine(" Exiting...");
   }

   public static int Main(String[] args) {
      bool hasAnnoFile = false;

      // Check the arguments.
      int argsLength = args.Length;
      if ( argsLength == 2 ) {
         hasAnnoFile = true;
      }
      else if ( argsLength != 1 ) {
         Usage();
         return -1;
      }

      try {
         // Create the modeler/solver object.
         using (Cplex cpx = new Cplex()) {

            // Read the problem file.
            cpx.ImportModel(args[0]);

            // If provided, read the annotation file.
            if ( hasAnnoFile ) {
               // Generate default annotations if annofile is "create".
               if (args[1].Equals("create")) {
                  Cplex.LongAnnotation
                     benders = cpx.NewLongAnnotation(Cplex.CPX_BENDERS_ANNOTATION,
                                                     Cplex.CPX_BENDERS_MASTERVALUE);

                  IEnumerator enumerator = cpx.GetLPMatrixEnumerator();
                  enumerator.MoveNext();
                  ILPMatrix lp = (ILPMatrix)enumerator.Current;
                  INumVar[] var = lp.NumVars;
                  foreach (INumVar v in var) {
                     if (v.Type == NumVarType.Float) {
                        cpx.SetAnnotation(benders, v,
                                          Cplex.CPX_BENDERS_MASTERVALUE+1);
                     }
                  }
               }
               else {
                  // Otherwise, read the annotation file.
                  cpx.ReadAnnotations(args[1]);
               }
            }
            else {
               // Set benders strategy to auto-generate a decomposition.
               cpx.SetParam(Cplex.Param.Benders.Strategy,
                            Cplex.BendersStrategy.Full);

               // Write out the auto-generated annotation.
               cpx.WriteBendersAnnotation("benders.ann");
            }

            // Solve the problem using Benders' decomposition.
            if ( !cpx.Solve() ) {
               throw new System.Exception("Failed to optimize.");
            }

            Cplex.Status status = cpx.GetStatus();
            double bestObjValue = cpx.GetBestObjValue();
            double objValue = cpx.GetObjValue();
            System.Console.WriteLine("Solution status: " + status);
            System.Console.WriteLine("Best bound:      " + bestObjValue);
            System.Console.WriteLine("Best integer:    " + objValue);
         }
      }
      catch (ILOG.Concert.Exception e) {
         throw new System.Exception("Concert exception caught", e);
      }

      return 0;
   }
}
