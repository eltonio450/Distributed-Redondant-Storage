package Task;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import Utilitaires.Global;
import Utilitaires.Message;
import Stockage.Donnees;
import Stockage.Machine;
import Stockage.Paquet;
import Stockage.Stockage;
import Utilitaires.Utilitaires;


/** TODO :
 * 
 * FERMER LA SOCKET après utilisation
 *
 */

public class taskClientExchange implements Runnable {

	Paquet aEnvoyer ;

	public taskClientExchange(Paquet p){
		aEnvoyer = p ;
	}


	public boolean initEtEnvoiePaquet() { //return true if succeeded
		try (SocketChannel clientSocket = SocketChannel.open()) { 

			Machine correspondant = Donnees.chooseMachine() ;
			//init connection
			InetSocketAddress local = new InetSocketAddress(0); 
			clientSocket.bind(local); 
			InetSocketAddress remote = new InetSocketAddress(correspondant.ipAdresse, correspondant.port); 
			clientSocket.connect(remote); 

			//ask to exchange
			ByteBuffer buffer = Utilitaires.stringToBuffer(Message.EXCHANGE) ;
			clientSocket.write(buffer) ;
			buffer.clear() ;
			clientSocket.read(buffer) ;
			buffer.flip() ;
			String s = Utilitaires.buffToString(buffer) ;

			if(!s.equals(Message.DEMANDE_ID)){
				return false ;
			}

			buffer = Utilitaires.stringToBuffer(aEnvoyer.idGlobal) ;
			buffer.flip() ;
			clientSocket.write(buffer) ;
			buffer.clear() ;
			clientSocket.read(buffer) ;
			buffer.flip() ;
			s = Utilitaires.buffToString(buffer) ;

			if (s.equals(Message.REPONSE_EXCHANGE)){
				//exchange can begin : send its package
				aEnvoyer.envoyerPaquet(clientSocket);

				if(recoitPaquet(clientSocket)){
					aEnvoyer.removePaquet();
					return true ;
				}
				else { return false ; }
			}

			else {
				return false ;
			}

		}
		catch(IOException e){
			return false ;
		}
	} 

	public boolean recoitPaquet(SocketChannel clientSocket) {
		try {

			//say I have finished, what Paquet do you want to send to me ?
			ByteBuffer buffer = Utilitaires.stringToBuffer(Message.END_ENVOI) ;
			buffer.flip() ;
			clientSocket.write(buffer) ;
			buffer.clear() ;
			clientSocket.read(buffer) ;
			buffer.flip() ;
			String s = Utilitaires.buffToString(buffer) ;

			while(!Donnees.acceptePaquet(s) || s.equals(Message.ANNULE_ENVOI)){
				buffer.clear() ;
				buffer = Utilitaires.stringToBuffer(Message.DO_NOT_ACCEPT) ;
				buffer.flip() ;
				clientSocket.write(buffer) ;
			}
			if(s.equals(Message.ANNULE_ENVOI)) {
				return false ;
			}
			else {
				buffer.clear() ;
				buffer = Utilitaires.stringToBuffer(Message.REPONSE_EXCHANGE) ;
				buffer.flip() ;
				clientSocket.write(buffer) ;

				//now receive the package in exchange
				Paquet receivedPaquet = Paquet.recoitPaquet(clientSocket) ;
				Machine otherMachine = Machine.otherMachineFromSocket(clientSocket) ;
				Donnees.receptionPaquet(otherMachine, receivedPaquet);
				return true ;
			}
		}
		catch(IOException e){
			return false ;
		}
	}

	public void run() {
		boolean success = false ;
		while(!success){
			success = initEtEnvoiePaquet() ;
		}
	}


}
