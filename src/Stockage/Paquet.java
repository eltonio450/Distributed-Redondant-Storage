package Stockage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractInterruptibleChannel;
import java.nio.file.FileSystems;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import Utilitaires.Global;
import Utilitaires.Message;
import Utilitaires.Utilitaires;

/**
 * Cette classe represente un paquet
 * 
 * @author SebastienD
 * 
 */
public class Paquet {

	/**
	 * L'identifiant unique correspondant a un paquet
	 */
	public String idGlobal;

	/**
	 * Un numero unique mais seulement du point de vue du proprietaire. Il
	 * correspond a sa position lorsque les donnees du proprietaires sont
	 * decoupees en paquets
	 */
	public int idMachine;

	/**
	 * La position du paquet parmi le groupe de paquet dont il fait partie.
	 */
	public int idInterne;

	/**
	 * Permet d'attribuer un role si un hote d'un paquet de son groupe est mort.
	 * En fait, power = idInterne.
	 */
	public int power;
	// boolean enLecture;

	/**
	 * Le cannal qui permet de relier le paquet a l'emplacement sur le disque ou
	 * sont rellement stockees les donnees.
	 */
	public FileChannel fichier;

	/**
	 * 
	 */
	public ArrayList<Machine> otherHosts;

	public Machine owner;
	public LinkedList<Integer> toUnlock = new LinkedList<Integer>();
	// boolean dernierSignificatif; //si le paquet contient la "fin" de
	// l'information
	// long dernierePositionSignificative; //la position de la "fin" de
	// l'information

	public boolean lockLogique = false; // défini si le paquet peut-être
	// déplacé. Attention : différent d'un
	// Lock : locked peut-être modifié par
	// n'importe quel Thread.
	// public boolean isAskingTheLock = false;

	// Lock lockHasAsked = new ReentrantLock();
	Lock isUsed = new ReentrantLock();
	Lock lockPhysique = new ReentrantLock();

	Lock spreadUnlockLock = new ReentrantLock();

	public Paquet(int Id, Machine proprio) {

		// possessionLock = false;
		idMachine = Id;
		idInterne = idMachine % Global.NOMBRESOUSPAQUETS;

		idGlobal = proprio.toString() + "-" + Id;

		power = idInterne;
		owner = proprio;

		otherHosts = new ArrayList<Machine>();
		// Utilitaires.out("Otherhosts créé avec une taille : "+otherHosts.size()
		// + " " + Global.NOMBRESOUSPAQUETS);
		// otherHosts.add(Global.MYSELF);

		try {
			fichier = FileChannel.open(FileSystems.getDefault().getPath(pathOnDisk()), StandardOpenOption.READ, StandardOpenOption.WRITE,
					StandardOpenOption.CREATE);
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			Utilitaires.out("Erreur lors de l'ouverture du fichier.");
			e.printStackTrace();
		}
		Utilitaires.out("Fichier généré : " + pathOnDisk(), 7, false);
	}

	public String pathOnDisk() {
		return Global.PATHTODATA + owner.toString() + "-" + idInterne + ".txt";
	}

	public void putPower(int p) {
		power = p;
	}

	// TODO : supprimer �a , c'est juste pour test1
	public void changeOwner(Machine m) {
		owner = m;
	}

	public void putOtherHosts(ArrayList<Machine> liste) {
		int n = liste.size();
		for (int j = 0; j < n; j++) {
			otherHosts.add(liste.get(j));
		}
	}

	public static LinkedList<ArrayList<Paquet>> fileToPaquets(String path) { // TODO
		// doit decouper un fichier en liste de groupes de (4+1) paquets
		// doit initialiser les champs : id, power et proprio
		return null;
	}

	public void envoyerPaquetReellement(SocketChannel s) throws IOException {
		// we assume connection has already started

		// envoie d'abord les informations

		ByteBuffer buffer = createBufferForPaquetInformation(); // already
		// flipped
		s.write(buffer);
		// Utilitaires.out("Buffer du paquet bien envoyé.", 1, true);
		isUsed.lock();
		try {
			fichier.transferTo(0, Global.PAQUET_SIZE, s);
			// Utilitaires.out("transfer done") ;
		}
		finally {
			isUsed.unlock();
		}
	}

	public ByteBuffer createBufferForPaquetInformation() {
		// create a buffer and flip it at the end

		String s = idMachine + " " + owner.ipAdresse + " " + owner.port;
		for (int i = 0; i < Global.NOMBRESOUSPAQUETS; i++) {
			Machine m = otherHosts.get(i);
			s = s + " " + m.ipAdresse + " " + m.port;
		}
		s = s + " " + Message.END_ENVOI + " ";
		ByteBuffer buffer = Utilitaires.stringToBuffer(s);
		return buffer;
	}

