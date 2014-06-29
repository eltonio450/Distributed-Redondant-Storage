package Stockage;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.sun.security.ntlm.Client;

import Utilitaires.Global;
import Utilitaires.Message;
import Utilitaires.Utilitaires;


public class Paquet {

	public String idGlobal;
	public int idMachine;
	public int idInterne;

	//pour rétablir un paquet manquant : si on a power 1, c'est à nous de rétablir le paquet.
	public int power ;
	//boolean enLecture;

	public FileChannel fichier;

	//il va falloir protéger cette variable vis-à-vis de la concurrence je pense
	//otherHosts est la liste des host : Attention, les indices correspondent au numero du paquet !
	public ArrayList<Machine> otherHosts ;

	public Machine owner ;

	//boolean dernierSignificatif; //si le paquet contient la "fin" de l'information
	//long dernierePositionSignificative; //la position de la "fin" de l'information

	public boolean lockLogique = false; //défini si le paquet peut-être déplacé. Attention : différent d'un Lock : locked peut-être modifié par n'importe quel Thread.
	public boolean isAskingTheLock = false;
	
	//Lock lockHasAsked = new ReentrantLock();
	Lock isUsed = new ReentrantLock();
	Lock lockPhysique = new ReentrantLock();


	public Paquet(int Id, Machine proprio) {

		//possessionLock = false;
		idMachine = Id;
		idInterne = idMachine%Global.NOMBRESOUSPAQUETS;

		idGlobal = proprio.toString()+"-"+Id ;

		power = idInterne ;
		owner = proprio ;

		otherHosts = new ArrayList<Machine> (Global.NOMBRESOUSPAQUETS) ;
		otherHosts.add(Global.MYSELF);

		try {
			fichier = FileChannel.open(FileSystems.getDefault().getPath(pathOnDisk()), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			Utilitaires.out("Erreur lors de l'ouverture du fichier.");
			e.printStackTrace();
		}
		Utilitaires.out("Fichier généré : "+pathOnDisk(), 7, false);
	}
	
	public String pathOnDisk()
	{
		return Global.PATHTODATA+owner.toString()+"-"+ idInterne+".txt";
	}

	public void putPower(int p){
		power = p ;
	}

	// TODO : supprimer �a , c'est juste pour test1
	public void changeOwner(Machine m){
	  owner = m ;
	}

	public void putOtherHosts(ArrayList<Machine> liste){
		int n = liste.size() ;
		for (int j=0; j< n; j++){
			otherHosts.add(liste.get(j)) ;
		}
	}


	public static LinkedList<ArrayList<Paquet>> fileToPaquets(String path){  //TODO
		//doit decouper un fichier en liste de groupes de (4+1) paquets
		// doit initialiser les champs : id, power et proprio
		return null ;
	}

	public void envoyerPaquet(SocketChannel s) throws IOException{
		//we assume connection has already started
		ByteBuffer buffer = createBufferForPaquetInformation();  //already flipped
		s.write(buffer) ;
		isUsed.lock();
		try {
			fichier.transferTo(0, Global.PAQUET_SIZE, s) ;
			Utilitaires.out("transfer done") ;
		}
		finally {
			isUsed.unlock();
		}
	}

	public ByteBuffer createBufferForPaquetInformation() {
		//create a buffer and flip it at the end
	  
		String s = idMachine + " " + owner.ipAdresse + " " + owner.port;
		for (int i = 0 ; i < Global.NOMBRESOUSPAQUETS ; i++){
			Machine m = otherHosts.get(i) ;
			s = s + " " + m.ipAdresse + " " + m.port ;
		}
		s = s + " " + Message.END_ENVOI ;
		ByteBuffer buffer = Utilitaires.stringToBuffer(s) ;
		return buffer;
	}

	public static Paquet createPaquetFromBuffer(SocketChannel socket) throws IOException{

    String[] t = new String[1] ;
    t[0] = Message.END_ENVOI ;
    String msg = Utilitaires.getAFullMessage(t, socket);
    
		Utilitaires.out("Paquet recu: " + msg) ;
		Scanner scan = new Scanner(msg) ; 

		int id = scan.nextInt() ;
		String IpAdresse = scan.next() ;
		int port = scan.nextInt() ;
		Utilitaires.out(id + " - " + IpAdresse + " - " + port) ;
		Machine owner = new Machine(IpAdresse,port) ;

		ArrayList<Machine> hosts = new ArrayList<Machine>(Global.NOMBRESOUSPAQUETS) ;
		for(int i = 0; i<Global.NOMBRESOUSPAQUETS;i++){
			String ip = scan.next() ;
			int p = scan.nextInt() ;
			hosts.add(i, new Machine(ip,p)) ;
		}

		Paquet paq = new Paquet(id,owner) ;
		paq.putOtherHosts(hosts);
		return paq ;
	}

	public static Paquet recoitPaquet(SocketChannel s) throws IOException{
		//we assume connection has already started
		Paquet p = createPaquetFromBuffer(s) ;
		p.isUsed.lock() ;
		try {
			p.fichier.transferFrom(s,0,Global.PAQUET_SIZE) ;
		}
		finally {
			p.isUsed.unlock() ;
		}
		return p ;
	}

	public void removePaquet(){
	  Donnees.removePaquet(this) ;
	  deleteData() ;
	}
	
	public void deleteData() {
	  //TODO : v�rifier que cette suppression fonctionne bien et ensuite l'utiliser
	  //ie pour l'instant on ne supprime rien du disque
	  /*
		isUsed.lock();
		try{
			File f = new File(pathOnDisk) ;
			f.delete() ;
		}
		finally{
			isUsed.unlock(); 
		}
		*/
	}






	/**
	 * 
	 * @author Antoine
	 * @return Booléen : true si il a réussi, false sinon.
	 *	Cette fonction permet de locker sur les machines distantes tous les paquets liés à ce paquet.
	 *	<b>Attention :</b> cette fonction est bloquante et doit obligatoirement être executée dans un thread séparé.
	 *
	 */

	public boolean askForlock(){
		lock();
		isAskingTheLock = true;
		int resultat = 0;
		int i = 0;

		while(i<Global.NOMBRESOUSPAQUETS){
			if(i!=idInterne && resultat == 0){				
				resultat = sendAskForLock(otherHosts.get(i), idGlobal);
			}
			i++;
		}
		switch(resultat){
		case 0:
			unlock();
			return true;
		default:
			return false;				
		}
		
	}

	/**
	 * 
	 * @author Antoine
	 * @param Machine : Serveur sur lequel se trouve le paquet à locker.
	 * @return 	0 : si le lock a réussi
	 * 			1 : si le lock a raté car le paquet est déjà en train d'être locké
	 *			2 : si le lock a raté car le paquet n'était pas présent sur la machine.
	 *		
	 *		<b>Attention :</b> cette fonction est bloquante et doit obligatoirement être executée dans un thread séparé.
	 *
	 */

	public int sendAskForLock(Machine m, String idGlobal){
		String reponse = "";
		try (SocketChannel clientSocket = SocketChannel.open()) 
		{ 

			//Etape 1 : Initialisation de la connexion

			InetSocketAddress local = new InetSocketAddress(0); 
			clientSocket.bind(local); 
			InetSocketAddress remote = new InetSocketAddress(m.ipAdresse, m.port); 
			clientSocket.connect(remote); 

			//Etape 2 : Envoie du pré-Lock
			ByteBuffer buffer = Utilitaires.stringToBuffer(Message.ASK_FOR_LOCK);
			buffer.flip() ;
			clientSocket.write(buffer) ;
			

			//Etpae 3 : Reception du la confirmation de la connexion
			reponse = Utilitaires.getAFullMessage(Message.OK, clientSocket);
			
			
			//Etape 4 : envoie du paquet incriminé.

			buffer.clear();
			Utilitaires.stringToBuffer(idGlobal);
			buffer.flip();
			clientSocket.write(buffer);

			//Etape 5 : reception de la confirmation
			
			reponse = Utilitaires.getAFullMessage(Message.END_ENVOI, clientSocket);
			clientSocket.close();
			Scanner scan = new Scanner(reponse);
			if(scan.next()==Message.OK)
				return 0;
			
			else
				return 1;
		


		}
		catch(IOException e){
			return 3;
		}
	}


	public void lock(){
		lockLogique = true;	  
	}
	
	public void unlock(){
		lockLogique = false;	  
	}


	public void spreadUnlock(){
		
	}

	public boolean isLocked(){
		return lockLogique;		
	}


	public void remettrePositionZero()
	{
		
				try {
					fichier.position(0);
				}
				catch (IOException e) {
					e.printStackTrace();
				}
	}

}
