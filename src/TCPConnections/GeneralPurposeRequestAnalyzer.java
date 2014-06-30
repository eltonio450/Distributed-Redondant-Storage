package TCPConnections;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import Task.taskLockPacket;
import Task.taskUnlockPacket;
import Utilitaires.Global;
import Utilitaires.Message;
import Utilitaires.Slaver;
import Utilitaires.Utilitaires;

/**
 * 
 * @author Simon
 * 
 *         Surveille une liste de sockets encapsulées dans un Requester Lit le
 *         buffer EN MODE NON BLOQUANT Met tout dans la chaîne du requester
 * 
 *         POUR FAIRE QUELQUE CHOSE : Regarder la chaîne du requester (avec le
 *         scan, bref...) Si on a le premier token, - REMETTRE LA SOCKET EN MODE
 *         BLOQUANT (socket.configureBlocking(false);) (ou en subir les
 *         conséquences) - Faire ce qu'on veut en sachant qu'on a la première
 *         partie du message (qui peut être tout le message ou juste le premier
 *         mot, donc) - AJOUTER LA SOCKET A aEnlever !! (pas de problème de
 *         concurrence)
 */
public class GeneralPurposeRequestAnalyzer extends Thread {
	LinkedBlockingQueue<Requester> aTraiter;
	LinkedList<Requester> aAjouter;
	LinkedList<Requester> aEnlever;
	ReentrantLock lock;
	Condition c;

