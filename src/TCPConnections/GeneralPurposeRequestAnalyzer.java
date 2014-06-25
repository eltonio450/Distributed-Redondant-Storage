package TCPConnections;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;

import Stockage.Donnees;
import Stockage.Machine;
import Utilitaires.*;
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
			aTraiter.clear();
			lock.unlock();
			for (Requester r : aEnlever) {
				aTraiter.remove(r);
			}
		}
	}

	public void addRequester(Requester requester) {
		lock.lock();
		try {
			requester.socket.configureBlocking(false);
		} catch (IOException e) {
			System.out.println("Problème de changement Blocking / Non blocking");
			return;
		}
		aAjouter.add(requester);
		lock.unlock();
	}

	private void traiter (Requester r) {
		Scanner s = new Scanner (r.recu);
		String token = s.next();
		try {
			if (token.equals(Global.EXCHANGE)) {
				r.socket.write(Utilitaires.stringToBuffer(Global.REPONSE_EXCHANGE));
				r.socket.configureBlocking(true);
				Slaver.giveTask(new Task.taskServeurExchange(r.socket), 20);
							}
			else if (token.equals(Global.MONITOR)){
				// Suite
			}
			else if (token.equals(Global.VERIFY_DEATH)) {
			//	r.socket.write(Global.)
			}
			else if (token.equals(Global.HOST_CHANGED)) {
			  r.socket.configureBlocking(true);
			  //TODO : interpr�ter pour r�cup�rer
			  String Id = "";
			  int place =0;
			  Machine newHost = Machine.otherMachineFromSocket(r.socket) ;
			  Donnees.changeHostForPaquet(Id, place, newHost);
	      }
		} catch (IOException e) {
			aEnlever.add(r);
			return;
		} finally {
			s.close();
		}
	}
}