package RecuitPack;

import Model.Modele;

public class RecuitStochastique extends RecuitDeterministe
{

	private double Z;
	private boolean overflow;
	
	RecuitStochastique(Modele m, int nbV, int nbIt,double Z) 
	{
		super(m, nbV, nbIt);
	}
	
	@Override
	public double optimize(boolean stocha)
	{
		super.optimize(stocha);
		if (meilleurCout > Z)
		{
			overflow = true;
		}
		else 
		{
			overflow = false;
		}
		return meilleurCout;
	}

}
