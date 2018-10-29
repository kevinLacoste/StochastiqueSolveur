package Model;

import java.util.ArrayList;
import java.util.HashMap;

public class Modele {
	public Modele(int nbVilles, ArrayList<HashMap<Integer, Double>> coutsArcs, ArrayList<HashMap<Integer, Double>> variances)
	{
		this.nbVilles = nbVilles;
		this.coutsArcs = new ArrayList<HashMap<Integer, Double>>();
		this.variances = new ArrayList<HashMap<Integer, Double>>();
		
		if(coutsArcs != null)
		{
			for(HashMap<Integer, Double> hM : coutsArcs)
			{
				this.coutsArcs.add(new HashMap<Integer, Double>(hM));
			}
		}
		
		if(variances != null)
		{
			for(HashMap<Integer, Double> hM : variances)
			{
				this.variances.add(new HashMap<Integer, Double>(hM));
			}
		}
		
		//TODO else exception
	}
	
	public int getNbVilles()
	{
		return this.nbVilles;
	}
	
	public double getCoutArc(int villeDepart, int villeArrivee)
	{
		if(villeDepart >= 0  && villeDepart < nbVilles &&
		   villeArrivee >= 0 && villeArrivee < nbVilles)
		{
			return coutsArcs.get(villeDepart).get(villeArrivee);
		}
		else return 0.f;
	}
	
	public double getVarianceArc(int villeDepart, int villeArrivee)
	{
		if(villeDepart >= 0  && villeDepart < nbVilles &&
		   villeArrivee >= 0 && villeArrivee < nbVilles)
		{
			return variances.get(villeDepart).get(villeArrivee);
		}
		else return 0.f;
	}
	
	private int nbVilles;
	private ArrayList<HashMap<Integer, Double>> coutsArcs;
	private ArrayList<HashMap<Integer, Double>> variances;
}
