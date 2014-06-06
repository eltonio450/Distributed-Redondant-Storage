package RelationsPubliques;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import Utilitaires.Utilitaires;

public class ClientPR extends Thread{
	private DatagramChannel channel;
	private ByteBuffer buffToSend;
	private int remoteIndex;
	private int serverPRPort;
	private int sleepTime;
	
	
	public ClientPR (int clientPRPort, int serverPRPort, int sleepTime, String messageToSend) throws IOException{
		this.channel = DatagramChannel.open();
		this.channel.socket().bind(new InetSocketAddress(clientPRPort));
		this.buffToSend = Utilitaires.stringToBuffer(messageToSend);
		this.remoteIndex = 0;
		this.serverPRPort = serverPRPort;
		this.sleepTime = sleepTime;
	}
	
	public void run () {		
		while (!this.isInterrupted()) {
			try {
				channel.send(buffToSend, getRemote());
				buffToSend.flip();
				
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
	
	private InetSocketAddress getRemote () {
		return new InetSocketAddress("localhost", serverPRPort);
	}
}
