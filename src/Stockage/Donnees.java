
package Stockage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;


public class Donnees {

	static private LinkedList<Machine> allServeur = new LinkedList<Machine>();
	static private HashSet<Machine> interestServeur = new HashSet<Machine>();
	static private HashSet<Machine> neighbours = new HashSet<Machine>();
	static private LinkedList<Machine> myHosts = new LinkedList<Machine>();
	static private HashMap<Long,Paquet> myData = new HashMap<Long,Paquet>();
	static private LinkedList<ArrayList<Paquet>> myOwnData = new LinkedList<ArrayList<Paquet>>() ;

	static private ReentrantLock allServeurLock = new ReentrantLock ();
	static private ReentrantLock interestServeurLock= new ReentrantLock ();
	static private ReentrantLock neighboursLock= new ReentrantLock ();
	static private ReentrantLock myHostsLock= new ReentrantLock ();
	static private ReentrantLock myDataLock= new ReentrantLock ();
	static private ReentrantLock myOwnDataLock= new ReentrantLock ();

	public static void initializeData(LinkedList<ArrayList<Paquet>> mesPaquets){
	  myOwnData = mesPaquets ;
	}

	public static void receptionPaquet(Machine m, Paquet p){
		addInterestServeur(m) ;
		Donnees.myData.put(p.id, p) ;
		for(Machine n : p.otherHosts){
			SendPaquet.prevenirHostChanged(p.id) ;
		}
	}
	public static Boolean verifieMort(Machine m){
    //envoie un message ‡ m pour vÈrifier qu'il est bien mort
    return null ;
  }
  
  public static void traiteUnMort(Machine m){
    if(myHosts.contains(m)){
      if(verifieMort(m)){
        myHosts.remove(m) ;
      }
    }
    if (interestServeur.contains(m) && verifieMort(m)){
      interestServeur.remove(m) ;
      for (Paquet p : myData.values()) {
        for(Machine n : p.otherHosts){
          if(m==n) {
            //check avec p.power si on doit intervenir ou non
            //Èventuellement, rÈtablir le paquet
          }
        }
      }
    }
  }

	public static void actualiseAllServeur(LinkedList<Machine> l){
		allServeurLock.lock();
		allServeur = l ;
		allServeurLock.unlock();
	}

	public static void removeServer (Machine m) {
		allServeurLock.lock();
		allServeur.remove(m);
		allServeurLock.unlock();
		
		// TODO : G√©rer la perte d'un voisin etc. 
		// PB. : il faut se reprendre un voisin quand on en perd un
		// 		 il faut g√©rer la r√©cup√©ration si c'est un interestServeur
		//		 etc.
	}

	public static void actualiseNeighbours(HashSet<Machine> voisins){
		neighboursLock.lock();
		neighbours = voisins;
		neighboursLock.unlock();
	}

	public static ArrayList<Paquet> firstOwnData(){
		myOwnDataLock.lock();
		ArrayList<Paquet> retour = myOwnData.peek() ;
		myOwnDataLock.unlock();
		return retour;
	}

	public static void addInterestServeur(Machine m){
		interestServeurLock.lock();
		interestServeur.add(m) ;
		interestServeurLock.unlock();
	}

	public static void addHost(Machine m){
		myHostsLock.lock();
		myHosts.add(m) ;
		myHostsLock.unlock();
	}
	
	public static Paquet getHostedPaquet(Long Id){
	  return myData.get(Id) ;
	}
}

