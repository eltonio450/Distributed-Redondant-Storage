package RelationsPubliques;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import Utilitaires.Utilitaires;

public class ServerPR extends Thread{
	private DatagramChannel channel;
	private ByteBuffer receivedMessage;
	private InetSocketAddress sender;
	private ClientPR clientPR;

	private ConcurrentHashMap<Message, Long> expectedMessages;


	public ServerPR (int serverPRPort, ClientPR clientPR) throws IOException{
		this.channel = DatagramChannel.open();
		this.channel.bind(new InetSocketAddress("localhost", serverPRPort));
		this.receivedMessage = ByteBuffer.allocateDirect(10000);
		this.clientPR = clientPR;
	}

	public void run () {
		while (true) {
			try {
				sender = (InetSocketAddress) channel.receive(receivedMessage);
				receivedMessage.flip();

				try {
					traiter (Utilitaires.buffToString(receivedMessage), sender);
				} catch (Exception e) {}

			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("ServerPR just crashed");
				System.exit(-2);
			}
		}
	}

	public void expectMessage (Message message) {
		expectedMessages.put(message, message.expirationDate);
	}

	public void traiter (String message, InetSocketAddress sender) throws Exception {
		Scanner sc = new Scanner (message);
		String token = sc.next();
		
		if (token.equals(Global.PREFIXE_BONJOUR)) {
			clientPR.sendMessage(new Message (Global.PREFIXE_REPONSE_BONJOUR, new InetSocketAddress(sender.getHostName(), Global.CLIENTPRPORT), Long.MAX_VALUE));
		}
		else if (token.equals(Global.PREFIXE_REPONSE_BONJOUR)) {
			if (expectedMessages.contains(new Message (message, sender, 0)));
		}
	}
}