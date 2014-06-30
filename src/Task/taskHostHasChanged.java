package Task;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

import Stockage.Donnees;
import Stockage.Machine;
import Utilitaires.Global;
import Utilitaires.Message;
import Utilitaires.Utilitaires;

public class taskHostHasChanged implements Runnable {

	SocketChannel socket ;

	public taskHostHasChanged(SocketChannel s){
		socket = s ;
	}
	
	/** TODO :
	 * 
	 * FERMER LA SOCKET après utilisation
	 *
	 */

	public void run() {
		try{
		  //Utilitaires.out("-------------------taskHostChanged-------------------") ;
			
			
			ByteBuffer buffer = Utilitaires.stringToBuffer(Message.OK) ;
			socket.write(buffer) ;
			//Utilitaires.out("Ici il a répondu");
			String[] t = new String[1] ;
			t[0] = Message.END_ENVOI ;
			String msg = Utilitaires.getAFullMessage(t, socket);
			
			//Utilitaires.out("a recu : " + msg);
			
			Scanner scan = new Scanner(msg) ;
			String machine = scan.next() ;
			String Id = scan.next() ;
			int place = scan.nextInt() ;
			Machine newHost = new Machine(machine) ;
			Donnees.changeHostForPaquet(Id, place, newHost);
			if(Donnees.myOwnData.contains(Id)){  //nous ne sommes pas le propri�taire du paquet
			  Donnees.addHost(Id, newHost) ;
			}
			
			socket.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}
