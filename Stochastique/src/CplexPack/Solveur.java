package CplexPack;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class Solveur 
{
	protected PL probleme;
	protected boolean isSolved;
	protected boolean isInit;
	
	public Solveur()
	{
		isSolved = false;
		isInit = false;
	}
	
	public void definePL(int vectSolDimension, dataType dt, ArrayList<HashMap<Integer, Double>> matContraintes, 
						 ArrayList<Double> secondMembre, ArrayList<inequalitySign> inequalitySigns, ArrayList<Double> fctObj)
	{
		this.probleme = new PL(vectSolDimension, dt, matContraintes, secondMembre, inequalitySigns, fctObj);
	}
	
	public void definePL(PL probleme)
	{
		this.probleme = probleme;
	}
	
	abstract public void initPL();
	abstract public void optimize();
	abstract public void addContrainte(HashMap<Integer, Double> newConstraint, double sc, inequalitySign iq);
	abstract public void setMinimize(boolean value);
	abstract public void getMinimize();
	
	public ArrayList<Double> getSolution()
	{
		if(isSolved) {
			return probleme.getSolution();
		}
		else return null;
	}
	
	public double getFctValue()
	{
		if(isSolved) {
			return probleme.calcFctSolution();
		}
		else return 0.d; //TODO Exception
	}
}
