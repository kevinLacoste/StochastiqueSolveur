/**
 * 
 */
package Interface;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import Model.Modele;

/**
 * @author Thibaut
 *
 */
public class Interface
{

	/**
	 * @param args
	 */
	private static JFrame frame;
	private static JPanel panelGlobal;
	private static JPanel panelLeft;
	private static AffichageVille panelRight;
	private static JComboBox<String> choixPb;
	private static JComboBox<String> choixResol;
	private static JButton buttonData;
	private static JButton buttonResol;
	private static JLabel labelChoix;
	private static JLabel labelData;
	private static JLabel labelSubData1;
	private static JLabel labelSubData2;
	private static JTextArea tamponResol;
	private static JTextArea tamponData;
	private static JTextArea test;
	private static PrintStream printOut;
	
	public static void setSystemOutBuffer()
	{
		
	}
	
	private static void createNewJFrame(Modele m)
	{

		frame = new JFrame("Stochastique");
		frame.setSize(800, 500);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		
		panelGlobal = new JPanel();
		panelGlobal.setLayout(new BorderLayout());
		
		labelChoix = new JLabel("Choix de resolution");
		
		String[] pb = { "Probleme du Voyageur de Commerce deterministe", "Probleme du Voyageur de Commerce stochastique"};
		choixPb = new JComboBox<String>(pb);
		choixPb.setPreferredSize(new Dimension(100,10));
		choixPb.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		
		String[] resol = { "CPlex", "Recuit simule"};
		choixResol = new JComboBox<String>(resol);
		choixResol.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		
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
	                
	         
	                labelSubData2.setText(chooser.getSelectedFile().getAbsolutePath()); //si un fichier est selectionné, récupérer le fichier puis sont path et l'afficher dans le champs de texte
	                String Firm = chooser.getSelectedFile().getAbsolutePath();
	                selectFile.setVisible(false);
	                System.out.println(Firm);
	                //TODO add parser
	                /*try {
	                    LF.LireFichier(Firm);
	                } catch (FileNotFoundException ex) {
	                    System.out.println(ex);
	                }*/
	            }
			}
		});
		
		labelSubData1 = new JLabel("Fichier selectionne :");
		
		labelSubData2 = new JLabel();
		
		buttonResol = new JButton("Résoudre");
	
		buttonResol.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
					
			}
		});
		
		tamponResol = new JTextArea();
		tamponResol.setEditable(false);
		tamponResol.setBackground(Color.lightGray);
		
		tamponData = new JTextArea();
		tamponData.setEditable(false);
		tamponData.setMinimumSize(new Dimension(10,50));
		tamponData.setBackground(Color.lightGray);
		
		test = new JTextArea();
		test.setEditable(false);
		test.setMinimumSize(new Dimension(200,300));
		test.setBackground(Color.lightGray);
		test.append("\n");
		test.append("Temperature : " + "\n");
		test.append("\n");
		test.append("Cout total initial : " + "\n");
		test.append("\n");
		test.append("Cout total final : " + "\n");
		test.append("\n");
		test.append("Temps de resolution : " + "\n");
		
		panelLeft = new JPanel();
		panelLeft.setBackground(Color.lightGray);
		GroupLayout layout = new GroupLayout(panelLeft);
		panelLeft.setLayout(layout);
		
		layout.setHorizontalGroup(
				   layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
					.addComponent(labelChoix)	
				   	.addComponent(choixPb)		  
				   	.addComponent(choixResol)
					.addComponent(tamponResol)
				   	.addComponent(labelData)
				   	.addComponent(buttonData)
				   	.addComponent(labelSubData1)
				   	.addComponent(labelSubData2)
					.addComponent(tamponData)
				   	.addComponent(buttonResol)
				   	.addComponent(test))
		);
		
		layout.setVerticalGroup(
				layout.createSequentialGroup()
					  .addComponent(labelChoix)
				      .addComponent(choixPb)
				      .addComponent(choixResol)
					  .addComponent(tamponResol)
					  .addComponent(labelData)	  
					  .addComponent(buttonData)
					  .addComponent(labelSubData1)
					  .addComponent(labelSubData2)
					  .addComponent(tamponData)
				      .addComponent(buttonResol)
					  .addComponent(test)
		);
		

		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		
		panelRight = new AffichageVille(m);
		//panelRight.setBackground(Color.WHITE);
		
		panelGlobal.add(panelLeft, BorderLayout.WEST);
		panelGlobal.add(panelRight, BorderLayout.CENTER);
		
		frame.setContentPane(panelGlobal);
		
		frame.setVisible(true);
		while(true)
		{
			frame.validate();
		}
	}
		
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		
	}

}
