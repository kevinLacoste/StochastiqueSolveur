package Controller;
import Model.Modele;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.helpers.DefaultHandler;


public class Parser {
	public Modele loadData(String filepath)
	{
		return null;
	}
	
	public Modele loadXML(String filepath)
	{
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			DefaultHandler handler = new XMLHandler();
			XMLHandler xmlHandler = (XMLHandler)handler;
			
			saxParser.parse(filepath, handler);
			return new Modele(xmlHandler.getNbVilles(), xmlHandler.getCoutsArcs(), null);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
