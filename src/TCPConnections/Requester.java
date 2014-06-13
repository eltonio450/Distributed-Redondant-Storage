package TCPConnections;

import java.nio.channels.SocketChannel;

public class Requester {
	public SocketChannel socket;
	public String recu;
	
	public Requester (SocketChannel socket) {
		recu = new String();
		this.socket = socket;
	}
}
