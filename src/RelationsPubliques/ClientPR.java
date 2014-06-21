package RelationsPubliques;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.ConcurrentLinkedQueue;

import Stockage.Donnees;
import Utilitaires.Global;
import Utilitaires.Utilitaires;

public class ClientPR extends Thread{
	
	private DatagramChannel channel;
	private ByteBuffer buffBonjour;
	private ByteBuffer buffDebout;
	private int remoteIndex;

	private ConcurrentLinkedQueue<Message> toSend;


	public ClientPR () throws IOException{
		this.channel = DatagramChannel.open();
		this.channel.socket().bind(new InetSocketAddress(Global.CLIENTPRPORT));
		this.buffBonjour = Utilitaires.stringToBuffer(Global.PREFIXE_BONJOUR);
		this.buffDebout = Utilitaires.stringToBuffer(Global.SELF_WAKE_UP);
		this.remoteIndex = 0;
		this.remoteIndex = 0;
	}

	public void run () {
		Message message;
		InetSocketAddress remote;

		while (true) {
			try {
				// Envoie coucou
				remote = getRemote();
				// On envoie bonjour au serveur de l'hôte distant
				channel.send(buffBonjour, remote);
				// On dit au serveur d'attendre une réponse du client de l'hôte distant
				Global.serverPR.expectMessage(new ExpectedMessage(Global.PREFIXE_REPONSE_BONJOUR, new InetSocketAddress(remote.getHostName(), remote.getPort()-1), System.currentTimeMillis() + Global.TIMEOUT));
				// Réveille le serveur si personne d'autre ne lui parle
				channel.send(buffDebout, new InetSocketAddress("localhost", Global.SERVERPRPORT));

				buffDebout.flip();
				buffBonjour.flip();

				// Envoie ce qu'on lui a demandé d'envoyer
				for (int i=0; !toSend.isEmpty() && i<100; i++) {
					message = toSend.poll();
					channel.send(Utilitaires.stringToBuffer(message.body), message.dest);
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

	private InetSocketAddress getRemote () {
		remoteIndex ++;
		return null; // Tricky :/
		//TODO
	}
}
