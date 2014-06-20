package Debug;

import javax.swing.*;

public class CentreDeControle {

	public static void main(String[] args) {
		JFrame fenetre = new JFrame();
        
	    //Définit un titre pour notre fenêtre
	    fenetre.setTitle("Ma première fenêtre Java");
	    //Définit sa taille : 400 pixels de large et 100 pixels de haut
	    fenetre.setSize(400, 100);
	    //Nous demandons maintenant à notre objet de se positionner au centre
	    fenetre.setLocationRelativeTo(null);
	    //Termine le processus lorsqu'on clique sur la croix rouge
	    fenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    //Et enfin, la rendre visible        
	    fenetre.setVisible(true);
	       
	}
	

}
