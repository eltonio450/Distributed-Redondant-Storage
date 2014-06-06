package modal;

import java.util.LinkedList;

import projetModal.Machine;
import projetModal.Paquet;

public class Paquet {

  long id ;
  int power ;
  LinkedList<Machine> otherHosts ;
  Machine owner ;
  
  Paquet(long Id, int p , LinkedList<Machine> others, Machine proprio) {
    id = Id ;
    power = p ;
    otherHosts = others ;
    owner = proprio ;
  }
  
  public LinkedList<LinkedList<Paquet>> fileToPaquets(String path){  //doit découper un fichier en liste de groupes de (4+1) paquets
    return null ;
  }
  
}