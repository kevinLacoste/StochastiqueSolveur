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
	private static JPanel solvTestDeter;
	private static JTextField solvResultDeter;
	private static JTextField solvTimeElapsedDeter;
	private static JPanel solvTestStocha;
	private static JTextField solvResultStocha;
	private static JTextField solvTimeElapsedStocha;
	private static JPanel recuitTestDeter;
	private static JTextField recuitTempDeter;
	private static JTextField recuitCoutCurrentDeter;
	private static JTextField recuitCoutFinalDeter;
	private static JTextField recuitTimeElapsedDeter;
	private static JPanel recuitTestStocha;
	private static JTextField recuitTempStocha;
	private static JTextField recuitCoutCurrentStocha;
	private static JTextField recuitCoutFinalStocha;
	private static JTextField recuitTimeElapsedStocha;
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
					if(slv == solveur.solvLin) {
						solvTestStocha.setVisible(false);
						solvTestDeter.setVisible(true);
					}
					else {
						recuitTestStocha.setVisible(false);
						recuitTestDeter.setVisible(true);
					}
				}
				else {
					dt = deterStocha.stochastique;
					if(slv == solveur.solvLin) {
						solvTestDeter.setVisible(false);
						solvTestStocha.setVisible(true);
					}
					else {
						recuitTestDeter.setVisible(false);
						recuitTestStocha.setVisible(true);
					}
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
					if(dt == deterStocha.deterministe) {
						solvTestDeter.setVisible(true);
						recuitTestDeter.setVisible(false);
					}
					else {
						solvTestStocha.setVisible(true);
						recuitTestStocha.setVisible(false);
					}
				}
				else {
					slv = solveur.recuit;
					if(dt == deterStocha.deterministe) {
						solvTestDeter.setVisible(false);
						recuitTestDeter.setVisible(true);
					}
					else {
						solvTestStocha.setVisible(false);
						recuitTestStocha.setVisible(true);
					}
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
				Thread loadThread = new Thread() {
					public void run() {
						JFileChooser chooser = new JFileChooser();//cr�ation dun nouveau filechosser
			            chooser.setApproveButtonText("Choix du fichier..."); //intitul� du bouton
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
			                    //si un fichier est selectionn�, r�cup�rer le fichier puis sont path et l'afficher dans le champs de texte
			                } catch (IncorrectModeleException e) {
			                	JOptionPane.showMessageDialog(null, e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
			                }
			            }
					};
				};
				loadThread.start();
			}
		});
		
		labelSubData1 = new JLabel("Fichier selectionne :");
		
		labelSubData2 = new JLabel();
		
		buttonResol = new JButton("R�soudre");
	
		buttonResol.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				Thread workThread = new Thread() {
					@Override
					public void run() {
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
								else
									arcs = manager.optimizeRecuitStocha();
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
				};
				workThread.start();
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
		
		//Solveur deterministe
		
		solvTestDeter = new JPanel();
		BoxLayout solvTestLayoutDeter = new BoxLayout(solvTestDeter, BoxLayout.PAGE_AXIS);
		solvTestDeter.setLayout(solvTestLayoutDeter);
		solvTestDeter.setVisible(true);
		solvTestDeter.setBorder(BorderFactory.createLineBorder(Color.gray));
		
		solvResultDeter = new JTextField("  Longueur du chemin optimal : ");
		solvResultDeter.setEditable(false);
		solvResultDeter.setMinimumSize(new Dimension(350, 50));
		solvResultDeter.setMaximumSize(new Dimension(350, 50));
		
		solvTimeElapsedDeter = new JTextField("  Temps de calcul total : ");
		solvTimeElapsedDeter.setEditable(false);
		solvTimeElapsedDeter.setMinimumSize(new Dimension(350, 50));
		solvTimeElapsedDeter.setMaximumSize(new Dimension(350, 50));
		
		solvTestDeter.add(solvResultDeter);
		solvTestDeter.add(solvTimeElapsedDeter);
		
		//Solveur stochastique
		
		solvTestStocha = new JPanel();
		BoxLayout solvTestLayoutStocha = new BoxLayout(solvTestStocha, BoxLayout.PAGE_AXIS);
		solvTestStocha.setLayout(solvTestLayoutStocha);
		solvTestStocha.setVisible(false);
		solvTestStocha.setBorder(BorderFactory.createLineBorder(Color.gray));
		
		solvResultStocha = new JTextField("  Longueur du chemin optimal : ");
		solvResultStocha.setEditable(false);
		solvResultStocha.setMinimumSize(new Dimension(350, 50));
		solvResultStocha.setMaximumSize(new Dimension(350, 50));
		
		solvTimeElapsedStocha = new JTextField("  Temps de calcul total : ");
		solvTimeElapsedStocha.setEditable(false);
		solvTimeElapsedStocha.setMinimumSize(new Dimension(350, 50));
		solvTimeElapsedStocha.setMaximumSize(new Dimension(350, 50));
		
		solvTestStocha.add(solvResultStocha);
		solvTestStocha.add(solvTimeElapsedStocha);
		
		//Recuit deterministe
		
		recuitTestDeter = new JPanel();
		BoxLayout recuitTestLayoutDeter = new BoxLayout(recuitTestDeter, BoxLayout.PAGE_AXIS);
		recuitTestDeter.setLayout(recuitTestLayoutDeter);
		recuitTestDeter.setBorder(BorderFactory.createLineBorder(Color.gray));
		recuitTestDeter.setVisible(false);
		
		recuitTempDeter = new JTextField("  Temperature : ");
		recuitTempDeter.setEditable(false);
		recuitTempDeter.setMinimumSize(new Dimension(350, 50));
		recuitTempDeter.setMaximumSize(new Dimension(350, 50));
		
		recuitCoutCurrentDeter = new JTextField("  Meilleur Cout actuel : ");
		recuitCoutCurrentDeter.setEditable(false);
		recuitCoutCurrentDeter.setMinimumSize(new Dimension(350, 50));
		recuitCoutCurrentDeter.setMaximumSize(new Dimension(350, 50));
		
		recuitCoutFinalDeter = new JTextField("  Cout final : ");
		recuitCoutFinalDeter.setEditable(false);
		recuitCoutFinalDeter.setMinimumSize(new Dimension(350, 50));
		recuitCoutFinalDeter.setMaximumSize(new Dimension(350, 50));
		
		recuitTimeElapsedDeter = new JTextField("  Temps de calcul total : ");
		recuitTimeElapsedDeter.setEditable(false);
		recuitTimeElapsedDeter.setMinimumSize(new Dimension(350, 50));
		recuitTimeElapsedDeter.setMaximumSize(new Dimension(350, 50));
		
		recuitTestDeter.add(recuitTempDeter);
		recuitTestDeter.add(recuitCoutCurrentDeter);
		recuitTestDeter.add(recuitCoutFinalDeter);
		recuitTestDeter.add(recuitTimeElapsedDeter);
		
		//Recuit stochastique
		
		recuitTestStocha = new JPanel();
		BoxLayout recuitTestLayoutStocha = new BoxLayout(recuitTestStocha, BoxLayout.PAGE_AXIS);
		recuitTestStocha.setLayout(recuitTestLayoutStocha);
		recuitTestStocha.setBorder(BorderFactory.createLineBorder(Color.gray));
		recuitTestStocha.setVisible(false);
		
		recuitTempStocha = new JTextField("  Temperature : ");
		recuitTempStocha.setEditable(false);
		recuitTempStocha.setMinimumSize(new Dimension(350, 50));
		recuitTempStocha.setMaximumSize(new Dimension(350, 50));
		
		recuitCoutCurrentStocha = new JTextField("  Meilleur Cout actuel : ");
		recuitCoutCurrentStocha.setEditable(false);
		recuitCoutCurrentStocha.setMinimumSize(new Dimension(350, 50));
		recuitCoutCurrentStocha.setMaximumSize(new Dimension(350, 50));
		
		recuitCoutFinalStocha = new JTextField("  Cout final : ");
		recuitCoutFinalStocha.setEditable(false);
		recuitCoutFinalStocha.setMinimumSize(new Dimension(350, 50));
		recuitCoutFinalStocha.setMaximumSize(new Dimension(350, 50));
		
		recuitTimeElapsedStocha = new JTextField("  Temps de calcul total : ");
		recuitTimeElapsedStocha.setEditable(false);
		recuitTimeElapsedStocha.setMinimumSize(new Dimension(350, 50));
		recuitTimeElapsedStocha.setMaximumSize(new Dimension(350, 50));
		
		recuitTestStocha.add(recuitTempStocha);
		recuitTestStocha.add(recuitCoutCurrentStocha);
		recuitTestStocha.add(recuitCoutFinalStocha);
		recuitTestStocha.add(recuitTimeElapsedStocha);
		
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
				   	.addComponent(solvTestDeter)
				   	.addComponent(solvTestStocha)
				   	.addComponent(recuitTestDeter)
				   	.addComponent(recuitTestStocha)
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
				      .addComponent(solvTestDeter)
					  .addComponent(solvTestStocha)
					  .addComponent(recuitTestDeter)
					  .addComponent(recuitTestStocha)
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
	
	public static void setSolvDeterOptimumCost (double optimalValue) {
		solvResultDeter.setText("  Longueur du chemin optimal : " + (optimalValue == -1.d ? "" : Double.toString(optimalValue)));
	}
	
	public static void setSolvStochaOptimumCost (double optimalValue) {
		solvResultStocha.setText("  Longueur du chemin optimal : " + (optimalValue == -1.d ? "" : Double.toString(optimalValue)));
	}
	
	public static void setSolvDeterTimeElapsed(double timeElapsed) {
		solvTimeElapsedDeter.setText("  Temps de calcul total : " + (timeElapsed == -1.d ? "" : Double.toString(timeElapsed) + " sec"));
	}
	
	public static void setSolvStochaTimeElapsed(double timeElapsed) {
		solvTimeElapsedStocha.setText("  Temps de calcul total : " + (timeElapsed == -1.d ? "" : Double.toString(timeElapsed) + " sec"));
	}
	
	public static void setRecuitDeterTimeElapsed(double timeElapsed) {
		recuitTimeElapsedDeter.setText("  Temps de calcul total : " + (timeElapsed == -1.d ? "" : Double.toString(timeElapsed) + " sec"));
	}
	
	public static void setRecuitStochaTimeElapsed(double timeElapsed) {
		recuitTimeElapsedStocha.setText("  Temps de calcul total : " + (timeElapsed == -1.d ? "" : Double.toString(timeElapsed) + " sec"));
	}
	
	public static void setRecuitDeterCoutFinal(double coutFinal) {
		recuitCoutFinalDeter.setText("  Cout final : " + (coutFinal == -1.d ? "" : Double.toString(coutFinal)));
	}
	
	public static void setRecuitStochaCoutFinal(double coutFinal) {
		recuitCoutFinalStocha.setText("  Cout final : " + (coutFinal == -1.d ? "" : Double.toString(coutFinal)));
	}
	
	public static void setRecuitDeterTemp(double temperature) {
		recuitTempDeter.setText("  Temperature : " + (temperature == -1.d ? "" : Double.toString(temperature)));
	}
	
	public static void setRecuitStochaTemp(double temperature) {
		recuitTempStocha.setText("  Temperature : " + (temperature == -1.d ? "" : Double.toString(temperature)));
	}
	
	public static void setRecuitDeterCurrent(double coutActuel) {
		recuitCoutCurrentDeter.setText("  Meilleur Cout actuel : " + (coutActuel == -1.d ? "" : Double.toString(coutActuel)));
	}
	
	public static void setRecuitStochaCurrent(double coutActuel) {
		recuitCoutCurrentStocha.setText("  Meilleur Cout actuel : " + (coutActuel == -1.d ? "" : Double.toString(coutActuel)));
	}
		
	public static void main(String[] args) {
		createNewJFrame();
		long heapMaxSize = Runtime.getRuntime().maxMemory();
		System.out.println(heapMaxSize);
	}

}
