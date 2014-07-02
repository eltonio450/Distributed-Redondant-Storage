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
		Utilitaires.out("ACK 53");
		
	}

	public void run() {
		// Etape 1 : renvoyer le message d'ACK
		Utilitaires.out("Très RAS tout ça 1",0,true);
		String id;
		int power;
		ByteBuffer b = Utilitaires.stringToBuffer(Message.OK);

		try {
			Utilitaires.out("Très RAS tout ça 2",0,true);
			s.write(b);

			Utilitaires.out("Très RAS tout ça 2",0,true);
			b.clear();
			s.read(b);
			b.flip();
			Utilitaires.out("Très RAS tout ça 3",0,true);
			//temp = Utilitaires.getAFullMessage(Message.END_ENVOI, s);
			String chaine = Utilitaires.buffToString(b);
			//Utilitaires.out("Le lock est demandé sur "+chaine,4,true);
			Scanner scan = new Scanner(chaine);
			id = scan.next();
			power = Integer.parseInt(scan.next());
			//Utilitaires.out("Très RAS tout ça 4",0,true);
			b.clear();

		
				if(Donnees.securedLock(id)){
					
					b = Utilitaires.stringToBuffer(Message.OK);
					s.write(b);

				
				}
				else{
	
					b = Utilitaires.stringToBuffer(Message.FAIL);
					s.write(b);
					
				}
			
				
		

			s.close();
			scan.close();

		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		

	}

}