	public static Paquet createPaquetFromBuffer(SocketChannel socket) throws IOException {
		
		String[] t = new String[1];
		t[0] = Message.END_ENVOI;
		// Utilitaires.out("Test 1",6,true);
		//Utilitaires.out("Fréquence 1",1,true);
		String msg = "";
		msg = Utilitaires.getAFullMessage(t, socket);

		//Utilitaires.out("Fréquence 7 : "+ msg);

		// Utilitaires.out("Paquet recu: " + msg) ;
		Scanner scan = new Scanner(msg);

		
		int id = scan.nextInt();
		
		String IpAdresse = scan.next();
		int port = scan.nextInt();
		
		// Utilitaires.out(id + " - " + IpAdresse + " - " + port) ;
		Machine owner = new Machine(IpAdresse, port);
		// Utilitaires.out("Test 2",6,true);
		ArrayList<Machine> hosts = new ArrayList<Machine>(Global.NOMBRESOUSPAQUETS);
		for (int i = 0; i < Global.NOMBRESOUSPAQUETS; i++) {
			// Utilitaires.out("Test 3",6,true);
			String ip = scan.next();
			int p = scan.nextInt();
			hosts.add(i, new Machine(ip, p));
		}
		// Utilitaires.out("Test 4",6,true);
		Paquet paq = new Paquet(id, owner);
		paq.putOtherHosts(hosts);

		scan.close();
		return paq;
	}

	public static Paquet recoitPaquetReellement(SocketChannel s) throws IOException {
		
		Paquet p = createPaquetFromBuffer(s);

		p.isUsed.lock();
		try {
			p.fichier.transferFrom(s, 0, Global.PAQUET_SIZE);
			// Utilitaires.out("J'ai bien reçu le corps", 1, true);
		}
		finally {
			p.isUsed.unlock();
		}
		return p;
	}

	public void removePaquet() {
		Donnees.removePaquet(this);
		deleteData();
	}

	public void deleteData() {
		// TODO : v�rifier que cette suppression fonctionne bien et ensuite
		// l'utiliser
		// ie pour l'instant on ne supprime rien du disque
		/*
		 * isUsed.lock(); try{ File f = new File(pathOnDisk) ; f.delete() ; }
		 * finally{ isUsed.unlock(); }
		 */
	}

	/**
	 * 
	 * @author Antoine
	 * @return Booléen : true si il a réussi, false sinon. Cette fonction permet
	 *         de locker sur les machines distantes tous les paquets liés à ce
	 *         paquet. <b>Attention :</b> cette fonction est bloquante et doit
	 *         obligatoirement être executée dans un thread séparé.
	 * 
	 */

	public boolean askForlock() {
		if(lockLogique)
		{
			return false;
		}
		// isAskingTheLock = true;
		int resultat = 0;
		Integer i = 0;
		// int j = idGlobal-idInterne;
		//Utilitaires.out("Demande de lock formulée par " + idGlobal, 5, true);
		while (i < Global.NOMBRESOUSPAQUETS) {

			if (i != idInterne && resultat == 0) {
				resultat = sendAskForLock(otherHosts.get(i), owner.toString() + "-" + (idMachine - idInterne + i), this.idInterne);
				spreadUnlockLock.lock();
				toUnlock.add(i);
				spreadUnlockLock.unlock();
			}
			i++;
		}
		switch (resultat) {

			case 0:
				//unlock();
				//Utilitaires.out("La demande de lock formulée par " + idGlobal + " a réussi.", 5, true);
				return true;
			default:

				//spreadUnlock();
				
				//Utilitaires.out("La demande de lock formulée par " + idGlobal + " a échoué.", 5, true);
				return false;

		}

	}

	/**
	 * 
	 * @author Antoine
	 * @param Machine
	 *            : Serveur sur lequel se trouve le paquet à locker.
	 * @return 0 : si le lock a réussi 1 : si le lock a raté car le paquet est
	 *         déjà en train d'être locké 2 : si le lock a raté car le paquet
	 *         n'était pas présent sur la machine.
	 * 
	 *         <b>Attention :</b> cette fonction est bloquante et doit
	 *         obligatoirement être executée dans un thread séparé.
	 * 
	 */

