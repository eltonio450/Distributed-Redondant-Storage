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
  
  
  public static Donnees initConnection(Machine m,String mesDonnees){  
    //se connecte à une Machine m connue, initialise un objet données avec les champs allServeur et voisins. Ses propres données sont stockées dans myOwnData
    
    //on connait une machine - on veut stocker les donnees dans le fichier de chemin mesDonnees
    LinkedList<Machine> serveurs = getAllServeurs(m) ;
    LinkedList<ArrayList<Paquet>> mesPaquets = Paquet.fileToPaquets(mesDonnees) ;
    Donnees data = new Donnees(mesPaquets) ;
    data.actualiseAllServeur(serveurs) ;
    data.actualiseNeighbours(chooseNeighbours()) ;
    return data ;
  }
  
  public static void initPartage(Donnees data){
    ArrayList<Paquet> lp = data.firstOwnData() ;
    while(lp != null){
      ArrayList<Machine> hosts = chooseMachines(5) ;
      SendPaquet.envoieData(data,lp,hosts) ;
      lp = data.firstOwnData() ;
    }
  }
  

}
