package Stockage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

public class Stockage {

	public static LinkedList<Machine> getAllServeurs(Machine m){ //TODO
		return null ;
	}

	public static HashSet<Machine> chooseNeighbours(){ //TODO
		return null ;
	}

	public static ArrayList<Machine> chooseMachines(int n){   //TODO
		//doit renvoyer n machines at random
		return null ;
	}


	public static void initConnection(Machine m,String mesDonnees){  
		//se connecte � une Machine m connue, initialise un objet donn�es avec les champs allServeur et voisins. Ses propres donn�es sont stock�es dans myOwnData
		//on connait une machine - on veut stocker les donnees dans le fichier de chemin mesDonnees
	  // TODO : ouvrir une socket
		LinkedList<Machine> serveurs = getAllServeurs(m) ;
		LinkedList<ArrayList<Paquet>> mesPaquets = Paquet.fileToPaquets(mesDonnees) ;
		Donnees.initializeData(mesPaquets);
		Donnees.actualiseAllServeur(serveurs) ;
		Donnees.actualiseNeighbours(chooseNeighbours()) ;
	}

	public static void initPartage(){
		ArrayList<Paquet> lp = Donnees.firstOwnData() ;
		while(lp != null){
			ArrayList<Machine> hosts = chooseMachines(5) ;
			SendPaquet.envoieData(lp,hosts) ;
			lp = Donnees.firstOwnData() ;
		}
	}
}
