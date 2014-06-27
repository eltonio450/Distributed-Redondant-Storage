package StartUpRoutine;


import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Scanner;

import Stockage.Donnees;
import Utilitaires.Global;
import Utilitaires.Message;
import Utilitaires.Utilitaires;

public class ServerGetter {
	public static void getServerList() {
		if (Global.FIRST_IP.equals(Global.NO_FIRST_SERVER)) {
			return; // premier serveur
		}

		Stockage.Donnees.fillingServers(true);
		
		String [] fins = new String [2];
		fins[0] = Message.END_ENVOI;
		fins[1] = Message.NEXT_BUFFER;

		try (SocketChannel clientSocket = SocketChannel.open()) { 
			InetSocketAddress local = new InetSocketAddress(0); 
			clientSocket.bind(local); 
			InetSocketAddress remote = new InetSocketAddress(Global.FIRST_IP, Global.FIRST_PORT); 
			clientSocket.connect(remote); 

			clientSocket.write(Utilitaires.stringToBuffer(Message.GET_LIST));

			ByteBuffer b = ByteBuffer.allocateDirect(Global.BUFFER_LENGTH);
			String token;
			String liste;
			boolean continuer = true;

			while (continuer) {
				
				liste = Utilitaires.getAFullMessage(fins, clientSocket);
				
				Scanner sc = new Scanner (liste);
				while (sc.hasNext()) {
					token = sc.next();
					if (token.equals(Message.END_ENVOI)) {
						continuer = false;
						break;
					}

					if (token.equals(Message.NEXT_BUFFER)) {
						break;
					}

					if (token.equals(Message.BEGIN)) {
						System.out.println("FUCK BITCH");
						try {
							Donnees.putServer(sc.next(), Integer.parseInt(sc.next()));
						} catch (Exception e) {
							System.out.println("Wapitit");
							// Parsing error - Nobody cares
						}
					}
				}
				sc.close();
			}
			clientSocket.close();
		} catch (Exception e) {
			System.out.println("Fatal error in getServerList");
			e.printStackTrace();
			System.exit(-1);
		}

		Stockage.Donnees.fillingServers(false);
	}
}