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
			
		  Machine newHost = Machine.otherMachineFromSocket(socket) ;
			ByteBuffer buffer = Utilitaires.stringToBuffer(Message.OK) ;
			socket.write(buffer) ;
		
			String[] t = new String[1] ;
			t[0] = Message.END_ENVOI ;
			String msg = Utilitaires.getAFullMessage(t, socket);
			
			//Utilitaires.out("a recu : " + msg);
			
			Scanner scan = new Scanner(msg) ;
			String Id = scan.next() ;
			int place = scan.nextInt() ;
			
			if(!Donnees.myOwnData.contains(Id)){  //nous ne sommes pas le propri�taire du paquet
			  Donnees.changeHostForPaquet(Id, place, newHost);
			}
			else{  //nous sommes le propri�taire du paquet
			  Donnees.addHost(Id, newHost) ;
			}
			
			socket.close();
		}
		catch(Exception e){
			//TODO : what can we do ???
		}
	}
}
