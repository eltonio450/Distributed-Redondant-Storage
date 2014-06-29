
package Task;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import Stockage.Donnees;
import Utilitaires.Global;
import Utilitaires.Message;
import Utilitaires.Utilitaires;

public class taskSendRequestedPaquet implements Runnable{

	SocketChannel s;
	String id = "test";
	
	public taskSendRequestedPaquet(SocketChannel socket){
		s = socket;
		//s.setOption(SocketOption<>, value)
		
		
	}
	public void run() {
		
		//Etape 1 : définir de quel paquet l'autre a besoin : il faut envoyer OK en premier.
		ByteBuffer b = Utilitaires.stringToBuffer(Message.OK);
		
		try {
			s.write(b);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		//Ensuite il faut savoir de quel fichier on parle
		//là encore il faut la super fonction de simon.
		
		//Etape 3 : on envoit le paquet
		try {
			Donnees.getHostedPaquet(id).envoyerPaquet(s);  //est-ce r�ellement getHostedPaquet que l'on souhaite utiliser ?
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	

}
