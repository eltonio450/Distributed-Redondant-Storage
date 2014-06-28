package RelationsPubliques;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.ConcurrentLinkedQueue;

import GestionnaireMort.deathVerifier;
import Stockage.Donnees;
import Utilitaires.Global;
import Utilitaires.Message;
import Utilitaires.Utilitaires;

public class ClientPR extends Thread{

	private DatagramChannel channel;
	private ByteBuffer buffBonjour;
	private ByteBuffer buffDebout;
	private ConcurrentLinkedQueue<Message> toSend;
	private long lastTime;


	public ClientPR () throws IOException{
		this.channel = DatagramChannel.open();
		this.channel.socket().bind(new InetSocketAddress(Global.CLIENTPRPORT));
		this.buffBonjour = Utilitaires.stringToBuffer(Message.PREFIXE_BONJOUR);
		this.buffDebout = Utilitaires.stringToBuffer(Message.SELF_WAKE_UP);
		this.toSend = new ConcurrentLinkedQueue<Message> ();
		this.lastTime = 0;
	}

	public void run () {
		Message message;
		InetSocketAddress remote;

		while (true) {
			try {
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
					lastTime = System.currentTimeMillis();
					} catch (Exception e) {
						deathVerifier.verifyDeath(new Stockage.Machine(remote));
					}
				}

				// Réveille le serveur si personne d'autre ne lui parle
				try{
					channel.send(buffDebout, new InetSocketAddress("127.0.0.1", Global.SERVERPRPORT));
				}catch(Exception e){
					Utilitaires.out("Dernier message crashé !", 1, true);
				}

				buffDebout.flip();
				buffBonjour.flip();

				// Envoie ce qu'on lui a demandé d'envoyer
				for (int i=0; !toSend.isEmpty() && i<4; i++) {
					Utilitaires.out("Taille : " +toSend.size());
					message = toSend.poll();
					try {
						channel.send(Utilitaires.stringToBuffer(message.body), message.dest);
					} catch (Exception e) {
						deathVerifier.verifyDeath(new Stockage.Machine(message.dest));
						Utilitaires.out("Dernier message crashé !", 1, true);
					}
				}

				if (toSend.isEmpty()) {
					Utilitaires.out("Essai de dodo");
					try {
						sleep(Global.SLEEPTIME);
					} catch (InterruptedException e) {}
				}

			} catch (IOException e) {
				e.printStackTrace();
				Utilitaires.out("ClientPR Thread just crashed");
				System.exit(-1); 
			}
		}
	}

	public void sendMessage (Message message) {
		toSend.add(message);
		this.interrupt();
	}
}
