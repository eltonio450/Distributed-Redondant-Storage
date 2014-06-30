package Stockage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import Task.taskGiveMeMyPaquet;
import Task.taskRetablirPaquets;
import Utilitaires.Global;
import Utilitaires.Slaver;
import Utilitaires.Utilitaires;



/**
 *
 * Cette classe represente les donnees stockees sur le disque d'une machine
 * 
 * 
 * @author SebastienD
 *
 */

public class Donnees {

  /**
   * La liste de toutes les machines.
   * 
   * Cette liste est protegee par un verrou.
   * @see allServeurLock
   * @see putServer
   * @see removeServeur
   * @see actualiseAllServeur
   * @see getAllServeur
   * @see chooseMAchine
   */
	static private LinkedList<Machine> allServeur = new LinkedList<Machine>();
	
	/**
	 * La liste des machines qui hebergent un paquet d'un m�me groupe qu'un de ses paquets.
	 * 
	 * Cette liste est protgegee par un verrou.
	 * @see interestServeurLock
	 * @see addInterestServeur
	 */
	static private LinkedList<Machine> interestServeur = new LinkedList<Machine>();
	
	/**
	 * Table de hashage des machines qui hebergent de de ses paquets.
	 * La cle correspond a l'identifiant du paquet concerne, la valeur etant l'hote.
	 * Cette table est protegee par un verrou
	 * @see myHostsLock
	 */
	static private HashMap<String, Machine> myHosts = new HashMap<String,Machine>();
	
	/**
	 * Table de hashage des donnees que la machine heberge.
	 * La cle correspond a l'identifiant du paquet.
	 * Cette table est protegee par un verrou.
	 * @see myDataLock
	 * @see removePaquet
	 * @see removeTemporarlyPaquet 
	 * @see putNewPaquet
	 * @see chooseManyPaquetToSend2
	 */
	static private HashMap<String, Paquet> myData = new HashMap<String, Paquet>();
	
	/**
	 * Liste des paquets a envoyer des que possible.
	 * On stocke ici seulement les identifiants des paquets.
	 * Cette liste est protegee par un verrou.
	 * @see toSendASAPLock
	 * @see notEmpty
	 * @see addPaquetToSendAsap
	 * @see addListToSendAsap
	 * @see chosseManyPaquetToSend1
	 */
  static private LinkedBlockingQueue<String> toSendASAP = new LinkedBlockingQueue<String>();

	
	/**
	 * Le nombre de paquet en trop par rapport a la moyenne
	 */
	static public AtomicInteger paquetsEnTrop = new AtomicInteger(0);

	// longueur de la data primaire en bits (ou bytes ?)
	static public long longueur;

	static private boolean filling = true;
	static private LinkedList<Machine> toRemove = new LinkedList<Machine>();
	static private int index = 0;

	/**
	 * La liste des paquets dont je suis proprietaire.
	 * On stocke seulement l'identifiant des paquets.
	 */
	static public LinkedList<String> myOwnData = new LinkedList<String>();


	static public ReentrantLock toSendASAPLock = new ReentrantLock();
	static public Condition notEmpty = toSendASAPLock.newCondition();

	static private ReentrantLock allServeurLock = new ReentrantLock();
	static private ReentrantLock interestServeurLock = new ReentrantLock();
	static private ReentrantLock myHostsLock = new ReentrantLock();
	static private ReentrantLock myDataLock = new ReentrantLock();

	// (Antoine) : le lock est inutile, la liste de mes propres paquets est
	// initialisée au début une bonne fois pour toute.
	// static private ReentrantLock myOwnDataLock= new ReentrantLock ();
	// deprecated
	/*
	 * 
	 * public static void initializeData(LinkedList<String> mesPaquets){
	 * myOwnData = mesPaquets ; }
	 */

	/**
	 * Decide si l'on peut ou non heberger un paquet. 
	 * 
	 * @param id
	 *         L'identifiant du paquet
	 * @return True or False
	 */
	public static boolean acceptePaquet(String id) {
	  Scanner scan = new Scanner(id) ;
	  scan.useDelimiter("-");
	  scan.next() ;
	  if(scan.hasNext()){
	    if(hasPaquetLike(id).isEmpty()){
	      return true ;
	    }
	  }
		return false;
	}

