package Stockage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import Task.taskRetablirPaquets;
import Utilitaires.Global;
import Utilitaires.Utilitaires;
import Utilitaires.Slaver;



/**
 *
 * Cette classe repr�sente les donn�es stock�es sur le disque d'une machine
 * 
 * 
 * @author SebastienD
 *
 */

public class Donnees {

  /**
   * La liste de toutes les machines.
   * 
   * Cette liste est prot�g�e par un verrou.
   * @see allServeurLock
   * @see putServer
   * @see removeServeur
   * @see actualiseAllServeur
   * @see getAllServeur
   * @see chooseMAchine
   */
	static private LinkedList<Machine> allServeur = new LinkedList<Machine>();
	
	/**
	 * La liste des machines qui h�bergent un paquet d'un m�me groupe qu'un de ses paquets.
	 * 
	 * Cette liste est protg�e par un verrou.
	 * @see interestServeurLock
	 * @see addInterestServeur
	 */
	static private LinkedList<Machine> interestServeur = new LinkedList<Machine>();
	
	/**
	 * Table de hashage des machines qui h�bergent de de ses paquets.
	 * La cl� correspond � l'identifiant du paquet concern�e, la valeur �tant l'h�te.
	 * Cette table est prot�g�e par un verrou
	 * @see myHostsLock
	 */
	static private HashMap<String, Machine> myHosts = new HashMap<String,Machine>();
	
	/**
	 * Table de hashage des donn�es que la machine h�berge.
	 * La cl� correspond � l'identifiant du paquet.
	 * Cette table est prot�g�e par un verrou.
	 * @see myDataLock
	 * @see removePaquet
	 * @see removeTemporarlyPaquet 
	 * @see putNewPaquet
	 * @see chooseManyPaquetToSend2
	 */
	static private HashMap<String, Paquet> myData = new HashMap<String, Paquet>();
	
	/**
	 * Liste des paquets � envoyer d�s que possible.
	 * On stocke ici seulement les identifiants des paquets.
	 * Cette liste est prot�g�e par un verrou.
	 * @see toSendASAPLock
	 * @see notEmpty
	 * @see addPaquetToSendAsap
	 * @see addListToSendAsap
	 * @see chosseManyPaquetToSend1
	 */
  static private LinkedBlockingQueue<String> toSendASAP = new LinkedBlockingQueue<String>();

	
	/**
	 * Le nombre de paquet en trop par rapport � la moyenne
	 */
	static public AtomicInteger paquetsEnTrop = new AtomicInteger(0);

	// longueur de la data primaire en bits (ou bytes ?)
	static public long longueur;

	static private boolean filling = true;
	static private LinkedList<Machine> toRemove = new LinkedList<Machine>();
	static private int index = 0;

	/**
	 * La liste des paquets dont je suis propri�taire.
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
	 * D�cide si l'on peut ou non h�berger un paquet. 
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
	 * Retourne la liste des paquets pr�sents dans myData et qui sont du m�me groupe que ce paquet.
	 * 
	 * @param ID
	 *       L'identifiant du paquet
	 * @return La liste des paquets du m�me groupe
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
	 * R�ceptionne un paquet :
	 * ajoute le paquet dans myData, ajoute les serveurs d'int�r�t et lance tackWarnHostHasChanged.
	 * @param p
	 *         Le paquet � r�ceptionner
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
		p.spreadUnlock();
		//Utilitaires.out("fin reception");
		
	}

	/**
	 * Change l'h�te d'un paquet du groupe du paquet d'identifiant <b>Id</b>. Il s'agit du paquet du groupe ayant comme idInterne <b>place</b>. 
	 * @param Id
	 *       Le paquet sur lequel il faut effectuer le changement
	 * @param place
	 *       L'idInterne du paquet du groupe qui a subi un changement d'h�te
	 * @param newHost
	 *       Le nouvel h�te
	 */
	public static void changeHostForPaquet(String Id, int place, Machine newHost) {
		Utilitaires.out("Change host !");
		myDataLock.lock();
		interestServeurLock.lock();
		try {
			LinkedList<String> paquets = hasPaquetLike(Id);
			for (String s : paquets) {
			  Machine toRemove = myData.get(Id).otherHosts.get(place) ;
			  interestServeur.remove(toRemove) ;
				myData.get(Id).otherHosts.set(place, newHost);
				interestServeur.add(newHost) ;
			}
		}
		finally {
			myDataLock.unlock();
			interestServeurLock.unlock();
		}
	}

	/**
	 * Adapte les listes allServeur, interestServeurs et myHosts du fait de la mort de la machine m. 
	 * Si on le doit, lance la t�che de reconstruction de paquet.
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

	
	public static Machine chooseMachine() {
		 //pour test1 : 
	  //return(new Machine("127.0.0.1",5004)) ;
		return allServeur.peek();
	}

	
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

	public static void actualiseAllServeur(LinkedList<Machine> l) {
		allServeurLock.lock();
		try{
		  allServeur = l;
		}
		finally{
		  allServeurLock.unlock(); 
		}
	}

	public static void removeServer(Machine m) {
		allServeurLock.lock();
		try{
		  allServeur.remove(m);
		}
		finally{
		  allServeurLock.unlock();
		}

		// TODO : Gerer la perte d'un voisin etc.
		// PB. : il faut se reprendre un voisin quand on en perd un
		// il faut gerer la recuperation si c'est un interestServeur
		// etc.
	}

	/*
	 * plus utile ! public static Paquet choosePaquetToSend() { if
	 * (toSendASAP.isEmpty()) { return (Paquet) myData.values().toArray()[0]; }
	 * else { return (myData.get(toSendASAP.getFirst())); } }
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

	public static void removeToSendAsap(String id) {
		toSendASAPLock.lock();
		try {
			toSendASAP.remove(id);
		}
		finally {
			toSendASAPLock.unlock();
		}
	}

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

	public static LinkedList<String> chooseManyPaquetToSend2() {
		return new LinkedList(myData.keySet());
	}

	public static void addInterestServeur(Machine m) {
		interestServeurLock.lock();
		try{
		  interestServeur.add(m);
		}
		finally{
		  interestServeurLock.unlock(); 
		}
	}

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

	
	public static Paquet getHostedPaquet(String id) {
		myDataLock.lock();
		Paquet temp;
		try {
			temp = myData.get(id);
			if(temp!=null)
				Utilitaires.out("Le paquet "+id+"est bien présent chez moi.",1,true);
			else
				Utilitaires.out("Le paquet "+id+" n'est pas présent chez moi.",1,true);
		}
		finally {
			myDataLock.unlock();
		}
		return temp;
	} 

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

	public static void putNewPaquet(Paquet p) {
		myDataLock.lock();
		try{
		  myData.put(p.idGlobal, p);
		}
		finally{
		  myDataLock.unlock();
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

	public static void printServerList() {
		Utilitaires.out("Liste des serveurs :", 1, true);
		for (Machine m : allServeur)
			Utilitaires.out("s : " + m.toString(), 1, false);

	}

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
