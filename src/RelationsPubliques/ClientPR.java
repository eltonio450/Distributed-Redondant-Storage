package RelationsPubliques;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class ClientPR extends Thread{
	DatagramChannel channel;
	
	public ClientPR (int PRPort) throws IOException{
		channel = DatagramChannel.open();
		channel.socket().bind(new InetSocketAddress(PRPort));
	}
	
	public void run () {
		ByteBuffer buff = ByteBuffer.allocateDirect(200000);
		
		while (true) {
			try {
				channel.receive(buff);
				buff.flip();
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("ClientPR Thread just crashed");
				System.exit(-1);
			}			
		}
	}
}
