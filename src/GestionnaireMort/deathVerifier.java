package GestionnaireMort;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import Utilitaires.*;

public class deathVerifier implements Runnable {
		Stockage.Machine m;

		public deathVerifier (Stockage.Machine m) {
			this.m = m;
		}

		public void run () {
			Boolean pasMort = false;
			try (SocketChannel clientSocket = SocketChannel.open()) { 
				InetSocketAddress local = new InetSocketAddress(0); 
				clientSocket.bind(local); 
				InetSocketAddress remote = new InetSocketAddress(m.ipAdresse, m.port); 
				clientSocket.connect(remote);
				clientSocket.write(Utilitaires.stringToBuffer(Global.VERIFY_DEATH));
				clientSocket.socket().setSoTimeout(Global.DEATH_TIMEOUT);
				if (clientSocket.read(ByteBuffer.allocateDirect(10)) > 0)
					pasMort = true;
			} catch (IOException e) {
				// Il est bel et bien mort (ou je suis déconnecté et il faut relancer l'appli)
			}
			if (!pasMort) {
				// Broadcast death
			}
		}
	}