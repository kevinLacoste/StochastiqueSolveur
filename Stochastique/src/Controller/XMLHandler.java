package Controller;

import java.util.ArrayList;
import java.util.HashMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLHandler extends DefaultHandler {
		
	public void startDocument() throws SAXException {
		nbVilles = 0;
		isEdge = false;
		coutsArcs = new ArrayList<HashMap<Integer, Double>>();
	}

	public void startElement(String uri, String localName,String qName, 
                Attributes attributes) throws SAXException {
		if(qName.equals("vertex"))
		{
			coutsArcs.add(new HashMap<Integer, Double>());
			nbVilles++;
		}
		
		
		else if(qName.equals("edge"))
		{
			isEdge = true;
			String costStr = attributes.getValue("cost");
			if(costStr == null) throw new SAXException("There is an edge node with no cost attribute...");
			else cost = Double.parseDouble(costStr);
		}
	}
	

	public void characters(char ch[], int start, int length) throws SAXException {
		if(isEdge)
		{
			coutsArcs.get(nbVilles-1).put(Integer.parseInt(new String(ch, start, length)), cost);
			isEdge = false;
		}
	}
	
	public int getNbVilles()
	{
		return nbVilles;
	}
	
	public ArrayList<HashMap<Integer, Double>> getCoutsArcs()
	{
		return coutsArcs;
	}
	
	private int nbVilles;
	private boolean isEdge;
	private double cost;
	private ArrayList<HashMap<Integer, Double>> coutsArcs;
};
