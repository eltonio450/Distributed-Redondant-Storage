package Task;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import Stockage.Donnees;
import Stockage.Machine;
import Stockage.Paquet;
import Utilitaires.Message;
import Utilitaires.Utilitaires;

public class taskAskForPaquets implements Runnable {

  private int numberToGet ;

  
  taskAskForPaquets(int n) {
    numberToGet = n ;
  }
  
  public void run(){
    
    while(numberToGet > 0){
     init() ; 
    }
 }
  
    public void init(){ //return true if succeeded
      try (SocketChannel clientSocket = SocketChannel.open()) { 

        Machine correspondant = Donnees.chooseMachine() ;
        //init connection
        InetSocketAddress local = new InetSocketAddress(0); 
        clientSocket.bind(local); 
        InetSocketAddress remote = new InetSocketAddress(correspondant.ipAdresse, correspondant.port); 
        clientSocket.connect(remote); 
        
        if(recoitPaquet(clientSocket)){
          numberToGet -- ;
        }
      }
      catch(IOException e){
      }
    } 

  
  public boolean recoitPaquet(SocketChannel clientSocket) {
    try {

      ByteBuffer buffer = Utilitaires.stringToBuffer(Message.AskForPaquet) ;
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
        Paquet receivedPaquet = Paquet.recoitPaquetReellement(clientSocket) ;
        Donnees.receptionPaquet(receivedPaquet);
        clientSocket.close();
        return true ;
      }
    }
    catch(IOException e){
      return false ;
    }
  }
  
  
  
}
