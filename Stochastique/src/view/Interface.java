/**
 * 
 */
package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.Point2D;
import java.io.PrintStream;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import Controller.Manager;
import Exceptions.IncorrectModeleException;
import Exceptions.NoSolutionException;
import Exceptions.NotInitalizedException;

/**
 * @author Thibaut
 *
 */

public class Interface
{
	private static enum solveur {
		solvLin, recuit;
	}
	private static enum deterStocha {
		deterministe, stochastique;
	}
	
	/**
	 * @param args
	 */
	private static JFrame frame;
	private static JPanel panelGlobal;
	private static JPanel panelLeft;
	private static CityDisplay panelRight;
	private static JComboBox<String> choixResol;
	private static JComboBox<String> choixPb;
	private static JPanel alphaPanel;
	private static JPanel alphaChoicePanel;
	private static JLabel alphaLabel;
	private static JTextField alphaField;
	private static JButton alphaButton;
	private static JLabel alphaActualValue;
	private static JButton buttonData;
	private static JButton buttonResol;
	private static JLabel labelChoix;
	private static JLabel labelData;
	private static JLabel labelSubData1;
	private static JLabel labelSubData2;
	private static JCheckBox systemOutputCheckBox;
	private static JPanel systemOutputPanel;
	private static JLabel systemOutputLabel;
	private static JScrollPane systemOutput;
	private static JTextArea systemOutputText;
	private static JTextArea tamponResol;
	private static JTextArea tamponData;
	private static JTextArea tamponAlpha;
	private static JTextArea solvResult;
	private static JTextArea test;
	private static Manager manager;
	private static solveur slv;
	private static deterStocha dt;
	private static double alpha;
	
