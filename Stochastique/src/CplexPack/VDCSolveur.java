package CplexPack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import org.apache.commons.math3.distribution.NormalDistribution;

import Exceptions.NotInitalizedException;
import Model.Modele;

public class VDCSolveur {
	
	private Solveur solv;
	private PL problemeStocha;
	private PL problemeDeter;
	private boolean isOptimised;
	private boolean isInit;
	private NormalDistribution normalDist;
	private ArrayList<Double> moyennes;
	private ArrayList<HashMap<Integer, Double>> variances;
	int nbVilles;
	
	public VDCSolveur()
	{
		//Ici, le solveur appele est Cplex, mais si besoin il est possible d'appeler un solveur
		//different implémenté dans un classe heritant de Solveur et respectant sa structure.
		solv = new Cplex();
		normalDist = new NormalDistribution();
		isOptimised = false;
		isInit = false;
	}
	
	public void initModele(Modele m)
	{
		System.out.println("Le nombre de ville : " + m.getNbVilles());
		
		this.nbVilles = m.getNbVilles();
		int vectSolDimension = nbVilles*nbVilles;
		ArrayList<Double> fctObj = new ArrayList<Double>();
		ArrayList<HashMap<Integer, Double>> matContraintes = new ArrayList<HashMap<Integer, Double>>();
		HashMap<Integer, Double> contrainte;
		ArrayList<Double> secondMembre = new ArrayList<Double>();
		ArrayList<inequalitySign> inequalitySigns = new ArrayList<inequalitySign>();
		
		double d;
		
		// **** Etablissement de la fonction objectif du VDC **** //
		for(int i=0; i<nbVilles; i++)
		{
			for(int j=0; j<nbVilles; j++)
			{
				if(i != j) {
					d = m.getCoutArc(i, j);
					if(d != 0 && i != j)
						fctObj.add(d);
					else 
						fctObj.add(0.d);
				}
				else 
					fctObj.add(0.d);	
			}
		}
		
		// **** Etablissement des contraintes du VDC **** //
		//Distances nulles -> variable = 0
		for(int i=0; i<fctObj.size(); i++) {
			d = fctObj.get(i);
			if(d == 0) {
				contrainte = new HashMap<Integer, Double>();
				contrainte.put(i, 1.d);
				matContraintes.add(contrainte);
				secondMembre.add(0.d);
				inequalitySigns.add(inequalitySign.Eq);
			}
		}
		
		//Contraintes d'entree*
		for(int v = 0; v<nbVilles; v++)
		{
			contrainte = new HashMap<Integer, Double>();
			for(int i=0; i<nbVilles;i++)
			{
				for(int j=0; j<nbVilles; j++)
				{
					if(i==v && fctObj.get(i*nbVilles+j) != 0)
						contrainte.put(i*nbVilles+j, 1.d);
				}
			}
			matContraintes.add(contrainte);
			secondMembre.add(1.d);
			inequalitySigns.add(inequalitySign.Eq);
		}
		
		//Contraintes de sortie
		for(int v = 0; v<nbVilles; v++)
		{
			contrainte = new HashMap<Integer, Double>();
			for(int i=0; i<nbVilles;i++)
			{
				for(int j=0; j<nbVilles; j++)
				{
					if(j==v && fctObj.get(i*nbVilles+j) != 0)
						contrainte.put(i*nbVilles+j, 1.d);
				}
			}
			matContraintes.add(contrainte);
			secondMembre.add(1.d);
			inequalitySigns.add(inequalitySign.Eq);
		}
		
		//Creation des donnees necessaires pour le probleme stochastique
		moyennes = new ArrayList<Double>(fctObj);
		variances = m.getVariances();
		
		this.problemeDeter = new PL(vectSolDimension, dataType.bool, matContraintes, secondMembre, inequalitySigns, fctObj);
		this.problemeStocha = new PL(vectSolDimension, dataType.bool, matContraintes, secondMembre, inequalitySigns, fctObj);
		isInit = true;
	}
	
	public void optimizeDeter() throws NotInitalizedException
	{
		optimize(0);
	}
	
	public void optimizeDeter(long nbMillisec) throws NotInitalizedException
	{
		optimize(nbMillisec);
	}
	
