package Task;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

import Stockage.Donnees;
import Stockage.Machine;
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
			
		
			//Utilitaires.out("Fréquence 4 : " + msg,1,true);
			
			//Utilitaires.out("a recu : " + msg);
			
			Scanner scan = new Scanner(msg) ;
			String machine = scan.next() ;
			String Id = scan.next() ;
			int place = scan.nextInt() ;
			Machine newHost = new Machine(machine) ;
			if(Donnees.getHostedPaquet(Id)!=null &&Donnees.getHostedPaquet(Id).isLocked())
			{
				Utilitaires.out("AAAAAAAAAAAAAAAAAAAAAAAAARRRRRRRRRRRRRRRRRRRRRRRRGGGGGGGGGGGGGGGGGGGG",2,true);
			}
			Donnees.changeHostForPaquet(Id, place, newHost);
			if(Donnees.myOwnData.contains(Id)){  //nous ne sommes pas le propri�taire du paquet
			  Donnees.addHost(Id, newHost) ;
			}
			
			socket.close();
			scan.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}
