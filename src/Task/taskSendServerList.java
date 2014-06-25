package Task;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

import Stockage.Donnees;
import Stockage.Machine;
import Utilitaires.Global;
import Utilitaires.Utilitaires;

public class taskSendServerList implements Runnable {
	SocketChannel s;
	
	public taskSendServerList (SocketChannel s) {
		this.s = s;
	}
	
	public void run () {
		String message;
		LinkedList<Stockage.Machine> servers = Donnees.getAllServeurs();
		
		while (true) {
			message = new String ();
			
			while (message.length() < Global.BUFFER_LENGTH/10 && !servers.isEmpty()) {
				Machine m = servers.pop();
				message += Global.BEGIN + " " + m.ipAdresse + " " + m.port + " ";
			}
			
			if (servers.isEmpty()) {
				message += Global.END_ENVOI;
				break;
			}
			else
				message += Global.NEXT_BUFFER;
			
			try {
				s.write(Utilitaires.stringToBuffer(message));
			} catch (IOException e) {
				return;
			}
		}
		
		TCPConnections.Broadcast.broadcastAll(Global.NEW_SERVER + " " + s.socket().getRemoteSocketAddress() + " " + s.socket().getLocalPort(), );
	}
}