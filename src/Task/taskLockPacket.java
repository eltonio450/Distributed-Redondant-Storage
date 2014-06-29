package Task;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

import Stockage.Donnees;
import Utilitaires.Message;
import Utilitaires.Utilitaires;

public class taskLockPacket implements Runnable {
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

	public taskLockPacket(SocketChannel socket) {
		s = socket;
	}

	public void run() {
		// Etape 1 : renvoyer le message d'ACK

		String temp;
		String id;
		int power;
		ByteBuffer b = Utilitaires.stringToBuffer(Message.OK);

		try {
			//Utilitaires.out("Demande de lock reçue.",4,true);
			s.write(b);

			// Etape 2 : attendre de recevoir l'identifiant du paquet qui
			// demande le lock
			b.clear();
			s.read(b);
			b.flip();
			//temp = Utilitaires.getAFullMessage(Message.END_ENVOI, s);
			String chaine = Utilitaires.buffToString(b);
			//Utilitaires.out("Le lock est demandé sur "+chaine,4,true);
			Scanner scan = new Scanner(chaine);
			id = scan.next();
			power = Integer.parseInt(scan.next());
			
			Utilitaires.out("Le lock est demandé sur "+id+ " "+power,4,true);
			// Etape 3 : effectuer le lock si c'est possible.
			b.clear();
		
			//Donnees.getHostedPaquet(id);
			if (Donnees.getHostedPaquet(id)!=null && (!Donnees.getHostedPaquet(id).isAskingTheLock 
					|| power > Donnees.getHostedPaquet(id).idInterne)) {
				Utilitaires.out("Test 8"+power,4,true);
				Donnees.getHostedPaquet(id).lock();
				Utilitaires.out("Demande de lock acceptée.",4,true);
				b = Utilitaires.stringToBuffer(Message.OK);
				s.write(b);
			}
			else {
				Utilitaires.out("Je refuse de donner le lock.",4,true);
				b = Utilitaires.stringToBuffer(Message.FAIL);
				s.write(b);
			}

			s.close();

		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch(Exception e){
			e.printStackTrace();
		}

	}

}