	/**
	 * Retourne la liste des paquets presents dans myData et qui sont du meme groupe que ce paquet.
	 * 
	 * @param ID
	 *       L'identifiant du paquet
	 * @return La liste des paquets du meme groupe
	 */
	public static LinkedList<String> hasPaquetLike(String ID) {
		Scanner s = new Scanner(ID);
		s.useDelimiter("-");
		final String newId = s.next() + "-" + s.next() + "-";
		long n = s.nextLong();
		long a = n / Global.NOMBRESOUSPAQUETS;
		long b = n % Global.NOMBRESOUSPAQUETS;
		LinkedList<String> res = new LinkedList<String>();
		for (int i = 0; i < Global.NOMBRESOUSPAQUETS; i++) {
			String testId = newId + (a * Global.NOMBRESOUSPAQUETS + i);
			if (myData.containsKey(testId)) {
				res.add(testId);
			}
		}
		return res;
	}

	/**
	 * Receptionne un paquet :
	 * ajoute le paquet dans myData, ajoute les serveurs d'interet et lance tackWarnHostHasChanged.
	 * @param p
	 *         Le paquet a receptionner
	 */
	public static void receptionPaquet(Paquet p) {
		//Utilitaires.out("-------------Reception paquet--------------------");
		p.lock();
		for(int i = 0 ; i < Global.NOMBRESOUSPAQUETS ; i ++){
		  if(i != p.power){
		    addInterestServeur(p.otherHosts.get(i)) ;
		  }
		}
		putNewPaquet(p);
		Slaver.giveUrgentTask(new Task.taskWarnHostChanged("" + p.idGlobal), 1);
		
		//p.spreadUnlock();
		//Utilitaires.out("fin reception");
		
	}

	/**
	 * Change l'hote d'un paquet du groupe du paquet d'identifiant <b>Id</b>. Il s'agit du paquet du groupe ayant comme idInterne <b>place</b>. 
	 * @param Id
	 *       Le paquet sur lequel il faut effectuer le changement
	 * @param place
	 *       L'idInterne du paquet du groupe qui a subi un changement d'hote
	 * @param newHost
	 *       Le nouvel hote
	 */
	public static void changeHostForPaquet(String Id, int place, Machine newHost) {
		Utilitaires.out("Change host !");
		myDataLock.lock();
		interestServeurLock.lock();
		try {
			LinkedList<String> paquets = hasPaquetLike(Id);
			for (String s : paquets) {
			  Machine toRemove = myData.get(s).otherHosts.get(place) ;
			  interestServeur.remove(toRemove) ;
				myData.get(s).otherHosts.set(place, newHost);
				
				interestServeur.add(newHost) ;
				myData.get(s).unlock();
			}
		}
		finally {
			myDataLock.unlock();
			interestServeurLock.unlock();
		}
	}

	/**
	 * Adapte les listes allServeur, interestServeurs et myHosts du fait de la mort de la machine m. 
	 * Si on le doit, lance la tache de reconstruction de paquet.
	 * 
	 * @param m
	 *       La machine qui est morte
	 */
	public static void traiteUnMort(Machine m) {
		myHostsLock.lock();
		try {
			for(String id : myHosts.keySet()){
			  if(myHosts.get(id) == m){
			    myHosts.remove(id) ;
			  }
			}
		}
		finally {
			myHostsLock.unlock();
		}
		interestServeurLock.lock();
		try {
			if (interestServeur.contains(m)) {
				interestServeur.remove(m);
				for (Paquet p : myData.values()) {
					for (int i=0 ; i < Global.NOMBRESOUSPAQUETS ; i++) {
						if (m == p.otherHosts.get(i)) {
						  if(p.power == 0) {
	              (new taskRetablirPaquets(p,i)).run() ;				    
						  }
						  else if(p.power== 1 && i==0) {
						    (new taskRetablirPaquets(p,i)).run() ;  
						  }
						}
					}
				}
			}
			while(interestServeur.contains(m)){  //il peut y avoir plusieurs occurrences
			  interestServeur.remove(m) ;
			}
		}
		finally {
			interestServeurLock.unlock();
		}
		allServeurLock.lock();
		try {
			if (!filling)
				allServeur.remove(m);
			else {
				synchronized (toRemove) {
					toRemove.add(m);
				}
			}
		}
		finally {
			allServeurLock.unlock();
		}
	}

	/**
	 * La liste de tous les serveurs
	 * @return une copie de allServeur
	 */
	public static LinkedList<Machine> getAllServeurs() {
		allServeurLock.lock();
		try {
			LinkedList<Machine> buff = new LinkedList<Machine>();
			buff.addAll(allServeur);
			return buff;
		}
		finally {
			allServeurLock.unlock();
		}
	}


	/**
	 * Choisi une machine aleatoirement dans allServeur
	 * @return une machine de allServeur
	 */
	public static Machine chooseMachine() {
	  allServeurLock.lock();
	  try{
	    int n =(int) ( Math.random() * (double) allServeur.size()) ;
	    return allServeur.get(n) ;	    
	  }
	  finally{
	    allServeurLock.unlock();
	  }

	}

