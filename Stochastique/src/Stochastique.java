import Controller.Parser;
import Model.Modele;

public class Stochastique {
	public static void main(String[] args) {
		Parser p = new Parser();
		Modele m = p.loadData("a280.xml");
		Modele m2 = p.loadData("a280.tsp");
		if(m2 != null)
		{
			System.out.println("Arc ville 0 vers 10 : " + m2.getCoutArc(0, 10));
			System.out.println(m2.getPosition(10).toString());
		}
	}
}
