package Controller;
import Model.Modele;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.helpers.DefaultHandler;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;


public class Parser {
	public Modele loadData(String filepath)
	{
		if(filepath.endsWith(".xml"))
		{
			return loadXML(filepath);
		}
		
		else if(filepath.endsWith(".tsp"))
		{
			return loadTSP(filepath);
		}
		
		else //Cas ou le type n'est pas reconnu
		{
			return null;
		}
	}
	
	private Modele loadXML(String filepath)
	{
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			DefaultHandler handler = new XMLHandler();
			XMLHandler xmlHandler = (XMLHandler)handler;
			
			saxParser.parse(filepath, handler);
			return new Modele(xmlHandler.getNbVilles(), xmlHandler.getCoutsArcs(), null, null);
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
            while(!buffer.equals("EOF"))
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
            
            return new Modele(nbVilles, coutsArcs, null, positions);
        }
        catch(IOException e)
        {
        	e.printStackTrace();
        	return null;
        }
	}
}
