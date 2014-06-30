package Task;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Scanner;

import Stockage.Donnees;
import Stockage.Machine;
import Stockage.Paquet;
import Utilitaires.Global;
import Utilitaires.Message;
import Utilitaires.Utilitaires;

public class taskWarnHostChanged implements Runnable {

	String ID;

	public taskWarnHostChanged(String id) {
		ID = id;
	}

	public void run() {
		// Utilitaires.out("-------------task warn host changed--------------------");
		prevenirHostChanged(ID);
		Donnees.getHostedPaquet(ID).unlock();
	}

	public static void prevenirHostChanged(String id) {
		// pr�viens une machine que cette machine remplace m pour le paquet d'id
		// Id
		SocketChannel clientSocket;
		Paquet p = Donnees.getHostedPaquet(id);
		int placeToModify = p.power;

		LinkedList<String> table = new LinkedList<String>();
		for (int i = 0; i < Global.NOMBRESOUSPAQUETS; i++) {
			if (i != placeToModify) {
				Machine m = p.otherHosts.get(i);
				if (!table.contains(m.toString())) {
					table.add(m.toString());
				}
			}
			else {
				if (!table.contains(p.owner.toString())) {
					table.add(p.owner.toString());
				}
			}
		}
		while (!table.isEmpty()) {
			String s2 = table.poll();
			Scanner scan = new Scanner(s2);
			scan.useDelimiter("-");
			Machine m = new Machine(scan.next(), scan.nextInt());

			/*
			 * HashSet<Machine> listeM = new HashSet<Machine>(); for (int i = 0;
			 * i < 5; i++) {
			 * 
			 * if (i != placeToModify) {
			 * 
			 * listeM.add(p.otherHosts.get(i)); // Utilitaires.out("Place : " +
			 * placeToModify + " "+ // m.toString()); } else {
			 * listeM.add(p.owner); } } for (Machine m : listeM) {
			 * Utilitaires.out("C'est pour m : " +m.toString()); }
			 */

			try {
				clientSocket = SocketChannel.open();
				// init connection
				InetSocketAddress local = new InetSocketAddress(0);
				clientSocket.bind(local);
				InetSocketAddress remote = new InetSocketAddress(m.ipAdresse, m.port);
				if (!clientSocket.connect(remote))
					Utilitaires.out("Ca a foiré !");
				// clientSocket.configureBlocking(true);

				// message
				ByteBuffer buffer = Utilitaires.stringToBuffer(Message.HOST_CHANGED);
				ByteBuffer buffer2 = Utilitaires.stringToBuffer(Message.HOST_CHANGED);
				Utilitaires.out(Utilitaires.buffToString(buffer2));
				clientSocket.write(buffer);
				buffer.clear();
				//Utilitaires.out("Test 235");
				clientSocket.read(buffer);
				//Utilitaires.out("Test 236");
				buffer.flip();
				String response = Utilitaires.buffToString(buffer);

				if (response.equals(Message.OK)) {
					String s = Global.MYSELF.toString() + " " +id + " " + placeToModify + " " + Message.END_ENVOI;
					// buffer.flip();
					buffer = Utilitaires.stringToBuffer(s);
					clientSocket.write(buffer);

					//Utilitaires.out("Test 237");
				}
				//Utilitaires.out("Test 238");
				//Donnees.getHostedPaquet(id).unlock();
				clientSocket.close();

			}
			catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

}
