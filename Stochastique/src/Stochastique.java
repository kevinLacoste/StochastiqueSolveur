import Controller.Parser;
import Model.Modele;

public class Stochastique {
	public static void main(String[] args) {
		Parser p = new Parser();
		Modele m = p.loadXML("a280.xml");
		System.out.println("Arc ville 0 vers 200 : " + m.getCoutArc(0, 200));
	}
}
