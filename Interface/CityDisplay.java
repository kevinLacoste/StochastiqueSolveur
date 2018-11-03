package Interface;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public class CityDisplay extends JPanel implements ActionListener {
	
	private ArrayList<Point2D> cityPoints;
	private ArrayList<Boolean> cityArcs;
	private Timer timer;
	private float zoom = .5f;
	
	public CityDisplay() {
		cityPoints = new ArrayList<Point2D>();
		cityArcs = new ArrayList<Boolean>();
		MouseMotionListener ac = new MouseMotionListener() {
			
			@Override
			public void mouseMoved(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseDragged(MouseEvent e) {
				System.out.println("X : " + e.getX() + ", Y = " + e.getY());
				zoom += 0.1;
				
			}
		};
		this.addMouseMotionListener(ac);
		timer = new Timer(1, this);
		timer.setInitialDelay(1);
		timer.start(); 
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		this.repaint();
	}
	
	public void loadCityModel() {
		
	}
	
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(new Color(255, 50, 50));
		g2d.fill(new Ellipse2D.Double(500, 500, 50*zoom, 50*zoom));
		//Dessin du background
	//	g2d.draw(new Ellipse2D.Double(10, 10, 100, 500));
	}
}
