import Controller.Parser;
import CplexPack.VDCSolveur;
import Model.Modele;

public class Stochastique {
	public static void main(String[] args) {
		
		Parser p = new Parser();
		Modele m = p.loadData("p654.xml");
		Modele m2 = p.loadData("a280.tsp");
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
		
		VDCSolveur solv = new VDCSolveur();
		solv.initModele(m);
		solv.optimize();
		
	}
}