package Stockage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

public class Stockage {

  public static LinkedList<Machine> getAllServeurs(Machine m){
    return null ;
  }
  
  public static HashSet<Machine> chooseNeighbours(){
    return null ;
  }
  
  public static ArrayList<Machine> chooseMachines(int n){   //doit renvoyer n machines at random
    return null ;
  }
  
  public static Paquet putAndGet(Machine m, Paquet p){
    return null
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
    while(data.firstOwnData() != null){
      ArrayList hosts = chooseMachines(5) ;
      envoieData(data,hosts) ;
    }
  }
  
  public static void envoieData(Donnees data, ArrayList hosts){
    
  }
}
