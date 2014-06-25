package Stockage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

public class Stockage {

	public static LinkedList<Machine> getAllServeurs(Machine m){ //TODO
		return null ;
	}

	public static Machine chooseMachine(){   //TODO
		//doit renvoyer 1 machine at random
		return null ;
	}


	public static void initConnection(Machine m,String mesDonnees){  
		//se connecte � une Machine m connue, initialise un objet donn�es avec les champs allServeur et voisins. Ses propres donn�es sont stock�es dans myOwnData
		//on connait une machine - on veut stocker les donnees dans le fichier de chemin mesDonnees
	  // TODO : ouvrir une socket
		LinkedList<Machine> serveurs = getAllServeurs(m) ;
		LinkedList<ArrayList<Paquet>> mesPaquets = Paquet.fileToPaquets(mesDonnees) ;
		Donnees.actualiseAllServeur(serveurs) ;
	}

	public static void initPartage(){
		Paquet p = Donnees.choosePaquetToSend() ;
		while(p != null){
			try {
			  //TODO
			}
			catch(Exception e) {
			  //TODO : do something smart
			}
		}
	}
	
}
