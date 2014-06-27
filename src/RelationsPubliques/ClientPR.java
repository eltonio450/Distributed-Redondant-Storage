package RelationsPubliques;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.ConcurrentLinkedQueue;

import Utilitaires.Global;

public class ClientPR extends Thread{
	
	private DatagramChannel channel;
	private ByteBuffer buffBonjour;
	private ByteBuffer buffDebout;
	private ConcurrentLinkedQueue<Message> toSend;


	public ClientPR () throws IOException{
		this.channel = DatagramChannel.open();
		this.channel.socket().bind(new InetSocketAddress(Global.CLIENTPRPORT));
		this.buffBonjour = Utilitaires.Utilitaires.stringToBuffer(Utilitaires.Message.PREFIXE_BONJOUR);
		this.buffDebout = Utilitaires.Utilitaires.stringToBuffer(Utilitaires.Message.SELF_WAKE_UP);
		this.toSend = new ConcurrentLinkedQueue<Message> ();
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
				// On envoie bonjour au serveur de l'hôte distant
				channel.send(buffBonjour, remote);
				// On dit au serveur d'attendre une réponse du client de l'hôte distant
				Global.serverPR.expectMessage(new ExpectedMessage(Utilitaires.Message.PREFIXE_REPONSE_BONJOUR, new InetSocketAddress(remote.getHostName(), remote.getPort()-1), System.currentTimeMillis() + Global.TIMEOUT));
				// Réveille le serveur si personne d'autre ne lui parle
				channel.send(buffDebout, new InetSocketAddress("localhost", Global.SERVERPRPORT));

				buffDebout.flip();
				buffBonjour.flip();

				// Envoie ce qu'on lui a demandé d'envoyer
				for (int i=0; !toSend.isEmpty() && i<100; i++) {
					message = toSend.poll();
					channel.send(Utilitaires.Utilitaires.stringToBuffer(message.body), message.dest);
				}

				if (toSend.isEmpty()) {
					try {
						Thread.sleep(Global.SLEEPTIME);
					} catch (InterruptedException e) {}
				}

			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("ClientPR Thread just crashed");
				System.exit(-1); 
			}
		}
	}

	public void sendMessage (Message message) {
		toSend.add(message);
		this.interrupt();
	}
}
