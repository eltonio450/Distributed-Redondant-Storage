package RelationsPubliques;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

import Stockage.Machine;

public class BroadcastAll {
	public static void broadcastAllUDP (String message) {
		// Not useful
	}

	public static void broadcastTCP (String message, LinkedList<Machine> liste) {
		ByteBuffer b = Utilitaires.Utilitaires.stringToBuffer(message);
		for (Machine m : liste) {
			try (SocketChannel clientSocket = SocketChannel.open()) { 
				InetSocketAddress local = new InetSocketAddress(0); 
				clientSocket.bind(local); 
				InetSocketAddress remote = new InetSocketAddress(m.ipAdresse, m.port); 
				clientSocket.connect(remote); 
				clientSocket.write(b);
				clientSocket.close();
			} catch (Exception e) {
				//Who cares ?
			}
		}
	}
}
