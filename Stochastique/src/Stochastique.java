import java.io.FileNotFoundException;

import Controller.Parser;
import CplexPack.VDCSolveur;
import Exceptions.VDCException;
import Model.Modele;

public class Stochastique {
	public static void main(String[] args) {
		
		Parser p = new Parser();
		Modele m = null, m2 = null;
		try {
			m = p.loadData("p654.xml");
			m2 = p.loadData("a280.tsp");
		}
		catch(FileNotFoundException e) {
			e.printStackTrace();
		}
		
		if(m2 != null)
		{
			System.out.println("Arc ville 0 vers 10 : " + m2.getCoutArc(0, 10));
			System.out.println(m2.getPosition(10).toString());
		}
		System.out.println("OKOK");
		int nbVilles = m.getNbVilles();
		for(int i=0; i<nbVilles; i++)
		{
			for(int j=0; j<nbVilles; j++)
			{
				if(i!=j)
				{
					if(Math.abs(m.getCoutArc(i, j)) < 0.1) System.out.println("Ah, une arrete a une taille nulle...");
				}
			}
		}
		
		/*try {
			VDCSolveur solv = new VDCSolveur();
			solv.initModele(m);
			solv.optimizeDeter();
			//solv.optimizeStocha(0);
		}
		catch(VDCException e) {
			e.printStackTrace();
		}*/
		
	}
}