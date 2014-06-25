package StartUpRoutine;


import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

import Stockage.Donnees;
import Utilitaires.Global;
import Utilitaires.Utilitaires;

public class ServerGetter {
	public static void getServerList() {
		if (Global.FIRST_IP.equals("127.0.0.1")) {
			return; // premier serveur
		}
		
		try (SocketChannel clientSocket = SocketChannel.open()) { 
			InetSocketAddress local = new InetSocketAddress(0); 
			clientSocket.bind(local); 
			InetSocketAddress remote = new InetSocketAddress(Global.FIRST_IP, Global.FIRST_PORT); 
			clientSocket.connect(remote); 

			clientSocket.write(Utilitaires.stringToBuffer(Global.GET_LIST));

			ByteBuffer b = ByteBuffer.allocateDirect(Global.BUFFER_LENGTH);
			String message, token;
			boolean continuer = true;

			while (continuer) {
				clientSocket.read(b);
				message = Utilitaires.buffToString(b);
				Scanner sc = new Scanner(message);
				while (sc.hasNext()) {
					token = sc.next();
					if (token.equals(Global.END_ENVOI)) {
						continuer = false;
						break;
					}
					
					if (token.equals(Global.NEXT_BUFFER)) {
						break;
					}

					if (token.equals(Global.BEGIN)) {
						try {
							Donnees.putServer(sc.next(), Integer.parseInt(sc.next()));
						} catch (Exception e) {
							// Parsing error - Nobody Cares
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
	}
}