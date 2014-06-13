package RelationsPubliques;

import java.net.InetSocketAddress;
import RelationsPubliques.*;

public class Message {
	String body;
	InetSocketAddress dest;
	long expirationDate;
	
	/**
	 * 
	 * @param body : message à envoyer
	 * @param dest : adresse où on l'envoie
	 * @param expirationDate : temps en ms à partir duquel on ne l'envoie plus (System.currentTimemillis())
	 */
	
	public Message (String body, InetSocketAddress dest, long expirationDate) {
		this.dest = dest;
		this.body = body;
		this.expirationDate = expirationDate;
		
		//Gestion du débuggage. 
	}
	
	public boolean equals (Object o) {
		return ((Message)o).body.equals(body);
	}
}