	public void run() {
		aTraiter = new LinkedBlockingQueue<Requester>();
		aAjouter = new LinkedList<Requester>();
		aEnlever = new LinkedList<Requester>();
		lock = new ReentrantLock();
		c = lock.newCondition();

		ByteBuffer buff = ByteBuffer.allocateDirect(10000);

		while (true) {
			while (aTraiter.isEmpty()) {
				lock.lock();
				c.awaitUninterruptibly(); // On patiente si la liste des sockets
										  // à écouter est vide
				aTraiter.addAll(aAjouter);
				aAjouter.clear();
				lock.unlock();
			}

			lock.lock();
			for (Requester r : aTraiter) {
				try {
					buff.clear();
					r.socket.read(buff);
				}
				catch (IOException e) {

					aEnlever.add(r);
					e.printStackTrace();
				}
				buff.flip();
				r.recu += Utilitaires.buffToString(buff);
				if (buff.hasRemaining())
					Utilitaires.out("(TCP) from " + r.socket.socket().getPort() + " : " + r.recu, 6, false);
				else
					Utilitaires.out("(TCP) from " + r.socket.socket().getPort() + ": message vide", 6, false);
				if (r.socket.socket().isClosed() || System.currentTimeMillis() - r.timeIni > Global.SOCKET_TIMEOUT) {
					try {
						r.socket.close();
					}
					catch (IOException e) {
						// Nope, still don't care.
					}
					aEnlever.add(r);
				}
				else {
					try {
						traiter(r);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			aTraiter.addAll(aAjouter);
			aAjouter.clear();
			lock.unlock();
			for (Requester r : aEnlever) {
				aTraiter.remove(r);
			}

			try {
				Thread.sleep(5); // Evite de tourner trop à vide quand une
									// connexion se tait.
			}
			catch (InterruptedException e) {
				// Nobody cares
			}
		}
	}

	public void addRequester(Requester requester) {
		try {
			requester.socket.configureBlocking(false);
		}
		catch (IOException e) {
			Utilitaires.out("Problème de changement Blocking / Non blocking");
			return;
		}
		lock.lock();
		try {
			aAjouter.add(requester);
			c.signal();
		}
		finally {
			lock.unlock();
		}
	}

	private void traiter(Requester r) {
		Scanner scan = new Scanner(r.recu);
		// Utilitaires.out("Message recu : "+r.recu);
		String token = "";
		if (scan.hasNext())
			token = scan.next();
		try {
			if (token.equals(Message.EXCHANGE)) {
				r.socket.configureBlocking(true);
				aEnlever.add(r);
				Slaver.giveTask(new Task.taskServeurExchange(r.socket), 20);
			}

			else if (token.equals(Message.SendOne)) {
				r.socket.configureBlocking(true);
				aEnlever.add(r);
				Slaver.giveTask(new Task.taskServeurReceiveOnePaquet(r.socket), 20);
			}

			else if (token.equals(Message.AskForPaquet)) {
				r.socket.configureBlocking(true);
				aEnlever.add(r);
				Slaver.giveTask(new Task.taskServeurGiveOnePaquet(r.socket), 20);
			}
			else if (token.equals(Message.GiveMeMyPaquet)) {
				r.socket.configureBlocking(true);
				aEnlever.add(r);
				Slaver.giveTask(new Task.taskServeurCopyPaquetToOwner(r.socket), 20);
			}

			else if (token.equals(Message.IS_DEAD)) {
				if (scan.hasNext()) {
					String ip = scan.next();
					int port = -1;
					if (scan.hasNext()) {
						try {
							port = Integer.parseInt(scan.next());
						}
						catch (Exception e) {
							// Parsing exception
							// The number following the IP Adress is wrong
							// Remove the requester (won't get any better
							// Warn
							aEnlever.add(r);
							Utilitaires.out("Corrupted message : " + r.recu);
							scan.close();
							return;
						}

						if (scan.hasNext()) {
							// Alors le numéro de port était bien fini
							// On peut agir
							Slaver.giveUrgentTask(new Task.taskReactToDeath(ip, port), 1);
							aEnlever.add(r);
							r.socket.close();
							scan.close();
							return;
						}
					}
				}
			}

			else if (token.equals(Message.NEW_SERVER)) {
				if (scan.hasNext()) {
					String ip = scan.next();
					int port = -1;
					if (scan.hasNext()) {
						try {
							port = Integer.parseInt(scan.next());
						}
						catch (Exception e) {
							// Parsing exception
							// The number following the IP Adress is wrong
							// Remove the requester (won't get any better
							// Warn
							aEnlever.add(r);
							Utilitaires.out("Corrupted message : " + r.recu);
							scan.close();
							return;
						}

						if (scan.hasNext()) {
							// Alors le numéro de port était bien fini
							// On peut agir
							Stockage.Donnees.putServer(ip, port);
							aEnlever.add(r);
							r.socket.close();
							scan.close();
							return;
						}
					}
				}
			}

			else if (token.equals(Message.GET_LIST)) {
				r.socket.configureBlocking(true);

				Slaver.giveTask(new Task.taskSendServerList(r.socket), 2);
				aEnlever.add(r);
			}

			else if (token.equals(Message.HOST_CHANGED)) {
				r.socket.configureBlocking(true);
				Slaver.giveTask(new Task.taskHostHasChanged(r.socket), 10);
				aEnlever.add(r);
			}

			else if (token.equals(Message.VERIFY_DEATH)) {
				// Utilitaires.out("OK DEATH");
				r.socket.configureBlocking(true);
				aEnlever.add(r);
				Slaver.giveUrgentTask(new Task.taskReplyStillAlive(r.socket), 1);
			}

			else if (token.equals(Message.ASK_FOR_LOCK)) {
				r.socket.configureBlocking(true);
				aEnlever.add(r);
				Slaver.giveUrgentTask(new taskLockPacket(r.socket), 2);
			}
			else if (token.equals(Message.ASK_FOR_UNLOCK)) {
				r.socket.configureBlocking(true);
				aEnlever.add(r);
				Slaver.giveUrgentTask(new taskUnlockPacket(r.socket), 2);
			}
			else if (token.isEmpty()) {
				Utilitaires.out("Chaine vide.", 5, true);

			}
			else {
				Utilitaires.out("Chaine non analysée : " + token.toString(), 5, true);

			}
		}
		catch (IOException e) {
			aEnlever.add(r);
			scan.close();
			return;
		}
		catch (Exception e) {
			// Catch parsing exception etc.
		}
		scan.close();
	}
}
