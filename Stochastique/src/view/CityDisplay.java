package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import javax.swing.JPanel;

public class CityDisplay extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5715041913597651423L;
	private ArrayList<Point> cityPoints;
	private ArrayList<Integer> chemin;
	private int ancientPosX;
	private int ancientPosY;
	private double zoom;
	
	public CityDisplay() {
		cityPoints = null; // Non initialise
		ancientPosX = -1;
		ancientPosY = -1;
		zoom = 1.d;
		
		this.setBackground(Color.white);
		this.addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(MouseEvent e) {}
			
			@Override
			public void mouseDragged(MouseEvent e) {
				if(ancientPosX == -1 && ancientPosY == -1) { //Initialise l'ancienne position
					ancientPosX = e.getX();
					ancientPosY = e.getY();
				}
				else {
					for(Point p : cityPoints) {
						p.setLocation(p.getX() + e.getX() - ancientPosX, p.getY() + e.getY() - ancientPosY);
					}
					ancientPosX = e.getX();
					ancientPosY = e.getY();
				}
				repaint();
			}
		});
		this.addMouseListener(new MouseListener() {
			@Override
			public void mousePressed(MouseEvent arg0) {}
			@Override
			public void mouseExited(MouseEvent arg0) {}
			@Override
			public void mouseEntered(MouseEvent arg0) {}
			@Override
			public void mouseClicked(MouseEvent arg0) {}
			
			@Override
			public void mouseReleased(MouseEvent arg0) {
				ancientPosX = -1;
				ancientPosY = -1;
			}
		});
		this.addMouseWheelListener(new MouseWheelListener() {
			
			@Override
			public void mouseWheelMoved(MouseWheelEvent arg0) {
				zoom -= 0.1 * (double)arg0.getWheelRotation();
				repaint();
			}
		});
	}
	
	public void loadCityModel(ArrayList<Point2D> cityPos) {
		if(cityPos != null && !cityPos.isEmpty()) {
			cityPoints = new ArrayList<Point>();
			double minX = cityPos.get(0).getX();
			double maxX = cityPos.get(0).getX();
			double minY = cityPos.get(0).getY();
			double maxY = cityPos.get(0).getY();
			for(Point2D p : cityPos) {
				if(p.getX() < minX)
					minX = p.getX();
				else if(p.getX() > maxX)
					maxX = p.getX();
				
				if(p.getY() < minY)
					minY = p.getY();
				else if(p.getY() > maxY)
					maxY = p.getY();
			}
			double dX = maxX-minX;
			double dY = maxY-minY;
			
			double panelWidth = this.getWidth();
			double panelHeight = this.getHeight();
			double coeffX = (panelWidth*14/15)/dX;
			double coeffY = (panelHeight*14/15)/dY;
			double space = 0;
			
			if(dX > dY)
				space = panelHeight - dY*coeffX - panelWidth/15;
			else
				space = panelWidth - dX*coeffY - panelHeight/15;
			
			if(cityPos != null) {
				for(Point2D p : cityPos) {
					if(dX > dY)
						cityPoints.add(new Point((int)((p.getX()-minX)*(coeffX) + panelWidth/30), 
												 (int)((p.getY()-minY)*(coeffX) + panelWidth/30 + space/2)));
					else
						cityPoints.add(new Point((int)((p.getX()-minX)*(coeffY) + panelHeight/30 + space/2),
												 (int)((p.getY()-minY)*(coeffY) + panelHeight/30)));
				}
			}
			this.chemin = null;
			repaint();
		}
		//TODO else
	}
	
	public void displaySolution(ArrayList<Integer> chemin) {
		this.chemin = chemin;
	}
	
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2d = (Graphics2D) g;
		Stroke oldStroke = g2d.getStroke();
		g2d.setStroke(new BasicStroke(1.f * (float)(zoom/4 + 0.75)));
		g2d.setStroke(oldStroke);
		
		boolean first = true;
		Point p;
		
		if(cityPoints != null) {
			for(int i=0; i<cityPoints.size(); i++) {
				p = cityPoints.get(i);
				if(first) {
					g2d.setColor(new Color(50, 50, 255));
					first = false;
				}
				else g2d.setColor(new Color(255, 50, 50));
			
				if((int)((double)p.x*zoom) >= 0 && (int)((double)p.x*zoom) < this.getWidth() && 
				   (int)((double)p.y*zoom) >= 0 && (int)((double)p.y*zoom) < this.getHeight()) 
				{
					g2d.fillOval((int)((double)p.x*zoom), (int)((double)p.y*zoom), (int)(10*(zoom/4 + 0.75)), (int)(10*(zoom/4 + 0.75)));
					g2d.setColor(Color.black);
					g2d.drawString(Integer.toString(i), (int)((double)p.x*zoom), (int)((double)p.y*zoom));
				}
			}
		}
		
		if(chemin != null) {
			Point p1, p2;
			for(int i=0; i<chemin.size()-1; i++) {
				p1 = cityPoints.get(chemin.get(i));
				p2 = cityPoints.get(chemin.get(i+1));
				g2d.drawLine((int)((double)p1.x*zoom + 5*(zoom/4 + 0.75)), (int)((double)p1.y*zoom + 5*(zoom/4 + 0.75)), 
							 (int)((double)p2.x*zoom + 5*(zoom/4 + 0.75)), (int)((double)p2.y*zoom + 5*(zoom/4 + 0.75)));
			}
		}
	}
}
