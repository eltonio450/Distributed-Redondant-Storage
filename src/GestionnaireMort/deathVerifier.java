package GestionnaireMort;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import Utilitaires.*;

public class deathVerifier implements Runnable {
		Stockage.Machine m;

		public deathVerifier (Stockage.Machine m) {
			this.m = m;
		}

		public void run () {
			try (SocketChannel clientSocket = SocketChannel.open()) { 
				InetSocketAddress local = new InetSocketAddress(0); 
				clientSocket.bind(local); 
				InetSocketAddress remote = new InetSocketAddress(m.ipAdresse, m.port); 
				clientSocket.connect(remote);
				clientSocket.write(Utilitaires.stringToBuffer(Global.VERIFY_DEATH));
			} catch (IOException e) {
				// Broadcast death
			}
		}
	}