package Model;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;

public class Modele {
	public Modele(int nbVilles, ArrayList<HashMap<Integer, Double>> coutsArcs, ArrayList<Point2D> positions)
	{
		this.nbVilles = nbVilles;
		this.coutsArcs = new ArrayList<HashMap<Integer, Double>>();
		this.positions = new ArrayList<Point2D>();
		this.variances = new ArrayList<HashMap<Integer, Double>>();
		HashMap<Integer, Double> actualVariance;
		
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
		
		if(positions != null)
		{
			for(Point2D p : positions)
			{
				this.positions.add(p);
			}
		}
		
		for(int i=0; i<coutsArcs.size(); i++) {
			HashMap<Integer, Double> tab = coutsArcs.get(i);
			variances.add(new HashMap<Integer, Double>());
			actualVariance = variances.get(variances.size()-1);
			
			for(int j=0; j<nbVilles; j++) {
				if(tab.containsKey(j)) {
					actualVariance.put(j, tab.get(j)*factCov);
				}
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
			Double costToReturn = coutsArcs.get(villeDepart).get(villeArrivee);
			return (costToReturn != null ? costToReturn : 0.f);
		}
		else return 0.f;
	}
	
	public double getVarianceArc(int villeDepart, int villeArrivee)
	{
		if(villeDepart >= 0  && villeDepart < nbVilles &&
		   villeArrivee >= 0 && villeArrivee < nbVilles)
		{
			Double costToReturn = variances.get(villeDepart).get(villeArrivee);
			return (costToReturn != null ? costToReturn : 0.f);
		}
		else return 0.f;
	}
	
	public Point2D getPosition(int ville)
	{
		if(ville >= 0  && ville < nbVilles)
		{
			return positions.get(ville);
		}
		else return null;
	}
	
	public ArrayList<HashMap<Integer, Double>> getCoutsArcs() {
		ArrayList<HashMap<Integer, Double>> toReturn = new ArrayList<HashMap<Integer, Double>>();
		for(HashMap<Integer, Double> cout : this.coutsArcs) {
			toReturn.add(new HashMap<Integer, Double>(cout));
		}
		return toReturn;
	}
	
	public ArrayList<HashMap<Integer, Double>> getVariances() {
		ArrayList<HashMap<Integer, Double>> toReturn = new ArrayList<HashMap<Integer, Double>>();
		for(HashMap<Integer, Double> variance : this.variances) {
			toReturn.add(new HashMap<Integer, Double>(variance));
		}
		return toReturn;
	}
	
	private int nbVilles;
	private ArrayList<HashMap<Integer, Double>> coutsArcs;
	private ArrayList<HashMap<Integer, Double>> variances;
	private ArrayList<Point2D> positions;
	private double factCov = 0.2d;
}
