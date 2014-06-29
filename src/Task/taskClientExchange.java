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
			
			else{
  			buffer = Utilitaires.stringToBuffer(aEnvoyer.idGlobal) ;
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
  			  clientSocket.close();
  				return false ;
  			}
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
			clientSocket.write(buffer) ;
			buffer.clear() ;
			clientSocket.read(buffer) ;
			buffer.flip() ;
			String s = Utilitaires.buffToString(buffer) ;

			while(!Donnees.acceptePaquet(s) && !s.equals(Message.ANNULE_ENVOI)){
				buffer = Utilitaires.stringToBuffer(Message.DO_NOT_ACCEPT) ;
				clientSocket.write(buffer) ;
	      buffer.clear() ;
	      clientSocket.read(buffer) ;
	      buffer.flip() ;
	      s = Utilitaires.buffToString(buffer) ;
			}
			if(s.equals(Message.ANNULE_ENVOI)) {
			  clientSocket.close();
				return false ;
			}
			else {
				buffer = Utilitaires.stringToBuffer(Message.REPONSE_EXCHANGE) ;
				clientSocket.write(buffer) ;

				//now receive the package in exchange
				Paquet receivedPaquet = Paquet.recoitPaquet(clientSocket) ;
				Donnees.receptionPaquet(receivedPaquet);
				clientSocket.close();
				return true ;
			}
		}
		catch(IOException e){
			return false ;
		}
	}

	public void run() {
	  //if(aEnvoyer.askForlock()){
  		boolean success = false ;
  		while(!success){
  			success = initEtEnvoiePaquet() ;
  		}
  		try {
        Thread.sleep(Message.TIME_TO_SLEEP_1) ;  //TODO : ?
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
	  //}
	}


}
