package CplexPack;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

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
		ArrayList<Double> solution;
		ArrayList<ArrayList<Integer>> sousTours;
		HashMap<Integer, Double> newContrainst;
		solv.initPL();
		solv.setMinimize(true);
		
		int it = 0;
		
		do
		{
			solv.optimize();
			
			solution = solv.getSolution();
			sousTours = possedeSousTour(solution);
			
			if(sousTours != null) //Presence de sous tours
			{ 
				    for(ArrayList<Integer> tour : sousTours)
				    {
				    	System.out.println(tour.toString());
						newContrainst = new HashMap<Integer, Double>();
						for(int i=0; i<nbVilles; i++)
						{
							for(int j=0; j<nbVilles; j++)
							{
								if(tour.contains(i) && tour.get(tour.indexOf(i)+1) == j)
									newContrainst.put(i*nbVilles+j, 1.d);
							}
						}
						solv.addContrainte(newContrainst, tour.size()-2, inequalitySign.LowEq);
				    }
				}
			it++;
			System.out.println("Iteration n" + it);
		} 
		while(sousTours != null);
		
		
		
		
		
		int c = (int)Math.sqrt(solution.size());
		
		for(int i=0; i<c;i++)
		{
			for(int j=0; j<c;j++)
			{
				System.out.print((int)(double)solution.get(i*c+j) + " ");
			}
			System.out.print("\n");
		}
		
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
}
