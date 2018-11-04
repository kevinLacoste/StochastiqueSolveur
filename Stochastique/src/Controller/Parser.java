package Controller;
import Model.Modele;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.helpers.DefaultHandler;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.lang.Math;


public class Parser {
	public Modele loadData(String filepath) throws FileNotFoundException
	{
		try {
			FileReader fr = new FileReader(filepath);
			fr.close();
		}
		catch (FileNotFoundException e) {
			throw e;
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		if(filepath.endsWith(".xml"))
		{
			return loadXML(filepath);
		}
		
		else if(filepath.endsWith(".tsp"))
		{
			return loadTSP(filepath);
		}
		
		else return null;  //Cas ou le type n'est pas reconnu
	}
	
	private Modele loadXML(String filepath)
	{
		int nbVilles;
		int thirdPoint;
		ArrayList<HashMap<Integer, Double>> coutsArcs;
		HashMap<Integer, Point2D> positions;
		ArrayList<Point2D> positionsArray;
		double x=0, y=0, x1, x2, x3, y1, y2, y3;
		double c1, c2, c3;
		double l1, l2;
		
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			DefaultHandler handler = new XMLHandler();
			XMLHandler xmlHandler = (XMLHandler)handler;
			
			saxParser.parse(filepath, handler);
			nbVilles = xmlHandler.getNbVilles();
			coutsArcs = xmlHandler.getCoutsArcs();
			positions = new HashMap<Integer, Point2D>();
			
			//Calculate positions
			positions.put(0, new Point2D.Double(0,0));
			positions.put(1, new Point2D.Double(coutsArcs.get(0).get(1), 0));
			for(thirdPoint=2; thirdPoint<nbVilles && y==0; thirdPoint++)
			{
				x = (Math.pow(coutsArcs.get(0).get(1), 2) + 
					 Math.pow(coutsArcs.get(thirdPoint).get(0), 2) -
					 Math.pow(coutsArcs.get(thirdPoint).get(1), 2)) / (2*coutsArcs.get(0).get(1));
				y = Math.sqrt(Math.pow(coutsArcs.get(thirdPoint).get(0), 2) - Math.pow(x, 2));
			}
			thirdPoint--;
			
			positions.put(thirdPoint, new Point2D.Double(x,y));
			
			x1 = positions.get(0).getX();
			x2 = positions.get(1).getX();
			x3 = positions.get(thirdPoint).getX();
			y1 = positions.get(0).getY();
			y2 = positions.get(1).getY();
			y3 = positions.get(thirdPoint).getY();
			
			c1 = (Math.pow(x2, 2) - Math.pow(x1, 2) +
				  Math.pow(y2, 2) - Math.pow(y1, 2))/2;
			
			c2 = (Math.pow(x3, 2) - Math.pow(x2, 2) +
				  Math.pow(y3, 2) - Math.pow(y2, 2))/2;
			
			c3 = 1 + (x3 - x2)*(y2 - y1)/(y3 - y2);
			
			for(int i=2;i<nbVilles;i++)
			{
				if(i != thirdPoint)
				{
					l1 = (Math.pow(coutsArcs.get(i).get(0), 2) -
						  Math.pow(coutsArcs.get(i).get(1), 2))/2;
					l2 = (Math.pow(coutsArcs.get(i).get(1), 2) -
						  Math.pow(coutsArcs.get(i).get(thirdPoint), 2))/2;
					
					x = ((c1+l1) - (c2+l2)*((y2-y1)/(y3-y2)))/((x2-x1)*c3);
					y = ((c2+l2) - x*(x3-x2))/(y3-y2);
					positions.put(i, new Point2D.Double(x,y));
				}
			}
			
			positionsArray = new ArrayList<Point2D>();
			for(int i=0; i<nbVilles; i++)
			{
				positionsArray.add(positions.get(i));
			}
			return new Modele(nbVilles, coutsArcs, positionsArray);
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private Modele loadTSP (String filepath)
	{
		Scanner sc;
		BufferedReader reader = null;
		String buffer;
		boolean coords = false;
		
		int nbVilles = 0;
		ArrayList<Integer> ranking;
		ArrayList<Point2D> positions;
		ArrayList<Point2D> temp;
		ArrayList<HashMap<Integer, Double>> coutsArcs;
        
        try {
            reader = new BufferedReader(new FileReader(filepath));
            buffer = reader.readLine();
            
            ranking = new ArrayList<Integer>();
            positions = new ArrayList<Point2D>();
            temp = new ArrayList<Point2D>();
            coutsArcs = new ArrayList<HashMap<Integer, Double>>();
            
            //Premiere etape : nb de villes du probleme et positions
            while(!buffer.equals("EOF") && buffer != null)
            {
            	if(!coords) {
            		if(buffer.contains("DIMENSION")) {
                		sc = new Scanner(buffer);
                		while(sc.hasNext())
                		{
                			buffer = sc.next();
                			try
                			{
                				nbVilles = Integer.parseInt(buffer);
                				break;
                			}
                			catch(NumberFormatException e){}
                		}
                	}
                	else if(buffer.startsWith("NODE_COORD_SECTION")) {
                		coords = true;
                	}
            	}
            	
            	else {
            		sc = new Scanner(buffer);
            		try
            		{
            			ranking.add(sc.nextInt());
                		temp.add(new Point2D.Double(Double.parseDouble(sc.next()), Double.parseDouble(sc.next())));
            		}
            		catch(Exception e)
            		{
            			e.printStackTrace();
            			sc.close();
            			reader.close();
            			return null;
            		}
            	}
            	
            	buffer = reader.readLine();
            }
            reader.close();
            
            for(int i=0; i<ranking.size(); i++)
            {
            	int index = ranking.indexOf(i+1);
            	positions.add(temp.get(index));
            }
            
            
            //Phase de calcul des distances
            
            HashMap<Integer, Double> hM;
            Point2D villeDepart;
            
            for(int i=0; i<nbVilles; i++)
            {
            	coutsArcs.add(new HashMap<Integer, Double>());
            	hM = coutsArcs.get(i);
            	villeDepart = positions.get(i);
            	for(int j=0; j<nbVilles; j++) {
            		if(i != j) {
            			hM.put(j, villeDepart.distance(positions.get(j)));
            		}
            	}
            }
            
            return new Modele(nbVilles, coutsArcs, positions);
        }
        catch(IOException e)
        {
        	e.printStackTrace();
        	return null;
        }
	}
}
