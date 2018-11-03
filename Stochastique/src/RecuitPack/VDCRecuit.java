package RecuitPack;

import Model.Modele;

public class VDCRecuit {

	private int[] chemin;
	private int nbV;
	private double meilleurCout;
	private RecuitSimule recuitSimule;
	
	public VDCRecuit(Modele m)
	{
		this.nbV = m.getNbVilles();
		//Par défaut le recuit déterminste, pour le moment
		//this.recuitSimule = new RecuitStochastique(m,nbV,nbV*nbV,100);
		this.recuitSimule = new RecuitDeterministe(m,nbV,nbV*nbV);
	}
	
	public int[] getChemin() 
	{
		return chemin;
	}

	public void updateChemin()
	{
		chemin = recuitSimule.getChemin();
	}

	public void run()
	{
		recuitSimule.initTemperature();
		recuitSimule.optimize();
		updateChemin();
		meilleurCout = recuitSimule.coutTotal();
	}

	public double getMeilleurCout() 
	{
		return meilleurCout;
	}
	
	public double getRecTemperature() {
		return recuitSimule.getTemperature();
	}
	
	
}
