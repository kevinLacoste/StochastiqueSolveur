package RecuitPack;
import java.util.Random;

public abstract class RecuitSimule {

	protected double temperature;
	protected double meilleurCout;
	protected int nbIterations;
	protected double coeffDecroissance;
	protected double tauxAcceptation;
	protected double tauxAcceptationMin;

	abstract public double optimize();
	abstract public boolean mvtPossible();
	abstract public void selectMvt();
	abstract public void refuseMvt();
	abstract public void initTemperature();
	abstract public double coutTotal();
	
	abstract public int[] getChemin();

	protected double randProba()
	{
		Random r = new Random();
		return r.nextDouble();
	}
	
	protected int randNumberRange(int min, int max)
	{
		Random r = new Random();
		return r.ints(min, (max + 1)).findFirst().getAsInt();
	}
	public double getTemperature() {
		return temperature;
	}
	
}
