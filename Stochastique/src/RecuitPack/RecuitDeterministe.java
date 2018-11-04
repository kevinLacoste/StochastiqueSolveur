package RecuitPack;

import java.util.ArrayList;

import org.apache.commons.math3.distribution.NormalDistribution;

import Model.Modele;

public class RecuitDeterministe extends RecuitSimule {

	private Modele modele;
	private int[] chemin;
	private int nbV;
	private int indice1;
	private int indice2;
	
	RecuitDeterministe(Modele m,int nbV,int nbIt)
	{
		modele = m;
		coeffDecroissance = 0.9;//Decroissance par palier
		this.nbV = nbV;
		nbIterations = nbIt;
		//Choix d'un chemin aléatoire
		this.chemin = new int[nbV+1];
		//initCheminOrdre();
		initCheminDist();
	}
	
	private void initCheminOrdre()//Ordre croissant
	{
		for (int i = 0;i < nbV;i++)
		{
			chemin[i] = i;
		}
		chemin[nbV] = 0;
	}
	
	private void initCheminDist()//Dist la plus courte à chaque fois
	{
		chemin[0] = 0;
		int nbElemChemin = 1;
		int idVilleActuelle = 0;
		ArrayList<Integer> visited = new ArrayList<Integer>();
		visited.add(0);
		double min;
		int idMin = -1;
		
		while (nbElemChemin < nbV)
		{
			min = Double.MAX_VALUE;
			for (int i = 0;i < nbV;i++)
			{
				if (!visited.contains(i))
				{
					if (modele.getCoutArc(idVilleActuelle,i) < min)
					{
						min = modele.getCoutArc(idVilleActuelle,i);
						idMin = i;
					}
				}
			}
			chemin[nbElemChemin] = idMin;
			visited.add(idMin);
			idVilleActuelle = idMin;
			nbElemChemin++;
		}
		chemin[nbElemChemin] = 0;
	}

	@Override
	public void selectMvt()
	{
		//Inverse 2 villes dans le chemin, sauf le début et la fin du chemin
		int tmp;
		indice1 = randNumberRange(1,nbV-1);
		indice2 = randNumberRange(1,nbV-1);
		if (indice1 == indice2)
		{
			if (indice1 == 1)
			{
				indice1++;
			}
			else
			{
				indice1--;
			}
		}
		tmp = chemin[indice1];
		chemin[indice1] = chemin[indice2];
		chemin[indice2] = tmp;
	}

	@Override
	public void refuseMvt() 
	{
		int tmp;
		tmp = chemin[indice1];
		chemin[indice1] = chemin[indice2];
		chemin[indice2] = tmp;
	}

	@Override
	public void initTemperature(boolean alea) 
	{
		int nbAcceptations;
		boolean mvtPossible;
		double coutActuel;
		double delta;
		double proba;
		
		if (alea)
		{
			appliquerVariance();
		}
		
		temperature = nbV;
		double tauxAcceptable = 0.80;
		tauxAcceptation = 0;
		meilleurCout = coutTotal();
		coutActuel = meilleurCout;
		
		while (tauxAcceptation < tauxAcceptable)
		{
			nbAcceptations = 0;
			for (int i = 0;i < nbIterations;i++)
			{
				coutActuel = coutTotal();
				do
				{
					selectMvt();
					mvtPossible = mvtPossible();
					if (!mvtPossible)
					{
						refuseMvt();
					}
				}
				while (!mvtPossible);
				
				delta = coutApresVoisinage(coutActuel) - coutActuel;
				if (delta < 0)
				{
					nbAcceptations++;
					if ((delta + coutActuel) < meilleurCout /*CoutTotal < meilleurCout*/)
					{
						meilleurCout = (delta + coutActuel);
					}
				}
				else 
				{
					proba = randProba();
					if (proba <= Math.exp(-delta/temperature))
					{
						nbAcceptations++;
					}
					else 
					{
						refuseMvt();
					}
				}
				
			}//for (int i = 0;i < nbIterations;i++)
			tauxAcceptation = (double)nbAcceptations/(double)nbIterations;
			System.out.println("Taux acceptation : " + tauxAcceptation);
			if (tauxAcceptation < tauxAcceptable)
			{
				temperature *= 2;
			}
		}//while (tauxAcceptation < tauxAcceptable)
		System.out.println("Temperature initialisee a " + temperature);
	}

