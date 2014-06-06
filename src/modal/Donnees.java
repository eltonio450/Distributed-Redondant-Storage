package modal;


import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;


public class Donnees {

  private LinkedList<Machine> allServeur ;
  private HashSet<Machine> interestServeur ;
  private HashSet<Machine> neighbours ;
  private LinkedList<Machine> myHosts ;
  private HashMap<Long,Paquet> myData ;
  private LinkedList<Paquet> myOwnData ;
  
  Donnees() {
    allServeur = new LinkedList<Machine>() ;
    interestServeur = new HashSet<Machine>() ;
    neighbours = new HashSet<Machine>() ;
    myData = new HashMap<Long,Paquet>() ;
    myOwnData = new LinkedList<Paquet>() ;
  }
  
  
}
