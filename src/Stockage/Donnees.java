package Stockage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;


public class Donnees {

  private LinkedList<Machine> allServeur ;
  private HashSet<Machine> interestServeur ;
  private HashSet<Machine> neighbours ;
  private LinkedList<Machine> myHosts ;
  private HashMap<Long,Paquet> myData ;
  private LinkedList<ArrayList<Paquet>> myOwnData ;
  
  Donnees(LinkedList<ArrayList<Paquet>> mesPaquets) {
    allServeur = new LinkedList<Machine>() ;
    interestServeur = new HashSet<Machine>() ;
    neighbours = new HashSet<Machine>() ;
    myData = new HashMap<Long,Paquet>() ;
    myOwnData = mesPaquets ;
  }
  
  public void actualiseAllServeur(LinkedList<Machine> l){
    allServeur = l ;
  }
  
  public void actualiseNeighbours(HashSet<Machine> voisins){
    neighbours = voisins;
  }
  
  public ArrayList<Paquet> firstOwnData(){
    return myOwnData.peek() ;
  }
}
