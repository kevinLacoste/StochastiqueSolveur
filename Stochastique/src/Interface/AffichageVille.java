/**
 * 
 */
package Interface;

import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

/**
 * @author Thibaut
 *
 */
public class AffichageVille extends JPanel {

	/**
	 * @param args
	 */	
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		int x1 = this.getWidth()/2;
		int y1 = this.getHeight()/2;
		for(int i = 0; i < 20; i++) {
			g2d.fillOval((int)(Math.random() * 400), (int)(Math.random() * 250), 10, 10);
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	}

}
