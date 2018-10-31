/**
 * 
 */
package Interface;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

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
	private static JPanel panelRight;
	private static JComboBox<String> choixPb;
	private static JComboBox<String> choixResol;
	private static JButton buttonData;
	private static JButton buttonResol;
	private static JLabel labelChoix;
	private static JLabel labelData;
	private static JLabel labelSubData;
	
	private static void createNewJFrame()
	{

		frame = new JFrame("Stochastique");
		frame.setSize(600, 500);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		panelGlobal = new JPanel();
		panelGlobal.setLayout(new BorderLayout());
		
		labelChoix = new JLabel("Choix de resolution");
		
		String[] pb = { "Probleme du Voyageur de Commerce deterministe", "Probleme du Voyageur de Commerce stochastique"};
		choixPb = new JComboBox<String>(pb);
		
		String[] resol = { "CPlex", "Recuit simule"};
		choixResol = new JComboBox<String>(resol);
		
		labelData = new JLabel("Choix des donnees");
		
		buttonData = new JButton("Selectionner un fichier de donnees");
		
		labelSubData = new JLabel("Fichier selectionne :");
		
		buttonResol = new JButton("Résoudre");
	
		buttonResol.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
					
			}
		});
		
		
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
				   	.addComponent(labelData)
				   	.addComponent(buttonData)
				   	.addComponent(labelSubData)
				   	.addComponent(buttonResol))
		);
		
		layout.setVerticalGroup(
				layout.createSequentialGroup()
					  .addComponent(labelChoix)
				      .addComponent(choixPb)
				      .addComponent(choixResol)	
					  .addComponent(labelData)	  
					  .addComponent(buttonData)
					   	.addComponent(labelSubData)
				      .addComponent(buttonResol)
		);
		

		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		
		panelRight = new JPanel();
		panelRight.setBackground(Color.WHITE);
		
		
		panelGlobal.add(panelLeft, BorderLayout.WEST);
		panelGlobal.add(panelRight, BorderLayout.CENTER);
		
		frame.setContentPane(panelGlobal);
		
		frame.setVisible(true);
	}
		
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		
		createNewJFrame();
	}

}
