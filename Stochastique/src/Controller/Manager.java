package Controller;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import CplexPack.VDCSolveur;
import Exceptions.IncorrectModeleException;
import Exceptions.NoSolutionException;
import Exceptions.NotInitalizedException;
import Model.Modele;
import RecuitPack.VDCRecuit;
import view.Interface;

public class Manager {
	private Parser parser;
	private Modele modeleLoaded;
	private VDCSolveur vdcSolv;
	private VDCRecuit vdcRecuit;
	
	public Manager() {
		this.parser = new Parser();
		this.vdcSolv = new VDCSolveur();
		this.vdcRecuit = new VDCRecuit();
		vdcRecuit.setManager(this);
		this.modeleLoaded = null;
	}
	
	public void loadModele(String filepath) throws IncorrectModeleException{
		try {
			System.out.println("Loading model...");
			modeleLoaded = parser.loadData(filepath);
			if(modeleLoaded == null) {
				throw new IncorrectModeleException("Le fichier ouvert n'est pas correct");
			}
			else {
				Interface.loadCityModel(modeleLoaded.getPositionsVilles());
				vdcSolv.initModele(modeleLoaded);
				vdcRecuit.initModele(modeleLoaded);
				Interface.setRecuitDeterCoutFinal(-1.d);
				Interface.setRecuitStochaCoutFinal(-1.d);
				Interface.setRecuitDeterCurrent(-1.d);
				Interface.setRecuitStochaCurrent(-1.d);
				Interface.setRecuitDeterTimeElapsed(-1.d);
				Interface.setRecuitStochaTimeElapsed(-1.d);
				Interface.setRecuitDeterTemp(-1.d);
				Interface.setRecuitStochaTemp(-1.d);
				Interface.setSolvDeterOptimumCost(-1.d);
				Interface.setSolvStochaOptimumCost(-1.d);
				Interface.setSolvDeterTimeElapsed(-1.d);
				Interface.setSolvStochaTimeElapsed(-1.d);
			}
		}
		catch(FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<Integer> optimizeSolvDeter() throws NotInitalizedException, NoSolutionException {
		if(modeleLoaded != null) {
			long start = System.currentTimeMillis();
			ArrayList<Double> solution = vdcSolv.optimizeDeter();
			
			if(solution == null) throw new NoSolutionException("Aucune solution n'a ete trouvee");
			else {
				Interface.setSolvDeterOptimumCost(vdcSolv.getOptimalDeter());
				Interface.setSolvDeterTimeElapsed((double)(System.currentTimeMillis() - start)/1000.d);
				return createChemin(solution);
			}
		}
		else throw new NotInitalizedException("Aucun modele n'a ete initialise");
	}
	
	public ArrayList<Integer> optimizeSolvStocha(double alpha) throws NotInitalizedException, NoSolutionException { //Renvoyer quoi ?
		if(modeleLoaded != null) {
			long start = System.currentTimeMillis();
			ArrayList<Double> solution = vdcSolv.optimizeStocha(alpha);
			
			if(solution == null) throw new NoSolutionException("Aucune solution n'a ete trouvee");
			else  {
				Interface.setSolvStochaOptimumCost(vdcSolv.getOptimalStocha());
				Interface.setSolvStochaTimeElapsed((double)(System.currentTimeMillis() - start)/1000.d);
				return createChemin(solution);
			}
		}
		else throw new NotInitalizedException("Aucun modele n'a ete initialise");
	}
	
	public ArrayList<Integer> optimizeRecuitDeter() throws NotInitalizedException {
		if(modeleLoaded != null) {
			long start = System.currentTimeMillis();
			vdcRecuit.run(false);
			Interface.setRecuitDeterTimeElapsed((double)(System.currentTimeMillis() - start)/1000.d);
			Interface.setRecuitDeterCoutFinal(vdcRecuit.getMeilleurCout());
			int chemin[] = vdcRecuit.getChemin();
			ArrayList<Integer> toReturn = new ArrayList<Integer>();
			for(int ville : chemin) {
				toReturn.add(ville);
			}
			
			return toReturn;
			//return donnee
		}
		else throw new NotInitalizedException("Aucun modele n'a ete initialise");
	}
	
	public ArrayList<Integer> optimizeRecuitStocha() throws NotInitalizedException {
		if(modeleLoaded != null) {
			long start = System.currentTimeMillis();
			vdcRecuit.run(true);
			Interface.setRecuitStochaTimeElapsed((double)(System.currentTimeMillis() - start)/1000.d);
			Interface.setRecuitStochaCoutFinal(vdcRecuit.getMeilleurCout());
			int chemin[] = vdcRecuit.getChemin();
			ArrayList<Integer> toReturn = new ArrayList<Integer>();
			for(int ville : chemin) {
				toReturn.add(ville);
			}
			
			return toReturn;
			//return donnee
		}
		else throw new NotInitalizedException("Aucun modele n'a ete initialise");
	}
	
	private ArrayList<Integer> createChemin(ArrayList<Double> solution) {
		ArrayList<Integer> toReturn = new ArrayList<Integer>();
		
		int nbVilles = modeleLoaded.getNbVilles();
		int actualCity = 0;
		toReturn.add(0);
		do 
		{
			for(int i=0; i<nbVilles; i++) {
				if(Math.abs(solution.get(actualCity*nbVilles + i)-1) < 0.01d) {
					actualCity = i;
					toReturn.add(i);
					break;
				}
			}
		} while(actualCity != 0);
		
		System.out.println(toReturn.toString());
		return toReturn;
	}
	
	public void setRecuitDeterTemp (double temperature) {
		Interface.setRecuitDeterTemp(temperature);
	}
	
	public void setRecuitStochaTemp (double temperature) {
		Interface.setRecuitStochaTemp(temperature);
	}
	
	public void setRecuitDeterCurrent (double coutActuel) {
		Interface.setRecuitDeterCurrent(coutActuel);
	}
	
	public void setRecuitStochaCurrent (double coutActuel) {
		Interface.setRecuitStochaCurrent(coutActuel);
	}
}
