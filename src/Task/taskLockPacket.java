package Task;

import java.net.Socket;
import java.nio.channels.SocketChannel;

public class taskLockPacket implements Runnable
{
	SocketChannel s;

	/**
	 * @author Antoine
	 *
	 * @param s : Socket sur lequel la communication se fait
	 * @param asker : true si c'est celui qui a demandé le lock, false sinon.
	 *	Ce reunnable est executé lorsqu'on reçoit l'instruction de locker des paquets sur le socket.
	 */
	
	/** TODO :
	 * 
	 * FERMER LA SOCKET après utilisation
	 *
	 */

	public taskLockPacket(SocketChannel socket){
		s = socket;
	}

	public void run() {
		//Etape 1 : renvoyer le message d'ACK
		
		String id;
		//s.write().
		//Etape 2 : attendre de recevoir l'identifiant du paquet qui demande le lock
		
		//Etape 3 : effectuer le lock si c'est possible.
		
		//si il n'est pas en train de le demander
		/*
		 * 
		 * if(paquet.isAsking.tryLock())
		 *		
		 *	else
		 *		if(priorityDemandeur>priorityReceveur)
		 *			paquet.lock();
		 *
		 	
		 *
		 */
			//then 
		//Etpae 4 : répondre que le lock a bien été effectué

	}

}