	@Override
	public double optimize()
	{
		int nbAcceptations;
		boolean mvtPossible;
		double coutActuel;
		double delta;
		double proba;
		double coutApres;

		tauxAcceptationMin = 0.001;//A changer peut etre
		tauxAcceptation = 1;
		meilleurCout = coutTotal();
		
		coutActuel = meilleurCout;
		
		while (tauxAcceptation > tauxAcceptationMin)
		{
			nbAcceptations = 0;
			for (int i = 0;i < nbIterations;i++)
			{
				coutActuel = coutTotal();
				do
				{
					selectMvt();
					mvtPossible = mvtPossible();
					if (!mvtPossible)
					{
						refuseMvt();
					}
				}
				while (!mvtPossible);
				
				coutApres = coutApresVoisinage(coutActuel);
				delta = coutApres - coutActuel;
				if (delta < 0)
				{
					nbAcceptations++;
					if (coutApres < meilleurCout /*CoutTotal < meilleurCout*/)
					{
						meilleurCout = coutApres;
					}
				}
				else 
				{
					proba = randProba();
					if (proba <= Math.exp(-delta/temperature))
					{
						nbAcceptations++;
					}
					else 
					{
						refuseMvt();
					}
				}
				
			}//for (int i = 0;i < nbIterations;i++)
			
			tauxAcceptation = (double)nbAcceptations/(double)nbIterations;
			System.out.println("Taux acceptation : " + tauxAcceptation + ", solution = " + meilleurCout);
			temperature *= coeffDecroissance;
			
		}//while (tauxAcceptation < tauxAcceptable)
		System.out.println("Temperature finale: " + temperature);
		System.out.println("Longeur totale: " + meilleurCout);
		return coutTotal();
	}

	@Override
	public double coutTotal()
	{
		double coutTotal = 0;
		for (int i = 0;i < nbV;i++)
		{
			coutTotal += modele.getCoutArc(chemin[i],chemin[i+1]);
		}
		return coutTotal;
	}

	public double coutApresVoisinage(double coutAct)//Evite de recalculer le cout total, plus rapide
	{
		double coutsToRemove = 0;
		double coutsToAdd = 0;
		double res = coutAct;
		
		coutsToAdd = modele.getCoutArc(chemin[indice1-1],chemin[indice1]) +
		modele.getCoutArc(chemin[indice1],chemin[indice1+1]) +
		modele.getCoutArc(chemin[indice2-1],chemin[indice2]) +
		modele.getCoutArc(chemin[indice2],chemin[indice2+1]);

		coutsToRemove = modele.getCoutArc(chemin[indice1-1],chemin[indice2]) +
		modele.getCoutArc(chemin[indice2],chemin[indice1+1]) +
		modele.getCoutArc(chemin[indice2-1],chemin[indice1]) +
		modele.getCoutArc(chemin[indice1],chemin[indice2+1]);

		return (res + coutsToAdd - coutsToRemove);
	}

	@Override
	public boolean mvtPossible() 
	{
		if (modele.getCoutArc(chemin[indice1-1],chemin[indice1]) == 0.f)
		{
			return false;
		}
		if (modele.getCoutArc(chemin[indice1],chemin[indice1+1]) == 0.f)
		{
			return false;
		}
		if (modele.getCoutArc(chemin[indice2-1],chemin[indice2]) == 0.f)
		{
			return false;
		}
		if (modele.getCoutArc(chemin[indice2],chemin[indice2+1]) == 0.f)
		{
			return false;
		}
		return true;
	}


	@Override
	public int[] getChemin() {
		return chemin;
	}
	
	private void appliquerVariance()
	{
		for (int i = 0;i < this.nbV;i++)
		{
			System.out.println("Avant : " + this.modele.getCoutArc(i,0));
			for (int j = 0;j < this.nbV;j++)
			{
				if (i != j && this.modele.getVarianceArc(i,j) > 0)
				{
					NormalDistribution n = new NormalDistribution(this.modele.getCoutArc(i,j),Math.sqrt(this.modele.getVarianceArc(i,j)));
					this.modele.setCoutArc(i,j,n.sample());
				}
			}
			System.out.println("Apres : " + this.modele.getCoutArc(i,0));

		}
	}

}
