package Task;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import Utilitaires.Message;
import Utilitaires.Utilitaires;

public class taskReplyStillAlive implements Runnable {
	SocketChannel s;
	
	public taskReplyStillAlive (SocketChannel s) {
		this.s = s;
	}
	
	public void run () {
		try {
			
			s.write(Utilitaires.stringToBuffer(Message.NOT_DEAD));
		} catch (IOException e) {
			Utilitaires.out("Problème dans la réponse de 'Je suis vivant'",0,true);
		}
	}
}