	public void optimizeStocha(double alpha) throws NotInitalizedException
	{
		double Z;	// Valeur optimiale obtenue pour le probleme
		double quantileAlpha;
		HashMap<Integer, Double> constraint = new HashMap<Integer, Double>();
		
		System.out.println("Resolution stochastique");
		optimize(0); //Resolution optimale deterministe
		Z = this.problemeDeter.getFctValue() * 1.3d;
		
		//Creation de la nouvelle contrainte (on suppose que les facteurs de covariance sont nuls)
		quantileAlpha = normalDist.inverseCumulativeProbability(alpha);
		for(int i=0; i<nbVilles; i++) {
			for(int j=0; j<nbVilles; j++) {
				if(this.variances.get(i).get(j) != null) {
					constraint.put(i*nbVilles+j, moyennes.get(i*nbVilles + j) + quantileAlpha*variances.get(i).get(j));
				}
			}
		}
		problemeStocha.addContrainte(constraint, Z, inequalitySign.LowEq);
		solv.definePL(problemeStocha);
		solv.initPL();
		solv.setMinimize(true);
		
		ArrayList<Double> solution = new ArrayList<Double>();
		ArrayList<ArrayList<Integer>> sousTours = new ArrayList<ArrayList<Integer>>();
		HashMap<Integer, Double> newContrainst;
		int it = 1;
		
		while(sousTours != null)
		{
			System.out.println("Iteration n" + it);
			solv.optimize();
			
			solution = solv.getSolution();
			if(solution == null) {
				System.out.println("Pas de solution !");
				return;
			}
			sousTours = possedeSousTour(solution);
			
			if(sousTours != null) //Presence de sous tours et timer en lice
			{ 
			    for(ArrayList<Integer> tour : sousTours)
			    {
			    	newContrainst = new HashMap<Integer, Double>();
					for(int i=0; i<nbVilles; i++)
					{
						for(int j=0; j<nbVilles; j++)
						{
							if(tour.contains(i) && tour.contains(j) && i!=j)
								newContrainst.put(i*nbVilles+j, 1.d);
						}
					}
					solv.addContrainte(newContrainst, tour.size()-2, inequalitySign.LowEq);
			    }
			}
			it++;
		}
		
		solution = this.problemeStocha.getSolution();
		int actualCity = 0;
		System.out.print("0->");
		do
		{
			for(int i=0; i<nbVilles;i++)
			{
				if(Math.abs(solution.get(actualCity*nbVilles+i) - 1) < 0.01d) {
					if(i != 0) System.out.print(i + "->");
					else System.out.print(i);
					actualCity = i;
					break;
				}
			}
		} while(actualCity != 0);
		System.out.print("\n");
		
		double solValue = problemeStocha.getFctValue();
		System.out.println("Solution value : " + solValue);
		System.out.println();
	}
	
	private void optimize(long nbMillisec) throws NotInitalizedException
	{
		if(isInit)
		{
			this.solv.definePL(problemeDeter);
			solv.initPL();
			solv.setMinimize(true);
			
			ArrayList<Double> solution = new ArrayList<Double>();
			ArrayList<ArrayList<Integer>> sousTours = new ArrayList<ArrayList<Integer>>();
			HashMap<Integer, Double> newContrainst;
			long end = -1;
			
			int it = 1;
			
			if(nbMillisec > 0)System.out.println("Temps de travail : " + nbMillisec + " millisecondes");
			else System.out.println("Temps de travail : pas de limites");
			
			if(!isOptimised)
			{
				if(nbMillisec > 0) 
					end = System.currentTimeMillis() + nbMillisec;
				
				while(sousTours != null)
				{
					System.out.println("Iteration n" + it);
					solv.optimize();
					
					solution = solv.getSolution();
					sousTours = possedeSousTour(solution);
					
					if(sousTours != null && ((end == -1) || (System.currentTimeMillis() < end))) //Presence de sous tours et timer en lice
					{ 
						for(ArrayList<Integer> tour : sousTours)
					    {
							System.out.println(tour.toString());
							newContrainst = new HashMap<Integer, Double>();
							for(int i=0; i<nbVilles; i++)
							{
								for(int j=0; j<nbVilles; j++)
								{
									if(tour.contains(i) && tour.contains(j) && i!=j)
										newContrainst.put(i*nbVilles+j, 1.d);
								}
							}
							solv.addContrainte(newContrainst, tour.size()-2, inequalitySign.LowEq);
					    }
					}
					it++;
					
					if(end != -1 && System.currentTimeMillis() >= end && sousTours != null)
					{
						breakSousTours(sousTours);
						break;
					}
				} 
				
				if(sousTours == null)isOptimised = true;
			}
			
			solution = this.problemeDeter.getSolution();
			int actualCity = 0;
			System.out.print("0->");
			do
			{
				for(int i=0; i<nbVilles;i++)
				{
					if(Math.abs(solution.get(actualCity*nbVilles+i) - 1) < 0.01d) {
						if(i != 0) System.out.print(i + "->");
						else System.out.print(i);
						actualCity = i;
						break;
					}
				}
			} while(actualCity != 0);
			System.out.print("\n");
			
			double solValue = problemeDeter.getFctValue();
			System.out.println("Solution value : " + solValue);
			System.out.println();
		}
		
		else throw new NotInitalizedException("Le probleme a resoudre n'a pas ete defini");
	}

