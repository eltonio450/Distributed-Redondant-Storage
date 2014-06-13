package RelationsPubliques;

import java.net.InetSocketAddress;

public class Message {
	String body;
	InetSocketAddress dest;

	public Message (String body, InetSocketAddress dest) {
		this.body = body;
		this.dest = dest;
	}
}
