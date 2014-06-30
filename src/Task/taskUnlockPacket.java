package Task;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

import Stockage.Donnees;
import Utilitaires.Message;
import Utilitaires.Utilitaires;

public class taskUnlockPacket implements Runnable {
	SocketChannel s;

	/**
	 * @author Antoine
	 * 
	 * @param s
	 *            : Socket sur lequel la communication se fait
	 * 

	/**
	 * TODO :
	 * 
	 * FERMER LA SOCKET après utilisation
	 * 
	 */

	public taskUnlockPacket(SocketChannel socket) {
		s = socket;
	}

	public void run() {
		// Etape 1 : renvoyer le message d'ACK

		String temp;
		String id;
		int power;
		ByteBuffer b = Utilitaires.stringToBuffer(Message.OK);

		try {
			s.write(b);

			// Etape 2 : attendre de recevoir l'identifiant du paquet qui
			// demande le lock
			temp = Utilitaires.getAFullMessage(Message.END_ENVOI, s);
			Scanner scan = new Scanner(temp);
			id = scan.next();
			power = Integer.parseInt(scan.next());

			// Etape 3 : effectuer le unlock si c'est possible.
			b.clear();
			Utilitaires.out("L'unlock a fonctioné !");
			Donnees.getHostedPaquet(id).unlock();
				

			s.close();

		}
		catch (IOException e) {
			e.printStackTrace();
		}

	}

}
