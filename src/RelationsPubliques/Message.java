package RelationsPubliques;

import java.net.InetSocketAddress;

public class Message {
	public String body;
	public InetSocketAddress dest;

	public Message (String body, InetSocketAddress dest) {
		this.body = body;
		this.dest = dest;
	}
}
