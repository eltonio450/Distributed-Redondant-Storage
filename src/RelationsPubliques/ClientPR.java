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
	private int remoteIndex;
	private int serverPRPort;
	private int sleepTime;
	
	private ConcurrentLinkedQueue<Message> toSend;
	
	
	public ClientPR (int clientPRPort, int serverPRPort, int sleepTime, String messageToSend) throws IOException{
		this.channel = DatagramChannel.open();
		this.channel.socket().bind(new InetSocketAddress(clientPRPort));
		this.buffToSend = Utilitaires.stringToBuffer(messageToSend);
		this.remoteIndex = 0;
		this.serverPRPort = serverPRPort;
		this.sleepTime = sleepTime;
	}
	
	public void run () {
		Message message;
		
		while (!this.isInterrupted()) {
			try {
				// Envoie coucou
				channel.send(buffToSend, getRemote());
				buffToSend.flip();
				
				// Envoie ce qu'on lui a demandé d'envoyer
				while (!toSend.isEmpty()) {
					message = toSend.poll();
					if (message.expirationDate < System.currentTimeMillis())
						channel.send(Utilitaires.stringToBuffer(message.body), message.dest);
				}
				
				Thread.sleep(sleepTime);
				
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("ClientPR Thread just crashed");
				System.exit(-1);
			} catch (InterruptedException e) {
				return;
			}
		}
	}
	
	public synchronized void sendMessage (Message message) {
		toSend.add(message);
	}
	
	private InetSocketAddress getRemote () {
		return new InetSocketAddress("localhost", serverPRPort);
	}
}
