package Stockage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;


public class Donnees {

	private LinkedList<Machine> allServeur ;
	private HashSet<Machine> interestServeur ;
	private HashSet<Machine> neighbours ;
	private LinkedList<Machine> myHosts ;
	private HashMap<Long,Paquet> myData ;
	private LinkedList<ArrayList<Paquet>> myOwnData ;

	private ReentrantLock allServeurLock ;
	private ReentrantLock interestServeurLock;
	private ReentrantLock neighboursLock;
	private ReentrantLock myHostsLock;
	private ReentrantLock myDataLock;
	private ReentrantLock myOwnDataLock;

	Donnees(LinkedList<ArrayList<Paquet>> mesPaquets) {
		allServeur = new LinkedList<Machine>() ;
		interestServeur = new HashSet<Machine>() ;
		neighbours = new HashSet<Machine>() ;
		myData = new HashMap<Long,Paquet>() ;
		myOwnData = mesPaquets ;

		allServeurLock = new ReentrantLock ();
		interestServeurLock = new ReentrantLock ();
		neighboursLock = new ReentrantLock ();
		myHostsLock = new ReentrantLock ();
		myDataLock = new ReentrantLock ();
		myOwnDataLock = new ReentrantLock ();
	}

	public void receptionPaquet(Machine m, Paquet p){
		addInterestServeur(m) ;
		myData.put(p.id, p) ;
		for(Machine n : p.otherHosts){
			SendPaquet.prevenirHostChanged(m,p.id) ;
		}
	}
	public static Boolean verifieMort(Machine m){
    //envoie un message ‡ m pour vÈrifier qu'il est bien mort
    return null ;
  }
  
  public void traiteUnMort(Machine m){
    if(myHosts.contains(m)){
      if(verifieMort(m)){
        myHosts.remove(m) ;
      }
    }
    if (interestServeur.contains(m) && verifieMort(m)){
      interestServeur.remove(m) ;
      for (Paquet p : myData) {
        for(Machine n : p.otherHosts){
          if(m==n) {
            //check avec p.poxer si on doit intervenir ou non
            //Èventuellement, rÈtablir le paquet
          }
        }
      }
    }
  }

	public void actualiseAllServeur(LinkedList<Machine> l){
		allServeurLock.lock();
		allServeur = l ;
		allServeurLock.unlock();
	}

	public void removeServer (Machine m) {
		allServeurLock.lock();
		allServeur.remove(m);
		allServeurLock.unlock();
		
		// TODO : G√©rer la perte d'un voisin etc. 
		// PB. : il faut se reprendre un voisin quand on en perd un
		// 		 il faut g√©rer la r√©cup√©ration si c'est un interestServeur
		//		 etc.
	}

	public void actualiseNeighbours(HashSet<Machine> voisins){
		neighboursLock.lock();
		neighbours = voisins;
		neighboursLock.unlock();
	}

	public ArrayList<Paquet> firstOwnData(){
		myOwnDataLock.lock();
		ArrayList<Paquet> retour = myOwnData.peek() ;
		myOwnDataLock.unlock();
		return retour;
	}

	public void addInterestServeur(Machine m){
		interestServeurLock.lock();
		interestServeur.add(m) ;
		interestServeurLock.unlock();
	}

	public void addHost(Machine m){
		myHostsLock.lock();
		myHosts.add(m) ;
		myHostsLock.unlock();
	}
}
