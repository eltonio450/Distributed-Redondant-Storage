package RelationsPubliques;

import java.net.InetSocketAddress;

public class ExpectedMessage {
	public final String body;
	public final InetSocketAddress sender;
	public final long timeOut;
	
	public ExpectedMessage(String body, InetSocketAddress sender, long timeOut) {
		this.body = body;
		this.sender = sender;
		this.timeOut= timeOut;
	}
	
	@ Override
	public boolean equals (Object o) {
		return ((ExpectedMessage)o).body.equals(body) && ((ExpectedMessage)o).sender.equals(sender);
	}
	
	@ Override
	public int hashCode () {
		return body.hashCode() ^ sender.hashCode();
	}
}
