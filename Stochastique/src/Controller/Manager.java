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
				Interface.setSolvOptimumCost(vdcSolv.getOptimalDeter());
				Interface.setSolvTimeElapsed((double)(System.currentTimeMillis() - start)/1000.d);
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
				Interface.setSolvOptimumCost(vdcSolv.getOptimalStocha());
				Interface.setSolvTimeElapsed((double)(System.currentTimeMillis() - start)/1000.d);
				return createChemin(solution);
			}
		}
		else throw new NotInitalizedException("Aucun modele n'a ete initialise");
	}
	
	public ArrayList<Integer> optimizeRecuitDeter() throws NotInitalizedException { //Renvoyer quoi ?
		if(modeleLoaded != null) {
			long start = System.currentTimeMillis();
			vdcRecuit.run();
			Interface.setRecuitTimeElapsed((double)(System.currentTimeMillis() - start)/1000.d);
			Interface.setRecuitCoutFinal(vdcRecuit.getMeilleurCout());
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
}
