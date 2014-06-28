package Stockage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import Utilitaires.Global;
import Utilitaires.Utilitaires;
import Utilitaires.Slaver;

public class Donnees {

	static private LinkedList<Machine> allServeur = new LinkedList<Machine>();
	static private HashSet<Machine> interestServeur = new HashSet<Machine>();
	static private LinkedList<Machine> myHosts = new LinkedList<Machine>();
	static private HashMap<String, Paquet> myData = new HashMap<String, Paquet>();
	
	static public AtomicInteger paquetsEnTrop = new AtomicInteger(0) ;

	// longueur de la data primaire en bits (ou bytes ?)
	static public long longueur;

	static private boolean filling = true;
	static private LinkedList<Machine> toRemove = new LinkedList<Machine>();
	static private int index = 0;

	// passage en public (cf la remarque sur le lock)
	static public LinkedList<String> myOwnData = new LinkedList<String>();

	static private LinkedList<String> toSendASAP = new LinkedList<String>();
	static public ReentrantLock toSendASAPLock = new ReentrantLock() ;
	static public Condition notEmpty =  toSendASAPLock.newCondition() ;
	
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

	public static boolean acceptePaquet(String s) {
		// TODO Auto-generated method stub
		return false;
	}

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

	public static void receptionPaquet(Machine m, Paquet p) {
	  Utilitaires.out("-------------Reception paquet--------------------"); 
		addInterestServeur(m);
		putNewPaquet(p);
		Slaver.giveUrgentTask(new Task.taskWarnHostChanged("" + p.idGlobal), 1);
		Utilitaires.out("fin reception"); 
	}

	

	public static void changeHostForPaquet(String Id, int place, Machine newHost) {
	  Utilitaires.out("Change host !") ;
		myDataLock.lock();
		try {
			LinkedList<String> paquets = hasPaquetLike(Id);
			for (String s : paquets) {
				myData.get(Id).otherHosts.set(place, newHost);
			}
		}
		finally {
			myDataLock.unlock();
		}
	}

	public static void traiteUnMort(Machine m) {
		myHostsLock.lock();
		try {
			if (myHosts.contains(m)) {
				myHosts.remove(m);
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
					for (Machine n : p.otherHosts) {
						if (m == n) {
							// check avec p.power si on doit intervenir ou non
							// �ventuellement, r�tablir le paquet
						}
					}
				}
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
	  //pour test1 : return(new Machine("127.0.0.1",5004)) ;
		return allServeur.peek();
	}

	public static void putServer(String ip, int port) {
		Utilitaires.out("Putting server " + ip + ":" + port);
		allServeurLock.lock();
		try {
			allServeur.add(new Machine(ip, port));
		}
		finally {
			allServeurLock.unlock();
		}
	}

	public static void actualiseAllServeur(LinkedList<Machine> l) {
		allServeurLock.lock();
		allServeur = l;
		allServeurLock.unlock();
	}

	public static void removeServer(Machine m) {
		allServeurLock.lock();
		allServeur.remove(m);
		allServeurLock.unlock();

		// TODO : Gerer la perte d'un voisin etc.
		// PB. : il faut se reprendre un voisin quand on en perd un
		// il faut gerer la recuperation si c'est un interestServeur
		// etc.
	}

	/* plus utile !
	public static Paquet choosePaquetToSend() {
		if (toSendASAP.isEmpty()) {
			return (Paquet) myData.values().toArray()[0];
		}
		else {
			return (myData.get(toSendASAP.getFirst()));
		}
	} */

	public static boolean toSendAsapEmpty(){
	  toSendASAPLock.lock() ;
	  try{
	    return toSendASAP.isEmpty() ;
	  }
	  finally{
	    toSendASAPLock.unlock() ;
	  }
	}
	
	
	public static void addPaquetToSendAsap(String id){
  	toSendASAPLock.lock();
  	try{
  	  toSendASAP.add(id) ;
  	  notEmpty.signalAll();
  	  }
  	finally{
  	  toSendASAPLock.unlock();
  	}
	}
	
	public static void addListToSendAsap(LinkedList<String> listId){
	  toSendASAPLock.lock();
	  try{
	    toSendASAP.addAll(listId) ;
	    notEmpty.signalAll();
	    }
	  finally{
	    toSendASAPLock.unlock();
	  }
	}
	
	
	public static void removeToSendAsap(String id){
  	toSendASAPLock.lock();
    try{
      toSendASAP.remove(id) ;
      }
    finally{
      toSendASAPLock.unlock();
    }
  }
	
	public static LinkedList<String> chooseManyPaquetToSend1() {
	  //TODO :lock
	  try{
	    LinkedList<String> temp = new LinkedList<String>() ;
	    temp.addAll(toSendASAP) ;
	    return temp;
	  }
	  finally{
	    //unlock
	  }
	}

	public static LinkedList<String> chooseManyPaquetToSend2() {
		return new LinkedList(myData.keySet());
	}

	public static void addInterestServeur(Machine m) {
		interestServeurLock.lock();
		interestServeur.add(m);
		interestServeurLock.unlock();
	}
	

	public static void addHost(Machine m) {
		myHostsLock.lock();
		myHosts.add(m);
		Utilitaires.out(m.ipAdresse + ":" + m.port + " added.");
		myHostsLock.unlock();
	}

	public static Paquet getHostedPaquet(String id) {
		myDataLock.lock();
		try {
			return myData.get(id);
		}
		finally {
			myDataLock.unlock();
		}
	}
	
	 public static Paquet removeHostedPaquet(String id) {
	    myDataLock.lock();
	    try {
	      if(myData.containsKey(id)) { return myData.remove(id) ; }
	      else{ return null ; } 
	    }
	    finally {
	      myDataLock.unlock();
	    }
	  }

	public static void putNewPaquet(Paquet p) {
		myDataLock.lock();
		myData.put(p.idGlobal, p);
		myDataLock.unlock();
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
					//if(tableau.get(i).fichier.isOpen())
					//	Utilitaires.out("Chack !");
					tableau.get(i).fichier.read(b);
					b.flip();
					//Global.debug(i);
					temp += (int) b.get(0);
				}
				b.clear();
				temp %= 256;
				b.put((byte) temp);
				b.flip();

				tableau.get(Global.NOMBRESOUSPAQUETS - 1).fichier.write(b);
			}
			
			for(int i=0;i<Global.NOMBRESOUSPAQUETS;i++)
				tableau.get(i).remettrePositionZero();

		}
		catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public static void printServerList(){
		Utilitaires.out("Liste des serveurs :", 1, true);
		for(Machine m : allServeur)
			Utilitaires.out("s : " + m.toString(), 1, false);
			
	}

	public static void removePaquet(String ID) {
		myDataLock.lock();
		toSendASAPLock.lock();
		try {
			myData.remove(ID);
			toSendASAP.remove(ID);
		}
		finally {
			myDataLock.unlock();
			toSendASAPLock.unlock() ;
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
			if (allServeur.isEmpty()) return null;
			index %= allServeur.size();
			Machine m = allServeur.get(index);
			if (m.ipAdresse.equals(Global.MYSELF.ipAdresse) && m.port == Global.MYSELF.port) return null;
			return new InetSocketAddress(m.ipAdresse, m.port + 2);

		}
		finally {
			allServeurLock.unlock();
		}
	}
	
	
}
