package Stockage;

import java.net.InetSocketAddress;

public class Machine {

	String IpAdresse ;
	int port;

	public Machine(String addr, int port){
		IpAdresse = addr ;
		this.port = port;
	}

	public Machine(InetSocketAddress m) {
		this.IpAdresse = m.getHostName();
		this.port = m.getPort();
	}
}