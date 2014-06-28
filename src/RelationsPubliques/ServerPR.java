package RelationsPubliques;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;

import GestionnaireMort.deathVerifier;
import Stockage.Machine;
import Utilitaires.Global;
import Utilitaires.Slaver;
import Utilitaires.Utilitaires;
import Utilitaires.Message;
/**
 * 
 * @author Simon
 * Serveur qui envoie des messages UDP
 * Pas nécessairement la meilleure idée du monde
 *
 */

public class ServerPR extends Thread{
	private DatagramChannel channel;
	private ByteBuffer receivedMessage;
	private InetSocketAddress sender;

	private LinkedList<ExpectedMessage> expectedMessages;
	private LinkedList<InetSocketAddress> dead;
	private ReentrantLock expectedMessagesLock;	// Nécessaire pour pouvoir itérer sur expected messages


	public ServerPR () throws IOException{
		this.channel = DatagramChannel.open();
		this.channel.bind(new InetSocketAddress("localhost", Global.SERVERPRPORT));
		this.receivedMessage = ByteBuffer.allocateDirect(10000);
		this.expectedMessagesLock = new ReentrantLock();
		this.expectedMessages = new LinkedList<ExpectedMessage>();
		this.dead = new LinkedList<InetSocketAddress> ();
	}

	public void run () {
		Utilitaires.out("UDP Server on port " + Global.SERVERPRPORT);
		while (true) {
			try {
				sender = (InetSocketAddress) channel.receive(receivedMessage);
				receivedMessage.flip();

				try {
					traiter (Utilitaires.buffToString(receivedMessage), sender);
				} catch (Exception e) {}
				
				receivedMessage.clear();
				
				checkExpectedMessages();

			} catch (IOException e) {
				e.printStackTrace();
				Utilitaires.out("ServerPR just crashed");
				System.exit(-2);
			}
		}
	}

	public void expectMessage (ExpectedMessage message) {
		expectedMessagesLock.lock();
			expectedMessages.add(message);
		expectedMessagesLock.unlock();
	}

	private void traiter (String message, InetSocketAddress sender) throws Exception {
		Scanner sc = new Scanner (message);
		String token = sc.next();
		
		if (token.equals(Message.PREFIXE_BONJOUR)) {
			Utilitaires.out(sender.getAddress() + ":" + sender.getPort() +" making sure we're still alive.");
			// On dit au client de répondre au serveur de l'hôte distant
			Global.clientPR.sendMessage(new Message (Message.PREFIXE_REPONSE_BONJOUR, new InetSocketAddress(sender.getHostName(), sender.getPort()+1)));
			// On met à jour l'attente de bonjour du client
			expectedMessages.remove(new ExpectedMessage(Message.PREFIXE_BONJOUR, sender, 0));
			expectedMessages.add(new ExpectedMessage(Message.PREFIXE_BONJOUR, sender, System.currentTimeMillis()+Global.TIMEOUT));
		}
		else if (token.equals(Message.PREFIXE_REPONSE_BONJOUR)) {
			// On a eu une réponse au bonjour
			expectedMessages.remove(new ExpectedMessage(message, sender, 0));
		}
		sc.close();
	}
	
	private void checkExpectedMessages() {
		long t = System.currentTimeMillis();
		expectedMessagesLock.lock();
		for (ExpectedMessage m : expectedMessages) {
			if (m.timeOut < t) {
				dead.add(m.sender);
			}
		}
		expectedMessagesLock.unlock();
		
		while (!dead.isEmpty()) {
			InetSocketAddress toCheck = dead.removeFirst();
			
			Slaver.giveTask(new deathVerifier(new Machine(new InetSocketAddress(toCheck.getAddress(), toCheck.getPort()-1))), 10);
		}
	}
}
