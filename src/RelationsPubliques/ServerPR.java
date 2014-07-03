package RelationsPubliques;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;

import GestionnaireMort.deathVerifier;
import Stockage.Machine;
import Utilitaires.Global;
import Utilitaires.Message;
import Utilitaires.Slaver;
import Utilitaires.Utilitaires;
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
	private LinkedList<ExpectedMessage> dead;
	private ReentrantLock expectedMessagesLock;	// Nécessaire pour pouvoir itérer sur expected messages


	public ServerPR () throws IOException{
		this.channel = DatagramChannel.open();
		this.channel.bind(new InetSocketAddress("localhost", Global.SERVERPRPORT));
		this.channel.socket().setSoTimeout(0);
		this.receivedMessage = ByteBuffer.allocateDirect(10000);
		this.expectedMessagesLock = new ReentrantLock();
		this.expectedMessages = new LinkedList<ExpectedMessage>();
		this.dead = new LinkedList<ExpectedMessage> ();
	}

	public void run () {
		Utilitaires.out("UDP Server on port " + Global.SERVERPRPORT);
		try {
			channel.socket().setSoTimeout(Global.SLEEPTIME);
		} catch (SocketException e1) {
			e1.printStackTrace();
			System.exit(-5);
		}
		while (true) {
			try {
				try {
				sender = (InetSocketAddress) channel.receive(receivedMessage);
				} catch (SocketTimeoutException e) {}
				receivedMessage.flip(); 

				try {
					traiter (Utilitaires.buffToString(receivedMessage), sender);
				} catch (Exception e) {
					e.printStackTrace();
				}

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
		Utilitaires.out("from  " + sender.getPort() + " : " + message, 4, false);
		Scanner sc = new Scanner (message);
		String token = sc.next();

		if (token.equals(Message.PREFIXE_BONJOUR)) {
			//Utilitaires.out(sender.getAddress() + ":" + sender.getPort() +" making sure we're still alive.");
			// On dit au client de répondre au serveur de l'hôte distant
			Global.clientPR.sendMessage(new Message (Message.PREFIXE_REPONSE_BONJOUR, new InetSocketAddress(sender.getHostName(), sender.getPort()+1)));
		}
		else if (token.equals(Message.PREFIXE_REPONSE_BONJOUR)) {
			// On a eu une réponse au bonjour
			expectedMessagesLock.lock();
			int i = expectedMessages.size();
			if (i==0){
				Utilitaires.out("WARNING Reply received while non was expected :/");
				sc.close();
				expectedMessagesLock.unlock();
				return;
			}
			expectedMessages.remove(new ExpectedMessage(message, sender, 0));
			if (i == expectedMessages.size())
				Utilitaires.out("WARNING Didnt remove expected message :/");
			expectedMessagesLock.unlock();

		}
		sc.close();
	}

	private void checkExpectedMessages() {
		long t = System.currentTimeMillis();
		expectedMessagesLock.lock();
		for (ExpectedMessage m : expectedMessages) {
			if (m.timeOut < t) {
				dead.add(m);
			}
		}
		expectedMessages.removeAll(dead);
		expectedMessagesLock.unlock();

		while (!dead.isEmpty()) {
			InetSocketAddress toCheck = dead.removeFirst().sender;

			Slaver.giveTask(new deathVerifier(new Machine(toCheck.getAddress().getHostAddress(), toCheck.getPort()-1)), 10);
		}
	}
}
