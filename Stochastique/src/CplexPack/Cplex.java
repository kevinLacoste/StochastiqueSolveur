package CplexPack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ilog.concert.*;
import ilog.cplex.*;

public class Cplex extends Solveur 
{
	private IloCplex modele;
	private IloLinearNumExpr fctObj;
	private IloNumVar[] x;
	
	public Cplex()
	{
		super();
		try
		{
			modele = new IloCplex();
			modele.setOut(null);	//Evite d'avoir plein de messages de cplex dans la console
			isInit = false;
			isSolved = false;
		}
		catch (IloException e) 
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public void definePL(int vectSolDimension, dataType dt, ArrayList<HashMap<Integer, Double>> matContraintes,
			ArrayList<Double> secondMembre, ArrayList<inequalitySign> inequalitySigns, ArrayList<Double> fctObj) {
		super.definePL(vectSolDimension, dt, matContraintes, secondMembre, inequalitySigns, fctObj);
		try {
			modele = new IloCplex();
			modele.setOut(null);	//Evite d'avoir plein de messages de cplex dans la console
			isInit = false;
			isSolved = false;
		}
		catch (IloException e) 
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public void definePL(PL probleme) {
		super.definePL(probleme);
		try {
			modele = new IloCplex();
			modele.setOut(null);	//Evite d'avoir plein de messages de cplex dans la console
			isInit = false;
			isSolved = false;
		}
		catch (IloException e) 
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public void initPL()
	{
		if(modele != null) try {
			
			// **** Etablissement du type des donnees : bool, int ou double **** //
			//Probleme a valeurs booleenes
			if(probleme.getDataType() == dataType.bool) {
				x = modele.boolVarArray(probleme.getVectSolDimension());
			}
			
			//Probleme a valeurs entieres
			else if(probleme.getDataType() == dataType.integer) {
				x = modele.intVarArray(probleme.getVectSolDimension(), 0, Integer.MAX_VALUE);
			}
			
			//Probleme a valeurs reelles
			else if(probleme.getDataType() == dataType.longFloat) {
				x = modele.numVarArray(probleme.getVectSolDimension(), 0, Double.MAX_VALUE);
			}
			
			// **** Etablissement de la fonction objectif **** //
			this.fctObj = modele.linearNumExpr();
			for(int i=0; i<probleme.getVectSolDimension();i++) {
				this.fctObj.addTerm(probleme.getFctObjCoeff(i), x[i]);
			}
			
			// **** Etablissement des contraintes **** //
			IloLinearNumExpr expr;
			HashMap<Integer, Double> contrainte;
			Iterator<Map.Entry<Integer, Double>> it;
			double sc;
			for(int i=0; i<probleme.getNbContraintes();i++) {
				expr = modele.linearNumExpr();
				contrainte = probleme.getContrainte(i);
				sc = probleme.getSCValue(i);
				
				it = contrainte.entrySet().iterator();
			    while (it.hasNext()) {
			        Map.Entry<Integer, Double> pair = (Map.Entry<Integer, Double>)it.next();
			        expr.addTerm(pair.getValue(), x[pair.getKey()]);
			    }
				
				if(probleme.getInequalityValue(i) == inequalitySign.Eq)
					modele.addEq(expr, sc);
				else if (probleme.getInequalityValue(i) == inequalitySign.GreatEq)
					modele.addGe(expr, sc);
				else if (probleme.getInequalityValue(i) == inequalitySign.LowEq)
					modele.addLe(expr, sc);
			}
			
			isInit = true;
		}
		catch(IloException e)
		{
			e.printStackTrace();
		}
		
		//TODO else exception
	}
	
	@Override
	public void optimize() 
	{
		if(isInit) try
		{
			isSolved = modele.solve();
			if (isSolved)
			{
				System.out.println("Obj = " + modele.getObjValue());
				ArrayList<Double> solution = new ArrayList<Double>();
				for(int i=0;i<x.length;i++)
				{
					solution.add(modele.getValue(x[i]));
				}
				this.probleme.setSolution(solution);
			}
		} 
		catch (IloException e)
		{
			e.printStackTrace();
		}
		//TODO else Exception
		//return null;
	}

	@Override
	public void addContrainte(HashMap<Integer, Double> newConstraint, double sc, inequalitySign iq)
	{
		try {
			this.probleme.addContrainte(newConstraint, sc, iq);
			IloLinearNumExpr expr = modele.linearNumExpr();
			
			//Construction de la contrainte
			Iterator<Map.Entry<Integer, Double>> it = newConstraint.entrySet().iterator();
		    while (it.hasNext()) {
		        Map.Entry<Integer, Double> pair = (Map.Entry<Integer, Double>)it.next();
		        expr.addTerm(pair.getValue(), x[pair.getKey()]);
		    }
				
			//Ajout de la contrainte
			if(iq == inequalitySign.Eq)
				modele.addEq(expr, sc);
			else if (iq == inequalitySign.GreatEq)
				modele.addGe(expr, sc);
			else if (iq == inequalitySign.LowEq)
				modele.addLe(expr, sc);
		}
		catch(IloException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void setMinimize(boolean value) 
	{
		try
		{
			if (value) {
				modele.addMinimize(this.fctObj);
			}
			else {
				modele.addMaximize(this.fctObj);
			}
		}
		catch (IloException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void getMinimize()
	{
		
	}
}
