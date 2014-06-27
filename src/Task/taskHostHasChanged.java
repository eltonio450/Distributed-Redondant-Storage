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
			Machine newHost = Machine.otherMachineFromSocket(socket) ;
			ByteBuffer buffer = Utilitaires.stringToBuffer(Message.OK) ;
			socket.write(buffer) ;
			buffer.clear() ;
			socket.read(buffer) ;
			buffer.flip() ;
			String msg = "" ;
			String s = Utilitaires.buffToString(buffer) ;
			int i = 0 ;
			Scanner scan = new Scanner(s) ;
			while(i != 2){
				if(scan.hasNext()){
					i++ ;
					msg = msg + scan.next() ;
				}
				else {
					Thread.sleep(Message.TIME_TO_SLEEP_1);
					buffer.clear();
					socket.read(buffer) ;
					s = s + Utilitaires.buffToString(buffer) ;
				}
			}
			Scanner scan2 = new Scanner(msg) ;
			String Id = scan2.next() ;
			int place = scan2.nextInt() ;
			Donnees.changeHostForPaquet(Id, place, newHost);
		}
		catch(Exception e){
			//TODO : what can we do ???
		}
	}
}
