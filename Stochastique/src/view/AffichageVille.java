/**
 * 
 */
package view;

import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import Model.Modele;

/**
 * @author Thibaut
 *
 */
public class AffichageVille extends JPanel {

	/**
	 * @param args
	 */	
	Modele modele;
	
	public AffichageVille(Modele m) {
		modele = m;
	}
	
	public void affichage(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		for(int i = 0; i < modele.getNbVilles(); i++) {
			g2d.fillOval((int)modele.getPosition(i).getX(), (int)modele.getPosition(i).getY(), 10, 10);
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	}

}
