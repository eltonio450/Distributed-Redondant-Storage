package Task;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
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
		Donnees.getHostedPaquet(ID).otherHosts.set(Donnees.getHostedPaquet(ID).idInterne,Global.MYSELF);
		if(!Donnees.getHostedPaquet(ID).isLocked())
			Utilitaires.out("Fatal Error",1,true);
		Donnees.getHostedPaquet(ID).spreadTotalUnlock();
	}

	public static void prevenirHostChanged(String id) {
		// previens une machine que cette machine remplace m pour le paquet d'id
		// Id
		SocketChannel clientSocket;
		Paquet p = Donnees.getHostedPaquet(id);
		int placeToModify = p.power;

		LinkedList<String> table = new LinkedList<String>();
		for (int i = 0; i < Global.NOMBRESOUSPAQUETS; i++) {
			//if (i != placeToModify) {
				Machine m = p.otherHosts.get(i);
				if (!table.contains(m.toString())) {
					table.add(m.toString());
				}
			//}

			//if (!table.contains(p.owner.toString())) {
				table.add(p.owner.toString());
			//}

		}
		while (!table.isEmpty()) {
			String s2 = table.poll();
			Scanner scan = new Scanner(s2);
			scan.useDelimiter("-");
			Machine m = new Machine(scan.next(), scan.nextInt());

	
			try {
				clientSocket = SocketChannel.open();
				// init connection
				InetSocketAddress local = new InetSocketAddress(0);
				clientSocket.bind(local);
				InetSocketAddress remote = new InetSocketAddress(m.ipAdresse, m.port);
				if (!clientSocket.connect(remote))
					Utilitaires.out("Ca a foiré !");

				// message
				ByteBuffer buffer = Utilitaires.stringToBuffer(Message.HOST_CHANGED);
				
				clientSocket.write(buffer);
				buffer.clear();

				clientSocket.read(buffer);

				buffer.flip();
				String response = Utilitaires.buffToString(buffer);

				if (response.equals(Message.OK)) {

					String s = Global.MYSELF.toString() + " " + id + " " + placeToModify + " " + Message.END_ENVOI + " ";
					// buffer.flip();

					buffer = Utilitaires.stringToBuffer(s);
					clientSocket.write(buffer);

				}

				clientSocket.close();

			}
			catch (IOException e) {
				e.printStackTrace();
			}
			finally {
				scan.close();
			}

		}
		
	}
	

}
