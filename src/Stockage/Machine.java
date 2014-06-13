package Stockage;

import java.net.InetSocketAddress;

public class Machine {

	public String ipAdresse ;
	public int port;

	public Machine(String addr, int port){
		ipAdresse = addr ;
		this.port = port;
	}

	public Machine(InetSocketAddress m) {
		this.ipAdresse = m.getHostName();
		this.port = m.getPort();
	}
}