	/**
	 * Ajoute une machine a allServeur a partir de l'ip et du port
	 * @param ip
	 *         L'ip de la machine
	 * @param port
	 *         Le port de la machine
	 */
	public static void putServer(String ip, int port) {
		allServeurLock.lock();
		try {
			allServeur.add(new Machine(ip, port));
			Utilitaires.out(ip + ":" + port + " added.");
		}
		finally {
			allServeurLock.unlock();
		}
	}
	
	/**
	 * Ajoute la machine m a la liste allServeur
	 * @param m
	 */
	 public static void putServer(Machine m) {
	    allServeurLock.lock();
	    try {
	      allServeur.add(m);
	      Utilitaires.out(m.ipAdresse + ":" + m.port + " added.");
	    }
	    finally {
	      allServeurLock.unlock();
	    }
	  }

	 /**
	  * Remplace la liste allServeur par cette nouvelle liste.
	  * @param list
	  *        La nouvelle liste
	  */
	public static void actualiseAllServeur(LinkedList<Machine> list) {
		allServeurLock.lock();
		try{
		  allServeur = list;
		}
		finally{
		  allServeurLock.unlock(); 
		}
	}

	/**
	 * Enleve une machine de la liste allServeur
	 * @param m
	 *       la machine a enlever
	 */
	public static void removeServer(Machine m) {
		allServeurLock.lock();
		try{
		  allServeur.remove(m);
		}
		finally{
		  allServeurLock.unlock();
		}
	}

	/**
	 * Regarde si toSendAsap est vide
	 * @return True or False
	 */
	public static boolean toSendAsapEmpty() {
		toSendASAPLock.lock();
		try {
			return toSendASAP.isEmpty();
		}
		finally {
			toSendASAPLock.unlock();
		}
	}

	/**
	 * Ajoute un paquet a toSendASAP en envoyant un signal
	 * @param id
	 *         L'identifiant du paquet a ajouter
	 */
	public static void addPaquetToSendAsap(String id) {
		toSendASAPLock.lock();
		try {
			toSendASAP.add(id);
			notEmpty.signalAll();
		}
		finally {
			toSendASAPLock.unlock();
		}
		//for(String s : toSendASAP)
			//Utilitaires.out("Paquet dans toSendASAP : " + s);
	}

	/**
	 * Ajoute une liste de paquets a toSendASAP
	 * @param listId
	 *         La liste des identifiants
	 */
	public static void addListToSendAsap(LinkedList<String> listId) {
		toSendASAPLock.lock();
		try {
			toSendASAP.addAll(listId);
			notEmpty.signalAll();
		}
		finally {
			toSendASAPLock.unlock();
		}
	}

	/**
	 * Supprime un paquet de toSendASAP
	 * @param id
	 */
	public static void removeToSendAsap(String id) {
		toSendASAPLock.lock();
		try {
			toSendASAP.remove(id);
		}
		finally {
			toSendASAPLock.unlock();
		}
	}
	
	

	/**
	 * Retourne une copie de la liste toSendASAP
	 * @return LinkedList<.String> copie de toSendASAP
	 */
	public static LinkedList<String> chooseManyPaquetToSend1() {
		// TODO :lock
		try {
			LinkedList<String> temp = new LinkedList<String>();
			temp.addAll(toSendASAP);
			return temp;
		}
		finally {
			// unlock
		}
	}

	/**
	 * Retourne une copie de myData
	 * @return LinkedList<.String> copie des identifiants des paquets de myData
	 */
	public static LinkedList<String> chooseManyPaquetToSend2() {
		return new LinkedList(myData.keySet());
	}

	/**
	 * Ajoute cette machine dans interestServeur.
	 * Gere la concurrence
	 * @param m
	 *        La machine e ajouter
	 */
	public static void addInterestServeur(Machine m) {
		interestServeurLock.lock();
		try{
		  interestServeur.add(m);
		}
		finally{
		  interestServeurLock.unlock(); 
		}
	}

	/**
	 * Ajoute ce matching � la table de hashage myHosts
	 * @param id
	 * @param m
	 *     La machine qui heberge le paquet d'identifiant <b>id</b>
	 */
	public static void addHost(String id,Machine m) {
	  myHostsLock.lock();
		try{
		  myHosts.put(id, m) ;
		  //Utilitaires.out("Host added : " + m.toString());
		}
		finally{
		  myHostsLock.unlock();
		}

	}

	/**
	 * Retourne ce paquet de myData sans le supprimer
	 * @param id
	 * @return Le paquet
	 */
	public static Paquet getHostedPaquet(String id) {
		myDataLock.lock();
		Paquet temp;
		try {
			temp = myData.get(id);
			if(temp!=null)
				;//Utilitaires.out("Le paquet "+id+"est bien présent chez moi.",1,true);
			else
				Utilitaires.out("Le paquet "+id+" n'est pas présent chez moi.",1,true);
		}
		finally {
			myDataLock.unlock();
		}
		return temp;
	} 

