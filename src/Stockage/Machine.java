package Stockage;

import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import Utilitaires.Utilitaires;

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
	
	public String toString(){
		return ipAdresse + "-" + port;
	}
	
	public static Machine otherMachineFromSocket(SocketChannel s){
	  String ip = s.socket().getInetAddress().getHostName().toString() ;
	  int p = s.socket().getPort();
	  Utilitaires.out("IP: "+ ip + " Port : "+p);
	  return new Machine(ip,p) ;
	}
	
}