	public ArrayList<ArrayList<Integer>> possedeSousTour(ArrayList<Double> solution)
	{
		ArrayList<ArrayList<Integer>> sousTours = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> tour = new ArrayList<Integer>();
		ArrayList<Boolean> visited = new ArrayList<Boolean>();
		for(int i=0; i<nbVilles;i++)
			visited.add(false);
		int actualCity = 0;
		visited.set(0, true);
		tour.add(0);
		boolean sousTour = false;
		
		while (visited.contains(false))
		{
			for (int i=0; i < nbVilles; i++)
			{
				if (Math.abs(solution.get(actualCity*nbVilles + i) - 1) < 0.01)
				{
					actualCity = i;
					tour.add(i);
					if (visited.get(i))
					{
						sousTour = true;
						sousTours.add(tour);
						tour = new ArrayList<Integer>();
						actualCity = visited.indexOf(false);
						tour.add(actualCity);
						visited.set(actualCity, true);
					}
					else
						visited.set(i, true);
				}
			}
		}
		if(sousTours.size() != 0) {
			for (int i=0; i < nbVilles; i++)
			{
				if ((int)(double)solution.get(actualCity*nbVilles + i) == 1)
				{
					tour.add(i);
					if (visited.get(i))
					{
						sousTour = true;
						sousTours.add(tour);
					}
				}
			}
		}
		
		if (sousTour) {
			System.out.println("Sous Tour!");		
		}
		else {
			System.out.println("Pas de sous tour!");
		}
		if(sousTour)
			return sousTours;
		else return null;
	}
	
	//Utilise un algorithme glouton, uniquement pour le probleme deterministe
	private void breakSousTours(ArrayList<ArrayList<Integer>> sousTours)
	{
		int toLink;
		int toDiscard;
		int index = 0;
		double bestValue;
		double valueTested;
		LinkedList<Integer> chemin = new LinkedList<Integer>(sousTours.get(0));
		System.out.println("Value : " + chemin.toString());
		ArrayList<Double> solution = this.problemeDeter.getSolution();
		
		toDiscard = chemin.pollLast();
		sousTours.remove(0);
		
		while(sousTours != null)
		{
			toLink = chemin.getLast();
			index = 0;
			if(sousTours.size() > 2)
			{
				bestValue = problemeDeter.getFctObjCoeff(toLink*nbVilles + sousTours.get(0).get(0));
				for(int i=0; i<sousTours.size(); i++) {
					valueTested = problemeDeter.getFctObjCoeff(toLink*nbVilles + sousTours.get(i).get(0));
					if(valueTested < bestValue) {
						index = i;
						bestValue = valueTested;
					}
				}
				
				solution.set(toLink*nbVilles + toDiscard, 0.d);
				solution.set(toLink*nbVilles + sousTours.get(index).get(0),  1.d);
				
				chemin.addAll(sousTours.get(index));
				sousTours.remove(index);
				
				toDiscard = chemin.pollLast();
			}
			
			else
			{
				int scIndex = 1;
				
				bestValue = problemeDeter.getFctObjCoeff(toLink*nbVilles + sousTours.get(0).get(0)) + 
							problemeDeter.getFctObjCoeff(sousTours.get(0).get(sousTours.get(0).size()-2) * nbVilles + sousTours.get(1).get(0)) + 
							problemeDeter.getFctObjCoeff(sousTours.get(1).get(sousTours.get(1).size()-2) * nbVilles);
				
				valueTested= problemeDeter.getFctObjCoeff(toLink*nbVilles + sousTours.get(1).get(0)) + 
							 problemeDeter.getFctObjCoeff(sousTours.get(1).get(sousTours.get(1).size()-2) * nbVilles + sousTours.get(0).get(0)) + 
							 problemeDeter.getFctObjCoeff(sousTours.get(0).get(sousTours.get(0).size()-2) * nbVilles);
				
				if(valueTested < bestValue) {
					scIndex = 0;
					index = 1;
				}
					
				// else index = 0 deja fait
				
				solution.set(toLink*nbVilles + toDiscard, 0.d);
				solution.set(toLink*nbVilles + sousTours.get(index).get(0), 1.d);
				solution.set(sousTours.get(index).get(sousTours.get(index).size()-2) * nbVilles + sousTours.get(index).get(sousTours.get(index).size()-1), 0.d);
				solution.set(sousTours.get(index).get(sousTours.get(index).size()-2) * nbVilles + sousTours.get(scIndex).get(0), 1.d);
				solution.set(sousTours.get(scIndex).get(sousTours.get(scIndex).size()-2) * nbVilles + sousTours.get(scIndex).get(sousTours.get(scIndex).size()-1), 0.d);
				solution.set(sousTours.get(scIndex).get(sousTours.get(scIndex).size()-2) * nbVilles, 1.d);
				
				sousTours = null;
			}
		}
		
		this.problemeDeter.setSolution(solution);
	}
}
