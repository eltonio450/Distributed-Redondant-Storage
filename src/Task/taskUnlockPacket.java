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
		//Utilitaires.out("L'unlock est lààààà !");
		String temp;
		String id;
		ByteBuffer b = Utilitaires.stringToBuffer(Message.OK);
		//Utilitaires.out("Test 234");
		try {
			s.write(b);
			
			b.clear();

			s.read(b);

			b.flip();
			//temp = Utilitaires.getAFullMessage(Message.END_ENVOI, s);
			temp = Utilitaires.buffToString(b);

			Scanner scan = new Scanner(temp);
			id = scan.next();
			//System.out.println("C'est l'ID récupéré :"+id);

			b.clear();
			//Utilitaires.out("Attention préparation de l'unlock de "+id);
			
			Donnees.printMyData();
			Donnees.printUnlockedInMyData();
			if(Donnees.getHostedPaquet(id)!=null)
				Donnees.getHostedPaquet(id).unlock();
				
			scan.close();
			s.close();

		}
		catch (IOException e) {
			e.printStackTrace();
		}
		

	}

}
