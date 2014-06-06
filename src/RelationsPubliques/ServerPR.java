package RelationsPubliques;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import Utilitaires.Utilitaires;

/*TODO : 
 * Vérifier les messages
 */

public class ServerPR extends Thread{
	private DatagramChannel channel;
	private ByteBuffer receivedMessage;
	private InetSocketAddress sender;
	private ClientPR clientPR;

	private ConcurrentHashMap<ExpectedMessage, Long> expectedMessages;


	public ServerPR (ClientPR clientPR) throws IOException{
		this.channel = DatagramChannel.open();
		this.channel.bind(new InetSocketAddress("localhost", Global.SERVERPRPORT));
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
				
				checkExpectedMessages();

			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("ServerPR just crashed");
				System.exit(-2);
			}
		}
	}

	public void expectMessage (ExpectedMessage message) {
		expectedMessages.put(message, message.timeOut);
	}

	private void traiter (String message, InetSocketAddress sender) throws Exception {
		Scanner sc = new Scanner (message);
		String token = sc.next();
		
		if (token.equals(Global.PREFIXE_BONJOUR)) {
			// On dit au client de répondre au serveur de l'hôte distant
			clientPR.sendMessage(new Message (Global.PREFIXE_REPONSE_BONJOUR, new InetSocketAddress(sender.getHostName(), sender.getPort()+1)));
			// On met à jour l'attente de bonjour du client
			expectedMessages.remove(new ExpectedMessage(Global.PREFIXE_BONJOUR, sender, 0));
			expectedMessages.put(new ExpectedMessage(Global.PREFIXE_BONJOUR, sender, System.currentTimeMillis()+Global.TIMEOUT), System.currentTimeMillis()+Global.TIMEOUT);
		}
		else if (token.equals(Global.PREFIXE_REPONSE_BONJOUR)) {
			// On a eu une réponse au bonjour
			expectedMessages.remove(new ExpectedMessage(message, sender, 0));
		}
		sc.close();
	}
	
	private void checkExpectedMessages() {
		
	}
}