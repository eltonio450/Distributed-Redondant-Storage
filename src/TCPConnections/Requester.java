package TCPConnections;

import java.nio.channels.SocketChannel;

public class Requester {
	public SocketChannel socket;
	public String recu;
	public final long timeIni;
	
	public Requester (SocketChannel socket) {
		recu = new String();
		this.socket = socket;
		timeIni = System.currentTimeMillis();
	}
}
