package Stockage;

import java.util.ArrayList;
import java.util.LinkedList;

public class Paquet {

  long id ;
  int power ;
  LinkedList<Machine> otherHosts ;
  Machine owner ;
  
  Paquet(long Id, int p , Machine proprio) {
    id = Id ;
    power = p ;
    owner = proprio ;
  }
  
  public void putPower(int p){
    power = p ;
  }
  
  public void putOtherHosts(ArrayList<Machine> liste){
    int n = liste.size() ;
    LinkedList<Machine> l = new LinkedList<Machine>() ;
    for (int j=0; j< n; j++){
      l.add(liste.get(j)) ;
    }
    otherHosts = l ;
  }
  
  public static LinkedList<ArrayList<Paquet>> fileToPaquets(String path){  
    //doit découper un fichier en liste de groupes de (4+1) paquets
    // doit initialiser les champs : id, power et proprio
    return null ;
  }
  
}