	private static void createNewJFrame()
	{
		manager = new Manager();
		
		frame = new JFrame("Stochastique");
		frame.setSize(900, 750);
		frame.setMinimumSize(new Dimension(900,  750));
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addComponentListener(new ComponentListener() {
			
			@Override
			public void componentMoved(ComponentEvent arg0) {}
			@Override
			public void componentHidden(ComponentEvent arg0) {}
			@Override
			public void componentShown(ComponentEvent arg0) {}
			
			@Override
			public void componentResized(ComponentEvent arg0) {
				panelRight.repaint();	
			}
		});
		
		panelGlobal = new JPanel();
		panelGlobal.setLayout(new BorderLayout());
		
		labelChoix = new JLabel("Choix de resolution");
		
		String[] pb = { "Probleme du Voyageur de Commerce deterministe", "Probleme du Voyageur de Commerce stochastique"};
		choixPb = new JComboBox<String>(pb);
		choixPb.setPreferredSize(new Dimension(100,10));
		choixPb.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		choixPb.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox<String> js = (JComboBox<String>)e.getSource();
				if(js.getSelectedIndex() == 0) {
					dt = deterStocha.deterministe;
					alphaPanel.setVisible(false);
				}
				else {
					dt = deterStocha.stochastique;
					alphaPanel.setVisible(true);
				}
			}
		});
		choixPb.setMaximumSize(new Dimension(350, 30));
		dt = deterStocha.deterministe;
		
		String[] resol = { "CPlex", "Recuit simule"};
		choixResol = new JComboBox<String>(resol);
		choixResol.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		choixResol.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox<String> js = (JComboBox<String>)e.getSource();
				if(js.getSelectedIndex() == 0) {
					slv = solveur.solvLin;
					test.setVisible(false);
					solvResult.setVisible(true);
				}
				else {
					slv = solveur.recuit;
					test.setVisible(true);
					solvResult.setVisible(false);
				}
			}
		});
		choixResol.setMaximumSize(new Dimension(350, 30));
		slv = solveur.solvLin;
		
		alphaPanel = new JPanel();
		alphaChoicePanel = new JPanel();
		alphaLabel = new JLabel("Alpha");
		alphaField = new JTextField("0.8");
		alphaField.setEditable(true);
		alphaField.setPreferredSize(new Dimension(100, 20));
		alphaButton = new JButton("Valider");
		alphaButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					double newAlpha = Double.parseDouble(alphaField.getText());
					if(newAlpha >= 0.d && newAlpha <= 1.d) {
						alpha = newAlpha;
						alphaActualValue.setText("Valeur alpha actuelle : " + Double.toString(alpha));
					}
					else {
						JOptionPane.showMessageDialog(null, "Alpha doit etre compris entre 0 et 1", "Erreur", JOptionPane.ERROR_MESSAGE);
						alphaField.setText(Double.toString(alpha));
					}
				}
				catch(NullPointerException ex) {
					JOptionPane.showMessageDialog(null, "Aucune valeur n'a ete entree", "Erreur", JOptionPane.ERROR_MESSAGE);
					alphaField.setText(Double.toString(alpha));
				}
				catch(NumberFormatException ex) {
					JOptionPane.showMessageDialog(null, "Le champ alpha est incorrect", "Erreur", JOptionPane.ERROR_MESSAGE);
					alphaField.setText(Double.toString(alpha));
				}
			}
		});
		alpha = 0.8;
		
		alphaActualValue = new JLabel("Valeur alpha actuelle : 0.8");
		tamponAlpha = new JTextArea();
		tamponAlpha.setEditable(false);
		tamponAlpha.setBackground(alphaPanel.getBackground());
		tamponAlpha.setPreferredSize(new Dimension(10, 10));
		tamponAlpha.setMinimumSize(new Dimension(10, 10));
		tamponAlpha.setMaximumSize(new Dimension(10, 10));
		
		alphaChoicePanel.add(alphaLabel);
		alphaChoicePanel.add(alphaField);
		alphaChoicePanel.add(alphaButton);
		
		BoxLayout alphaLayout = new BoxLayout(alphaPanel, BoxLayout.PAGE_AXIS);
		alphaPanel.setLayout(alphaLayout);
		alphaPanel.add(alphaChoicePanel);
		alphaPanel.add(alphaActualValue);
		alphaPanel.add(tamponAlpha);
		alphaActualValue.setAlignmentX(Component.CENTER_ALIGNMENT);
		alphaPanel.setBorder(BorderFactory.createLineBorder(Color.gray));
		alphaPanel.setVisible(false);
		alphaPanel.setMaximumSize(new Dimension(300, 30));
		
		
		labelData = new JLabel("Choix des donnees");
		
		buttonData = new JButton("Selectionner un fichier de donnees");
		
		buttonData.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				JFileChooser chooser = new JFileChooser();//création dun nouveau filechosser
	            chooser.setApproveButtonText("Choix du fichier..."); //intitulé du bouton
	            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
	            {	
            	    JFrame selectFile = new JFrame("Explorateur de fichier"); //titre
            	    selectFile.setSize(450,100); //taille
            	    selectFile.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//fermeture
                    JPanel pane = new JPanel();
                    BorderLayout bord = new BorderLayout();
                    pane.setLayout(bord);
                    selectFile.setContentPane(pane);
                    selectFile.setVisible(true);
	         
	                String filepath = chooser.getSelectedFile().getAbsolutePath();
	                selectFile.setVisible(false);
	                System.out.println(filepath);
	                try {
	                    manager.loadModele(filepath);
	                    labelSubData2.setText(chooser.getSelectedFile().getAbsolutePath()); 
	                    //si un fichier est selectionné, récupérer le fichier puis sont path et l'afficher dans le champs de texte
	                } catch (IncorrectModeleException e) {
	                	JOptionPane.showMessageDialog(null, e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
	                }
	            }
			}
		});
		
		labelSubData1 = new JLabel("Fichier selectionne :");
		
		labelSubData2 = new JLabel();
		
		buttonResol = new JButton("Résoudre");
	
		buttonResol.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				try {
					ArrayList<Integer> arcs = null;
					if(slv == solveur.solvLin) {
						if(dt == deterStocha.deterministe)
							arcs = manager.optimizeSolvDeter();
						else
							arcs = manager.optimizeSolvStocha(alpha);
					}
					else if(slv == solveur.recuit) {
						if(dt == deterStocha.deterministe)
							arcs = manager.optimizeRecuitDeter();
					}
					
					panelRight.displaySolution(arcs);
					panelRight.repaint();
				}
				catch (NotInitalizedException e) {
					JOptionPane.showMessageDialog(null, e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
				}
				catch (NoSolutionException e) {
					JOptionPane.showMessageDialog(null, e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		systemOutputCheckBox = new JCheckBox("Afficher le flux de sortie console");
		systemOutputCheckBox.setPreferredSize(new Dimension(350, 40));
		systemOutputCheckBox.setMinimumSize(new Dimension(350, 40));
		systemOutputCheckBox.setBackground(Color.lightGray);
		systemOutputCheckBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JCheckBox jcb = (JCheckBox)e.getSource();
				if(jcb.isSelected()) {
					systemOutputPanel.setVisible(true);
				}
				else systemOutputPanel.setVisible(false);
			}
		});
		
		systemOutputPanel = new JPanel();
		BoxLayout sysoLayout = new BoxLayout(systemOutputPanel, BoxLayout.PAGE_AXIS);
		systemOutputPanel.setLayout(sysoLayout);
		systemOutputPanel.setPreferredSize(new Dimension(350, 100));
		systemOutputPanel.setMinimumSize(new Dimension(350, 100));
		systemOutputPanel.setVisible(false);
		
		systemOutputLabel = new JLabel("Console : ");
		systemOutputLabel.setPreferredSize(new Dimension(100, 40));
		
		systemOutputText = new JTextArea();
		systemOutputText.setEditable(false);
		PrintStream printStream = new PrintStream(new JTextAreaStream(systemOutputText));
		System.setOut(printStream);
		System.setErr(printStream);
		systemOutput = new JScrollPane(systemOutputText);
		systemOutput.setBackground(Color.white);
		systemOutput.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		systemOutputPanel.add(systemOutputLabel);
		systemOutputPanel.add(systemOutput);
		systemOutputPanel.setBackground(Color.lightGray);
		
		tamponResol = new JTextArea();
		tamponResol.setEditable(false);
		tamponResol.setBackground(Color.lightGray);
		tamponResol.setMaximumSize(new Dimension(300, 30));
		
		tamponData = new JTextArea();
		tamponData.setEditable(false);
		tamponData.setMinimumSize(new Dimension(10,50));
		tamponData.setMaximumSize(new Dimension(300, 30));
		tamponData.setBackground(Color.lightGray);
		
		solvResult = new JTextArea("\n  Longueur du chemin optimal : ");
		solvResult.setEditable(false);
		solvResult.setMinimumSize(new Dimension(350, 50));
		solvResult.setMaximumSize(new Dimension(350, 50));
		solvResult.setBorder(BorderFactory.createLineBorder(Color.gray));
		solvResult.setVisible(true);
		
		test = new JTextArea();
		test.setEditable(false);
		test.setMinimumSize(new Dimension(310 ,160));
		test.setMaximumSize(new Dimension(310 ,160));
		test.setBorder(BorderFactory.createLineBorder(Color.gray));
		test.append("\n");
		test.append("  Temperature : " + "\n");
		test.append("\n");
		test.append("  Cout total initial : " + "\n");
		test.append("\n");
		test.append("  Cout total final : " + "\n");
		test.append("\n");
		test.append("  Temps de resolution : " + "\n");
		test.setVisible(false);
		
		panelLeft = new JPanel();
		panelLeft.setBackground(Color.lightGray);
		GroupLayout layout = new GroupLayout(panelLeft);
		panelLeft.setLayout(layout);
		
		layout.setHorizontalGroup(
				   layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
					.addComponent(labelChoix)	
				   	.addComponent(choixResol)		  
				   	.addComponent(choixPb)
				   	.addComponent(alphaPanel)
					.addComponent(tamponResol)
				   	.addComponent(labelData)
				   	.addComponent(buttonData)
				   	.addComponent(labelSubData1)
				   	.addComponent(labelSubData2)
					.addComponent(tamponData)
				   	.addComponent(buttonResol)
				   	.addComponent(solvResult)
				   	.addComponent(test)
				   	.addComponent(systemOutputCheckBox)
				   	.addComponent(systemOutputPanel))
		);
		
		layout.setVerticalGroup(
				layout.createSequentialGroup()
					  .addComponent(labelChoix)
				      .addComponent(choixResol)
				      .addComponent(choixPb)
				      .addComponent(alphaPanel)
					  .addComponent(tamponResol)
					  .addComponent(labelData)	  
					  .addComponent(buttonData)
					  .addComponent(labelSubData1)
					  .addComponent(labelSubData2)
					  .addComponent(tamponData)
				      .addComponent(buttonResol)
				      .addComponent(solvResult)
					  .addComponent(test)
					  .addComponent(systemOutputCheckBox)
					  .addComponent(systemOutputPanel)
		);
		

		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		
		panelRight = new CityDisplay();
		//panelRight.setBackground(Color.WHITE);
		
		panelGlobal.add(panelLeft, BorderLayout.WEST);
		panelGlobal.add(panelRight, BorderLayout.CENTER);
		
		frame.setContentPane(panelGlobal);
		
		frame.setVisible(true);
		frame.validate();
	}
	
	public static void loadCityModel(ArrayList<Point2D> cityPos)
	{
		systemOutputText.setText("");
		panelRight.loadCityModel(cityPos);
	}
	
	public static void setSolvOptimumCost (double optimalValue) {
		solvResult.setText("\n  Longueur du chemin optimal : " + (optimalValue == -1.d ? "" : Double.toString(optimalValue)));
	}
		
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		createNewJFrame();
	}

}
