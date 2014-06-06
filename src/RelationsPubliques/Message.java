package RelationsPubliques;

import java.net.InetSocketAddress;

public class Message {
	String body;
	InetSocketAddress dest;
	
	/**
	 * 
	 * @param body : message à envoyer
	 * @param dest : adresse où on l'envoie
	 */
	
	public Message (String body, InetSocketAddress dest) {
		this.dest = dest;
		this.body = body;
	}
	
	public boolean equals (Object o) {
		return ((Message)o).body.equals(body);
	}
}
