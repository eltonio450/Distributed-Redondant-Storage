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
		ByteBuffer b = Utilitaires.stringToBuffer(Message.OK);
		try {

			s.write(b);
			
			b.clear();

			s.read(b);

			b.flip();

			temp = Utilitaires.buffToString(b);

			Scanner scan = new Scanner(temp);
			id = scan.next();


			b.clear();
			
			Donnees.securedUnlock(id);


			ByteBuffer c = Utilitaires.stringToBuffer(Message.OK);
			s.write(c);
			scan.close();
			//s.close();

		}
		catch (Exception e) {
			e.printStackTrace();
		}finally{
			try {
				s.close();
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		

	}

}
