package RelationsPubliques;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;

import Stockage.Machine;
import GestionnaireMort.deathVerifier;

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
		this.channel.bind(new InetSocketAddress("localhost", Utilitaires.Global.SERVERPRPORT));
		this.receivedMessage = ByteBuffer.allocateDirect(10000);
		this.expectedMessagesLock = new ReentrantLock();
		this.expectedMessages = new LinkedList<ExpectedMessage>();
		this.dead = new LinkedList<InetSocketAddress> ();
	}

	public void run () {
		System.out.println("UDP Server on port " + Utilitaires.Global.SERVERPRPORT);
		while (true) {
			try {
				sender = (InetSocketAddress) channel.receive(receivedMessage);
				receivedMessage.flip();

				try {
					traiter (Utilitaires.Utilitaires.buffToString(receivedMessage), sender);
				} catch (Exception e) {}
				
				receivedMessage.clear();
				
				checkExpectedMessages();

			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("ServerPR just crashed");
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
		System.out.println(message);
		Scanner sc = new Scanner (message);
		String token = sc.next();
		
		if (token.equals(Utilitaires.Message.PREFIXE_BONJOUR)) {
			System.out.println(sender.getAddress() + ":" + sender.getPort() +" making sure we're still alive.");
			// On dit au client de répondre au serveur de l'hôte distant
			Utilitaires.Global.clientPR.sendMessage(new Message (Utilitaires.Message.PREFIXE_REPONSE_BONJOUR, new InetSocketAddress(sender.getHostName(), sender.getPort()+1)));
			// On met à jour l'attente de bonjour du client
			expectedMessages.remove(new ExpectedMessage(Utilitaires.Message.PREFIXE_BONJOUR, sender, 0));
			expectedMessages.add(new ExpectedMessage(Utilitaires.Message.PREFIXE_BONJOUR, sender, System.currentTimeMillis()+Utilitaires.Global.TIMEOUT));
		}
		else if (token.equals(Utilitaires.Message.PREFIXE_REPONSE_BONJOUR)) {
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
			
			Utilitaires.Slaver.giveTask(new deathVerifier(new Machine(new InetSocketAddress(toCheck.getAddress(), toCheck.getPort()-1))), 10);
		}
	}
}