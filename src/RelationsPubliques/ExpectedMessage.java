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
}