	/**
	 * Supprime et retourne ce paquet de myData. Cependant, aucun ajustement n'est effectue dans interestServeur.
	 * @param id
	 * @return Le paquet s'il est dans myData, <b>null</b> sinon.
	 */
	public static Paquet removeTemporarlyPaquet(String id) {
		myDataLock.lock();
		try {
			if (myData.containsKey(id)) {
				return myData.remove(id);
			}
			else {
				return null;
			}
		}
		finally {
			myDataLock.unlock();
		}
	}

	/**
	 * Ajoute ce paquet � myData.
	 * Gere la concurrence
	 * @param p
	 *         Le paquet
	 */
	public static void putNewPaquet(Paquet p) {
		myDataLock.lock();
		try{
		  myData.put(p.idGlobal, p);
		}
		finally{
		  myDataLock.unlock();
		}
	}
	
	/**
	 * Recupere, si possible, ses propres donn�es et les ajoute dans myData
	 */
	public static void recupereMyOwnData(){
	  for(String id : myHosts.keySet()){
	    myHostsLock.lock();
	    try{
	      Machine host = myHosts.get(id);
	      (new taskGiveMeMyPaquet(id,host)).run() ;
	    }
	    finally{
	      myHostsLock.unlock();
	    }
	  }
	}

	public static void genererPaquetsSecurite(ArrayList<Paquet> tableau) {
		Paquet p = new Paquet(tableau.get(0).idMachine + Global.NOMBRESOUSPAQUETS - 1, Global.MYSELF);
		tableau.add(Global.NOMBRESOUSPAQUETS - 1, p);

		try {
			int temp = 0;
			ByteBuffer b = ByteBuffer.allocate(1);
			for (long j = 0; j < Global.PAQUET_SIZE; j++) {
				for (int i = 0; i < Global.NOMBRESOUSPAQUETSSIGNIFICATIFS; i++) {

					b.clear();
					// if(tableau.get(i).fichier.isOpen())
					// Utilitaires.out("Chack !");
					tableau.get(i).fichier.read(b);
					b.flip();
					// Global.debug(i);
					temp += (int) b.get(0);
				}
				b.clear();
				temp %= 256;
				b.put((byte) temp);
				b.flip();

				tableau.get(Global.NOMBRESOUSPAQUETS - 1).fichier.write(b);
			}

			for (int i = 0; i < Global.NOMBRESOUSPAQUETS; i++)
				tableau.get(i).remettrePositionZero();

		}
		catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Affiche la liste des machines dans allServeur.
	 * Attention cette methode n'utilise pas les verrous !
	 */
	public static void printServerList() {
		Utilitaires.out("Liste des serveurs :", 1, true);
		for (Machine m : allServeur)
			Utilitaires.out("s : " + m.toString(), 1, false);

	}

	/**
	 * Supprime un paquet de myData, en actualisant egalement toSendASAP et interestServeur.
	 * @param p
	 *       Le paquet � supprimer
	 */
	public static void removePaquet(Paquet p) {
		//TODO : remove interestServeur
	  myDataLock.lock();
		toSendASAPLock.lock();
		interestServeurLock.lock();
		try {
			myData.remove(p.idGlobal);
			toSendASAP.remove(p.idGlobal);
			for(int i = 0 ; i < Global.NOMBRESOUSPAQUETS ; i ++){
			  if(i != p.power){
			    interestServeur.remove(p.otherHosts.get(i)) ;
			  }
			}
		}
		finally {
			myDataLock.unlock();
			toSendASAPLock.unlock();
		}
	}

	public static void fillingServers(boolean flag) {
		filling = flag;
		if (flag = false) {
			synchronized (toRemove) {
				for (Machine m : toRemove) {
					traiteUnMort(m);
				}
				toRemove.clear();
			}
		}
	}

	public static InetSocketAddress getRemote() {
		allServeurLock.lock();
		try {
			index++;
			if (allServeur.isEmpty())
				return null;
			index %= allServeur.size();
			Machine m = allServeur.get(index);
			if (m.ipAdresse.equals(Global.MYSELF.ipAdresse) && m.port == Global.MYSELF.port)
				return null;
			return new InetSocketAddress(m.ipAdresse, m.port + 2);

		}
		finally {
			allServeurLock.unlock();
		}
	}
	
	/**
	 * Attend qu'un paquet soit ajoute � toSendASAP
	 */
	public static void waitForSomethingInToSendASAP(){
		try {
			toSendASAP.put(toSendASAP.take());
		}
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
