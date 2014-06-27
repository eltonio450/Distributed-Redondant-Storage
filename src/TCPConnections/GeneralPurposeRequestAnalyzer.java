package TCPConnections;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;

import Task.taskLockPacket;
import Task.taskSendRequestedPaquet;
import Utilitaires.Message;
import Utilitaires.Slaver;
import Utilitaires.Utilitaires;
/**
 * 
 * @author Simon
 * 
 * Surveille une liste de sockets encapsulées dans un Requester
 * Lit le buffer EN MODE NON BLOQUANT
 * Met tout dans la chaîne du requester
 * 
 * POUR FAIRE QUELQUE CHOSE :
 * Regarder la chaîne du requester (avec le scan, bref...)
 * Si on a le premier token, 
 * 		- REMETTRE LA SOCKET EN MODE BLOQUANT (socket.configureBlocking(false);) (ou en subir les conséquences)
 * 		- Faire ce qu'on veut en sachant qu'on a la première partie du message (qui peut être tout le message ou
 * 					juste le premier mot, donc)
 * 		- AJOUTER LA SOCKET A aEnlever !! (pas de problème de concurrence)
 */
public class GeneralPurposeRequestAnalyzer extends Thread {
	LinkedList<Requester> aTraiter;
	LinkedList<Requester> aAjouter;
	LinkedList<Requester> aEnlever;
	ReentrantLock lock;

	public void run () {
		aTraiter = new LinkedList<Requester> ();
		aAjouter = new LinkedList<Requester> ();
		aEnlever = new LinkedList<Requester> ();
		lock = new ReentrantLock ();
		ByteBuffer buff = ByteBuffer.allocateDirect(10000);

		while (true) {
			for (Requester r : aTraiter) {
				try {
					buff.clear();
					r.socket.read(buff);
				} catch (IOException e) {
					aEnlever.add(r);
				}
				buff.flip();
				r.recu += Utilitaires.buffToString(buff);
				try {
					traiter(r);
				} catch (Exception e) {

				}
			}

			lock.lock();
			aTraiter.addAll(aAjouter);
			aAjouter.clear();
			lock.unlock();
			for (Requester r : aEnlever) {
				aTraiter.remove(r);
			}
		}
	}

	public void addRequester(Requester requester) {
		try {
			requester.socket.configureBlocking(false);
		} catch (IOException e) {
			System.out.println("Problème de changement Blocking / Non blocking");
			return;
		}
		lock.lock();
		aAjouter.add(requester);
		lock.unlock();
	}

	private void traiter (Requester r) {
		Scanner scan = new Scanner (r.recu);
		String token = scan.next();
		try {
			if (token.equals(Message.EXCHANGE)) {
				r.socket.configureBlocking(true);
				aEnlever.add(r);
				Slaver.giveTask(new Task.taskServeurExchange(r.socket), 20);
			}
			
			
			else if (token.equals(Message.MONITOR)){
				// Suite
			}
			
			
			else if (token.equals(Message.IS_DEAD)) {
				if (scan.hasNext()) {
					String ip = scan.next();
					int port = -1;
					if (scan.hasNext()) {
						try {
							port = Integer.parseInt(scan.next());
						} catch (Exception e) {
							// Parsing exception
							// The number following the IP Adress is wrong
							// Remove the requester (won't get any better
							// Warn
							aEnlever.add(r);
							System.out.println("Corrupted message : " + r.recu);
							return;
						} 

						if (scan.hasNext()) {
							// Alors le numéro de port était bien fini
							// On peut agir
							Slaver.giveUrgentTask(new Task.taskReactToDeath(ip, port), 1);
							aEnlever.add(r);
							r.socket.close();
							return;
						}
					}
				}
			}
			
			else if(token.equals(Message.DEMANDE_PAQUET)){
				r.socket.configureBlocking(true);
				aEnlever.add(r);
				Slaver.giveTask(new taskSendRequestedPaquet(r.socket), 1);
			}
			
			
			else if (token.equals(Message.NEW_SERVER)) {
				if (scan.hasNext()) {
					String ip = scan.next();
					int port = -1;
					if (scan.hasNext()) {
						try {
							port = Integer.parseInt(scan.next());
						} catch (Exception e) {
							// Parsing exception
							// The number following the IP Adress is wrong
							// Remove the requester (won't get any better
							// Warn
							aEnlever.add(r);
							System.out.println("Corrupted message : " + r.recu);
							return;
						} 

						if (scan.hasNext()) {
							// Alors le numéro de port était bien fini
							// On peut agir
							Stockage.Donnees.addHost(new Stockage.Machine (ip, port));
							aEnlever.add(r);
							r.socket.close();
							return;
						}
					}
				}
			}
			
			
			else if (token.equals(Message.GET_LIST)) {
				r.socket.configureBlocking(true);
				Slaver.giveTask(new Task.taskSendServerList(r.socket), 2);	
				aEnlever.add(r) ;
			}
			
			
			else if (token.equals(Message.HOST_CHANGED)) {
				r.socket.configureBlocking(true);
				Slaver.giveTask(new Task.taskHostHasChanged(r.socket), 10);
				aEnlever.add(r) ;
			}
			
			
			else if (token.equals(Message.ASK_FOR_LOCK))
			{ 
				r.socket.configureBlocking(true);
				aEnlever.add(r);
				Slaver.giveUrgentTask(new taskLockPacket (r.socket), 2);
			}

			/**
			 * Modèle :
			 * 
			 * else if (token.equals(MOT_CLEF) {
			 * 		MyTache m = new MyTache (r.socket);					// Nouvelle tâche
			 * 		r.socket.configureBlocking(true);		// La nouvelle tâche va attendre qu'il parle
			 * 		aEnlever.add(r); 						// On enlève r de la liste à analyser par le GPRA
			 * 		Slaver.giveTask(m); 					// Ou Slaver.giveUrgentTask(m)
			 * }
			 *
			 *
			 */
		} catch (IOException e) {
			aEnlever.add(r);
			return;
		} catch (Exception e) {
			// Catch parsing exception etc.
		}
		scan.close();
	}
}
