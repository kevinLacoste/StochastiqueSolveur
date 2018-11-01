package CplexPack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PL {
	private int vectSolDimension;
	private ArrayList<Double> vectSolution;
	private dataType dt;
	private ArrayList<HashMap<Integer, Double>> matContraintes;
	private ArrayList<inequalitySign> inequalitySigns;
	private ArrayList<Double> secondMembre;
	private int nbContraintes;
	private ArrayList<Double> fctObj;
	
	public PL(int vectSolDimension, dataType dt, ArrayList<HashMap<Integer, Double>> matContraintes, 
			ArrayList<Double> secondMembre, ArrayList<inequalitySign> inequalitySigns, ArrayList<Double> fctObj)
	{
		//Verification de la validite des donnees
		boolean isValid = true;
		if(matContraintes != null)
		for(HashMap<Integer, Double> constraintLine : matContraintes)
		{
			Iterator<Map.Entry<Integer, Double>> it = constraintLine.entrySet().iterator();
		    while (it.hasNext()) {
		        Map.Entry<Integer, Double> pair = (Map.Entry<Integer, Double>)it.next();
		        if((Integer)pair.getKey() < 0 || (Integer)pair.getKey() >= vectSolDimension) 
		        {
		        	isValid = false;
		        	break;
		        }
		    }
		}
		if(secondMembre != null) {
			if(secondMembre.size() != matContraintes.size()) isValid = false;
		}
		else if(matContraintes != null) isValid = false;
		if(inequalitySigns.size() != secondMembre.size()) isValid = false;
		
		if(!isValid)
		{
			//TODO Exception
			return;
		}
		// Fin des verifications
		
		//Dimension du vecteur de solution
		this.vectSolDimension = vectSolDimension;
		
		//Init. du vecteur de solution
		this.vectSolution = new ArrayList<Double>();
		for(int i=0; i<vectSolDimension; i++)
		{
			vectSolution.add(0.d);
		}
		
		//Type de donnees traitees (bool, int, double)
		this.dt = dt;
		
		//Init de la matrice des contraintes
		this.matContraintes = new ArrayList<HashMap<Integer, Double>>();
		for(HashMap<Integer, Double> constraintLine : matContraintes)
		{
			this.matContraintes.add(new HashMap<Integer, Double>(constraintLine));
		}
		
		//Init du nombre de contraintes
		this.nbContraintes = matContraintes.size();
		
		//Init du vecteur second membre
		this.secondMembre = new ArrayList<Double>(secondMembre);
		
		//Init des signes des inequations (==, <=, >=)
		this.inequalitySigns = new ArrayList<inequalitySign>(inequalitySigns);
		
		//Init de la fonction objectif
		this.fctObj = new ArrayList<Double>(fctObj);
		
	}
	
	public void addContrainte(HashMap<Integer, Double> newConstraint, double sc, inequalitySign iq)
	{
		Iterator<Map.Entry<Integer, Double>> it = newConstraint.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<Integer, Double> pair = (Map.Entry<Integer, Double>)it.next();
	        if((Integer)pair.getKey() < 0 || (Integer)pair.getKey() >= vectSolDimension) 
	        {
	        	//TODO Exception
	        	return;
	        }
	    }
		
		HashMap<Integer, Double> contrainteToAdd = new HashMap<Integer, Double>(newConstraint);
		this.matContraintes.add(contrainteToAdd);
		this.secondMembre.add(sc);
		this.inequalitySigns.add(iq);
		nbContraintes++;
		
		
		//else EXCEPTION
	}
	
	public void removeContrainte(int i)
	{
		if(i>=0 && i<this.nbContraintes)
		{
			this.matContraintes.remove(i);
			this.secondMembre.remove(i);
			this.inequalitySigns.remove(i);
			nbContraintes--;
		}
		
		//else EXCEPTION
	}
	
	public int getVectSolDimension()
	{
		return this.vectSolDimension;
	}
	
	public int getNbContraintes()
	{
		return this.nbContraintes;
	}
	
	public dataType getDataType()
	{
		return this.dt;
	}
	
	public double getFctObjCoeff(int i)
	{
		if(i >= 0 && this.fctObj.size() > i)
			return this.fctObj.get(i);
		else return 0; //TODO Exception
	}
	
	public HashMap<Integer, Double> getContrainte(int i)
	{
		if(i >= 0 && this.nbContraintes > i)
			return new HashMap<Integer, Double>(this.matContraintes.get(i));
		else return null; //TODO Exception
	}
	
	public double getSCValue(int i)
	{
		if(i >= 0 && this.nbContraintes > i)
			return this.secondMembre.get(i);
		else return 0; //TODO Exception
	}
	
	public inequalitySign getInequalityValue(int i)
	{
		if(i >= 0 && this.nbContraintes > i)
			return this.inequalitySigns.get(i);
		else return null; //TODO Exception
	}
	
	public void setSolution(ArrayList<Double> solution)
	{
		this.vectSolution = new ArrayList<Double>(solution); //TODO A Ameliorer
	}
	
	public ArrayList<Double> getSolution()
	{
		return new ArrayList<Double>(vectSolution);
	}
	
	public double calcFctSolution()
	{
		double d = 0;
		for(int i=0; i<vectSolDimension; i++)
		{
			d += vectSolution.get(i) * fctObj.get(i);
		}
		return d;
	}
}
