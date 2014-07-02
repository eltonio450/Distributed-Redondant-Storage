package GestionnaireMort;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

import Stockage.Donnees;
import Stockage.Machine;
import Utilitaires.Global;
import Utilitaires.Message;
import Utilitaires.Utilitaires;

public class deathVerifier implements Runnable {
	Stockage.Machine m;

	public deathVerifier (Stockage.Machine m) {
		this.m = m;
	}

	public void run () {
		Boolean mort = verifyDeath(m);
		if (mort) {
			LinkedList<Machine> liste = Donnees.getAllServeurs();
			liste.add(Global.MYSELF);
			RelationsPubliques.BroadcastAll.broadcastTCP(Message.IS_DEAD + " " + m.ipAdresse + " " + m.port + " #", liste);
		}
	}
	
	// Vérifie la mort et renvoie oui ou non
	public static boolean verifyDeath (Stockage.Machine m) {
		Boolean mort = true;
		try (SocketChannel clientSocket = SocketChannel.open()) { 
			//Utilitaires.out("Test de la mort 1 : " + m.port);
			InetSocketAddress local = new InetSocketAddress(0); 
			clientSocket.bind(local); 
			//Utilitaires.out("Test de la mort 2 : " + m.port);
			InetSocketAddress remote = new InetSocketAddress(m.ipAdresse, m.port); 
			clientSocket.connect(remote);
			//Utilitaires.out("Test de la mort 3 : " + m.port);
			clientSocket.write(Utilitaires.stringToBuffer(Message.VERIFY_DEATH));
			clientSocket.socket().setSoTimeout(Global.DEATH_TIMEOUT);
			//Utilitaires.out("Test de la mort 4 : " + m.port);
			if (clientSocket.read(ByteBuffer.allocateDirect(1000)) > 0){
				mort = false;
				Utilitaires.out(m.port + " is still alive !",4, true);
			}
				
			clientSocket.close();
		}catch (IOException e){
			Utilitaires.out(m.port + " est mort.");
			mort = true;
		}
		return mort;
	}
}