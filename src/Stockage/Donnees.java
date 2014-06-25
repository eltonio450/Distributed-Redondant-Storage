
package Stockage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;


public class Donnees {

	static private LinkedList<Machine> allServeur = new LinkedList<Machine>();
	static private HashSet<Machine> interestServeur = new HashSet<Machine>();
	static private LinkedList<Machine> myHosts = new LinkedList<Machine>();
	static private HashMap<String,Paquet> myData = new HashMap<String,Paquet>();
	
	//longueur de la data primaire en bits (ou bytes ?)
	static public long longueur;
	
	//passage en public (cf la remarque sur le lock)
	static public LinkedList<String> myOwnData = new LinkedList<String>() ;
	static public LinkedList<String> toSendASAP = new LinkedList<String>();
	
	static private ReentrantLock allServeurLock = new ReentrantLock ();
	static private ReentrantLock interestServeurLock= new ReentrantLock ();
	static private ReentrantLock myHostsLock= new ReentrantLock ();
	static private ReentrantLock myDataLock= new ReentrantLock ();
	
	//(Antoine) : le lock est inutile, la liste de mes propres paquets est initialisée au début une bonne fois pour toute.
	//static private ReentrantLock myOwnDataLock= new ReentrantLock ();

	public static void receptionPaquet(Machine m, Paquet p){
		addInterestServeur(m) ;
		putNewPaquet(p) ;
		SendPaquet.prevenirHostChanged(p.id) ; //TODO : faire une t�che et l� donner � un slave
	}
	
  public static Paquet selectPaquetToSend() {
    //for the task taskServeurExchange : should choose a random package to exchange for a first connection
    //TODO : is it a reasonable choice ?
    return (Paquet) myData.values().toArray()[0] ;
  }	
  
	// TODO : implement this and put it in an other place
  public static Boolean verifieMort(Machine m){
    //envoie un message � m pour v�rifier qu'il est bien mort
    return null ;
  }
  
  
  public static void changeHostForPaquet(String Id, int place, Machine newHost){
    myDataLock.lock();
    try{
      myData.get(Id).otherHosts.set(place, newHost) ;
    }
    finally{
      myDataLock.unlock();
    }
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
            //�ventuellement, r�tablir le paquet
          }
        }
      }
    }
  }
  
  public static LinkedList<Machine> getAllServeurs () {
	  allServeurLock.lock();
	  try {
		  LinkedList<Machine> buff = new LinkedList<Machine> ();
		  buff.addAll(allServeur);
		  return buff;
	  } finally {
		  allServeurLock.unlock();
	  }
  }
  
  public static void putServer (String ip, int port) {
	  allServeurLock.lock();
	  try {
		  allServeur.add(new Machine(ip, port));
	  } finally {
		  allServeurLock.unlock();
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
		
		// TODO : Gerer la perte d'un voisin etc. 
		// PB. : il faut se reprendre un voisin quand on en perd un
		// 		 il faut gerer la recuperation si c'est un interestServeur
		//		 etc.
	}

	public static Paquet choosePaquetToSend(){
	  //TODO
	  return null ;
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
	
	public static Paquet getHostedPaquet(String id){
	  myDataLock.lock();
	  try{
	    return myData.get(id) ;
	  }
	  finally {
	    myDataLock.unlock();
	  }
	}
	
	public static void putNewPaquet(Paquet p) {
	  myDataLock.lock();
	  myData.put(p.id, p) ;
	  myDataLock.unlock();
	}
	
	public static void genererPaquetsSécurité(ArrayList<Paquet> tableau)
	{
		//génère les paquets supplémentaire dans le tableau. Algo à faire
	}
}

