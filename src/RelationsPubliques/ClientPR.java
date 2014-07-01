package RelationsPubliques;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import GestionnaireMort.deathVerifier;
import Utilitaires.Global;
import Utilitaires.Message;
import Utilitaires.Utilitaires;

public class ClientPR extends Thread{

	private DatagramChannel channel;
	private ByteBuffer buffBonjour;
	private ByteBuffer buffDebout;
	private LinkedList<Message> toSend;
	private LinkedList<Message> toAdd;
	private long lastTime;
	public ReentrantLock lock;
	public Condition c;


	public ClientPR () throws IOException{
		this.lock = new ReentrantLock();
		this.channel = DatagramChannel.open();
		this.channel.socket().bind(new InetSocketAddress(Global.CLIENTPRPORT));
		this.channel.socket().setSoTimeout(0);
		this.buffBonjour = Utilitaires.stringToBuffer(Message.PREFIXE_BONJOUR);
		this.buffDebout = Utilitaires.stringToBuffer(Message.SELF_WAKE_UP);
		this.toSend = new LinkedList<Message> ();
		this.toAdd = new LinkedList<Message> ();
		this.lastTime = 0;
		c = lock.newCondition();
		//permet de désynchroniser les envois de messages.


	}

	public void run () {
		Message message;
		InetSocketAddress remote;

		while (true) {
			// Envoie coucou
			remote = Stockage.Donnees.getRemote();
			if (remote == null) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
				}
				continue;
			}


			if (System.currentTimeMillis() - lastTime > Global.SLEEPTIME) {
				try {
					// On envoie bonjour au serveur de l'hôte distant
					channel.send(buffBonjour, remote);
					// On dit au serveur d'attendre une réponse du client de l'hôte distant
					Global.serverPR.expectMessage(new ExpectedMessage(Message.PREFIXE_REPONSE_BONJOUR, new InetSocketAddress(remote.getHostName(), remote.getPort()-1), System.currentTimeMillis() + Global.TIMEOUT));
					buffBonjour.position(0);
					lastTime = System.currentTimeMillis();
				} catch (Exception e) {
					deathVerifier.verifyDeath(new Stockage.Machine(remote));
				}
			}

			// Envoie ce qu'on lui a demandé d'envoyer
			lock.lock();
			toSend.addAll(toAdd);
			toAdd.clear();
			lock.unlock();
			for (int i=0; !toSend.isEmpty() && i<100; i++) {
				//Utilitaires.out("Taille : " +toSend.size());
				message = toSend.poll();
				try {
					channel.send(Utilitaires.stringToBuffer(message.body), message.dest);
				} catch (Exception e) {
					deathVerifier.verifyDeath(new Stockage.Machine(message.dest));
					Utilitaires.out("Dernier message crashé !", 1, true);
				}
			}

			if (toSend.isEmpty()) {
				try {
					lock.lock();
					c.await(Global.SLEEPTIME, TimeUnit.MILLISECONDS);
					lock.unlock();
				} catch (InterruptedException e) {}
			}
		}
	}

	public void sendMessage (Message message) {
		lock.lock();
		toAdd.add(message);
		c.signal();
		lock.unlock();
	}
}
