package RelationsPubliques;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

import Utilitaires.Utilitaires;

public class ClientPR extends Thread{
	private DatagramChannel channel;
	private ByteBuffer buffToSend;
	private String stringSent;
	private int remoteIndex;
	private int sleepTime;
	private ServerPR serveurPR;

	private ConcurrentLinkedQueue<Message> toSend;


	public ClientPR (int sleepTime, String messageToSend, ServerPR serveurPR) throws IOException{
		this.channel = DatagramChannel.open();
		this.channel.socket().bind(new InetSocketAddress(Global.CLIENTPRPORT));
		this.buffToSend = Utilitaires.stringToBuffer(messageToSend);
		this.remoteIndex = 0;
		this.sleepTime = sleepTime;
		this.serveurPR = serveurPR;
	}

	public void run () {
		Message message;
		stringSent = Utilitaires.bufferToString(buffToSend);

		while (true) {
			try {
				// Envoie coucou
				channel.send(buffToSend, getRemote());
				
			
				buffToSend.flip();

				// Envoie ce qu'on lui a demandé d'envoyer
				for (int i=0; !toSend.isEmpty() && i<100; i++) {
					message = toSend.poll();
					if (message.expirationDate < System.currentTimeMillis())
						channel.send(Utilitaires.stringToBuffer(message.body), message.dest);
				}

				if (toSend.isEmpty()) {
					try {
						Thread.sleep(sleepTime);
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
		return new InetSocketAddress("localhost", Global.SERVERPRPORT);
	}
}
