package CplexPack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import Model.Modele;

public class VDCSolveur {
	
	private Solveur solv;
	private PL probleme;
	int nbVilles;
	
	public VDCSolveur()
	{
		//Ici, le solveur appele est Cplex, mais si besoin il est possible d'appeler un solveur
		//different implémenté dans un classe heritant de Solveur et respectant sa structure.
		solv = new Cplex();
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
		
		this.probleme = new PL(vectSolDimension, dataType.bool, matContraintes, secondMembre, inequalitySigns, fctObj);
		this.solv.definePL(probleme);
		//this.solv.setMinimize(true);
		for(int i=0; i<nbVilles;i++)
		{
			for(int j=0; j<nbVilles;j++)
			{
				System.out.print((int)(double)fctObj.get(i*nbVilles+j) + " ");
			}
			System.out.print("\n");
		}
	}
	
	public void optimize()
	{
		optimize(0);
	}
	
	public void optimize(int nbSec)
	{
		ArrayList<Double> solution = new ArrayList<Double>();
		ArrayList<ArrayList<Integer>> sousTours = new ArrayList<ArrayList<Integer>>();
		HashMap<Integer, Double> newContrainst;
		long end = -1;
		
		solv.initPL();
		solv.setMinimize(true);
		
		int it = 0;
		
		if(nbSec > 0) 
			end = System.currentTimeMillis() + nbSec*1000;
		
		while(sousTours != null)
		{
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
				solution = breakSousTours(sousTours);
				probleme.setSolution(solution);
				break;
			}
			System.out.println("Iteration n" + it);
		} 
		
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
		
		double solValue = solv.getFctValue();
		System.out.println("Solution value : " + solValue);
		
		/*solv.getCplex().setMinimize(true);
		solv.getCplex().optimize();
		boolean st = solv.possedeSousTour();
		int iteration = 2;
		while (st == true)
		{
			System.out.println("Iteration : " + iteration);
			solv.getCplex().addContrainte(solv.getVisited());
			solv.getCplex().optimize();
			st = solv.possedeSousTour();
			iteration++;
		}*/
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
	
	//Utilise un algorithme glouton
	private ArrayList<Double> breakSousTours(ArrayList<ArrayList<Integer>> sousTours)
	{
		int toLink;
		int toDiscard;
		int index = 0;
		double bestValue;
		double valueTested;
		LinkedList<Integer> chemin = new LinkedList<Integer>(sousTours.get(0));
		System.out.println("Value : " + chemin.toString());
		ArrayList<Double> solution = this.probleme.getSolution();
		
		toDiscard = chemin.pollLast();
		sousTours.remove(0);
		
		while(sousTours != null)
		{
			toLink = chemin.getLast();
			index = 0;
			if(sousTours.size() > 2)
			{
				System.out.println("Start : " + chemin.toString());
				
				bestValue = probleme.getFctObjCoeff(toLink*nbVilles + sousTours.get(0).get(0));
				for(int i=0; i<sousTours.size(); i++) {
					valueTested = probleme.getFctObjCoeff(toLink*nbVilles + sousTours.get(i).get(0));
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
				
				System.out.println("End : " + chemin.toString());
			}
			
			else
			{
				int scIndex = 1;
				
				bestValue = probleme.getFctObjCoeff(toLink*nbVilles + sousTours.get(0).get(0)) + 
							probleme.getFctObjCoeff(sousTours.get(0).get(sousTours.get(0).size()-2) * nbVilles + sousTours.get(1).get(0)) + 
							probleme.getFctObjCoeff(sousTours.get(1).get(sousTours.get(1).size()-2) * nbVilles);
				
				valueTested= probleme.getFctObjCoeff(toLink*nbVilles + sousTours.get(1).get(0)) + 
							 probleme.getFctObjCoeff(sousTours.get(1).get(sousTours.get(1).size()-2) * nbVilles + sousTours.get(0).get(0)) + 
							 probleme.getFctObjCoeff(sousTours.get(0).get(sousTours.get(0).size()-2) * nbVilles);
				
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
		
		return solution;
	}
}
