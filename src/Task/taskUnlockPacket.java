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

			// Etape 3 : effectuer le lock si c'est possible.
			b.clear();
			if (!Donnees.getHostedPaquet(id).isAskingTheLock || power < Donnees.getHostedPaquet(id).idInterne) {
				Donnees.getHostedPaquet(id).lock();
				b = Utilitaires.stringToBuffer(Message.OK + " " + Message.END_ENVOI);
				s.write(b);
			}
			else {
				b = Utilitaires.stringToBuffer(Message.FAIL + " " + Message.END_ENVOI);
				s.write(b);
			}

			s.close();

		}
		catch (IOException e) {
			e.printStackTrace();
		}

	}

}