	public int sendAskForLock(Machine m, String idGlobal, int power) {
		// SocketChannel clientSocket;
		try {
			SocketChannel clientSocket = SocketChannel.open();
			// Etape 1 : Initialisation de la connexion
			// Utilitaires.out("Envoi de la demande de lock à " + idGlobal, 6,
			// true);
			InetSocketAddress local = new InetSocketAddress(0);
			clientSocket.bind(local);
			InetSocketAddress remote = new InetSocketAddress(m.ipAdresse, m.port);

			clientSocket.connect(remote);

			ByteBuffer buffer = Utilitaires.stringToBuffer(Message.ASK_FOR_LOCK);
			//Utilitaires.out("ASK 1",0,true);
			clientSocket.write(buffer);
			buffer.clear();
			//Utilitaires.out("ASK 2",0,true);
			clientSocket.read(buffer);
			buffer.flip();

			if (Utilitaires.buffToString(buffer).equals(Message.OK)) {
				//Utilitaires.out("ASK 3",0,true);
				// Utilitaires.out("Test 4", 6, true);
				buffer.clear();
				buffer = Utilitaires.stringToBuffer(idGlobal + " " + idInterne);
				clientSocket.write(buffer);

			
				buffer.clear();
				//Utilitaires.out("ASK 4",0,true);
				clientSocket.read(buffer);
				//Utilitaires.out("ASK 5",0,true);
				clientSocket.close();
				buffer.flip();

				if (Utilitaires.buffToString(buffer).equals(Message.OK))
					return 0;

				else
					return 1;
			}
			else {
				clientSocket.close();
				Utilitaires.out("Gros bug : lock impossible !");
				return 2;
			}

		}
		catch (Exception e) {
			Utilitaires.out("La demande de lock a foiré");
			e.printStackTrace();
			return 3;
		}
		finally {

		}
	}

	public void lock() {
		//Utilitaires.out("Lock du paquet : " + idGlobal,5,true);
		
		lockLogique = true;

	}

	public void unlock() {
		//Utilitaires.out("Unlock du paquet : " + idGlobal,5,true);
		
		lockLogique = false;
		//Donnees.printMyData();
	}

	public void spreadUnlock() {

		// isAskingTheLock = true;
		int resultat = 0;

		spreadUnlockLock.lock();
		//Utilitaires.out("Apres le spread unlock ici",0,true);
		try {
			for (Integer i : toUnlock) {
				//Utilitaires.out("Youhou et i avant comp : " + i);
				if (i != idInterne && resultat == 0) {
					//Utilitaires.out("Youhou et i après comp : " + i);
					askForUnlock(otherHosts.get(i), owner.toString() + "-" + (idMachine - idInterne + i));
					//Utilitaires.out("Youhou et i en dessous dans comp: " + i);
				}
				//Utilitaires.out("Youhou dessous sortie comp : " + i);
			}
			//Utilitaires.out("Le unlock a réussi");
		}
		catch(Exception e)
		{
			System.exit(-1);
			e.printStackTrace();
		}
		finally {
			//Utilitaires.out("Le unlock a réussi 2");
			toUnlock.clear();
			spreadUnlockLock.unlock();
			//Donnees.printMyData();
			
		}
	}

	public void spreadTotalUnlock() {
		spreadUnlockLock.lock();
		for (int i = 0; i < Global.NOMBRESOUSPAQUETS; i++)
			toUnlock.add(i);
		toUnlock.remove((Integer.valueOf(idInterne)));
		spreadUnlockLock.unlock();
		spreadUnlock();
	}

	public int askForUnlock(Machine m, String idGlobal) {
		
		try(SocketChannel clientSocket = SocketChannel.open()) {
			//Utilitaires.out("UNLOCK 0");
			
			// Etape 1 : Initialisation de la connexion
			//Utilitaires.out("Envoi de la demande de unlock pour " + idGlobal, 6, true);
			InetSocketAddress local = new InetSocketAddress(0);
			clientSocket.bind(local);
			InetSocketAddress remote = new InetSocketAddress(m.ipAdresse, m.port);

			if (!clientSocket.connect(remote))
				Utilitaires.out("Enorme erreur !", 6, true);
			//
			// Etape 2 : Envoie du pré-Lock

			ByteBuffer buffer = Utilitaires.stringToBuffer(Message.ASK_FOR_UNLOCK);
			
			//Utilitaires.out("UNLOCK 1");
			clientSocket.write(buffer);
			buffer.clear();
			
			clientSocket.read(buffer);
			//Utilitaires.out("UNLOCK 3");
			buffer.flip();
			
			if (Utilitaires.buffToString(buffer).equals(Message.OK)) {
				buffer = Utilitaires.stringToBuffer(idGlobal);
				
				clientSocket.write(buffer);
				buffer.clear();
				clientSocket.read(buffer);
				buffer.flip();
				String m2 = Utilitaires.buffToString(buffer);
				//Utilitaires.out("Et juste avant le return : "+m2 +idGlobal,0,true);
				return 0;
			}

			else {
				Utilitaires.out("Yolo on a raté!",0,true);
				return 1;
			}
		}
		catch (Exception e) {
			Utilitaires.out("La demande de unlock a foiré");
			e.printStackTrace();
			return 3;
		}
		
	}

	public boolean isLocked() {
		return lockLogique;
	}

	public void remettrePositionZero() {

		try {
			fichier.position(0);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	

}
