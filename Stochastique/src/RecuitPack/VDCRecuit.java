package RecuitPack;

import Model.Modele;

public class VDCRecuit {

	private int[] chemin;
	private int nbV;
	private double meilleurCout;
	private RecuitSimule recuitSimule;
	private boolean isInit;
	
	public VDCRecuit()
	{
		isInit = false;
		meilleurCout = -1;
	}
	
	public void initModele(Modele m)
	{
		this.nbV = m.getNbVilles();
		//Par défaut le recuit déterminste, pour le moment
		//this.recuitSimule = new RecuitStochastique(m,nbV,nbV*nbV,100);
		this.recuitSimule = new RecuitDeterministe(m,nbV,nbV*nbV);
		isInit = true;
	}
	
	public int[] getChemin() 
	{
		if(isInit)
			return chemin;
		else return null;
	}

	public void updateChemin()
	{
		if(isInit)
			chemin = recuitSimule.getChemin();
	}

	public void run(boolean stocha)
	{
		if(isInit)
		{
			recuitSimule.initTemperature(stocha);
			recuitSimule.optimize();
			updateChemin();
			meilleurCout = recuitSimule.coutTotal();
		}
	}

	public double getMeilleurCout() 
	{
		return meilleurCout;
	}
	
	public double getRecTemperature() {
		if(isInit)
			return recuitSimule.getTemperature();
		else return -1;
	}
	
	
}
