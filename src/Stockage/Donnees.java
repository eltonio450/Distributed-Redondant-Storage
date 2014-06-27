package Stockage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import Utilitaires.Global;

public class Donnees {

	static private LinkedList<Machine> allServeur = new LinkedList<Machine>();
	static private HashSet<Machine> interestServeur = new HashSet<Machine>();
	static private LinkedList<Machine> myHosts = new LinkedList<Machine>();
	static private HashMap<String, Paquet> myData = new HashMap<String, Paquet>();

	// longueur de la data primaire en bits (ou bytes ?)
	static public long longueur;

	static private boolean filling = true;
	static private LinkedList<Machine> toRemove = new LinkedList<Machine>();
	static private int index = 0;

	// passage en public (cf la remarque sur le lock)
	static public LinkedList<String> myOwnData = new LinkedList<String>();

	static public LinkedList<String> toSendASAP = new LinkedList<String>();

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
		addInterestServeur(m);
		putNewPaquet(p);
		Utilitaires.Slaver.giveUrgentTask(new Task.taskWarnHostChanged("" + p.idGlobal), 1);
	}

	public static void changeHostForPaquet(String Id, int place, Machine newHost) {
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
		return allServeur.peek();
	}

	public static void putServer(String ip, int port) {
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

	public static Paquet choosePaquetToSend() {
		if (toSendASAP.isEmpty()) {
			return (Paquet) myData.values().toArray()[0];
		}
		else {
			return (myData.get(toSendASAP.getFirst()));
		}
	}

	public static LinkedList<String> chooseManyPaquetToSend1() {
		return toSendASAP;
	}

	public static LinkedList<String> chooseManyPaquetToSend2() {
		return new LinkedList(myData.keySet());
	}

	public static Paquet getPaquet(String id) {
		return myData.get(id);
	}

	public static void addInterestServeur(Machine m) {
		interestServeurLock.lock();
		interestServeur.add(m);
		interestServeurLock.unlock();
	}

	public static void addHost(Machine m) {
		myHostsLock.lock();
		myHosts.add(m);
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
				tableau.get(i).fichier.read(b);
				b.flip();
				Global.debug(i);
				temp += (int) b.get(0);
			}
			b.clear();
			temp %= 256;
			b.put((byte) temp);
			b.flip();
			
				tableau.get(Global.NOMBRESOUSPAQUETS - 1).fichier.write(b);
			}
			
		}
		catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void removePaquet(String ID) {
		myDataLock.lock();
		try {
			myData.remove(ID);
			toSendASAP.remove(ID);
		}
		finally {
			myDataLock.unlock();
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

			if (allServeur.isEmpty()) {
				index %= allServeur.size();
				Machine m = allServeur.get(index);
				return new InetSocketAddress(m.ipAdresse, m.port + 2);
			}
			else {
				return null;
			}
		}
		finally {
			allServeurLock.unlock();
		}
	}
}
