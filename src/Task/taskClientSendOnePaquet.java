package Task;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

import Stockage.Donnees;
import Stockage.Machine;
import Stockage.Paquet;
import Utilitaires.Global;
import Utilitaires.Message;
import Utilitaires.Utilitaires;

public class taskClientSendOnePaquet implements Runnable {

  Paquet aEnvoyer ;

  public taskClientSendOnePaquet(Paquet p){
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
      ByteBuffer buffer = Utilitaires.stringToBuffer(Message.SendOne) ;
      clientSocket.write(buffer) ;
      buffer.clear() ;
      clientSocket.read(buffer) ;
      buffer.flip() ;
      String s = Utilitaires.buffToString(buffer) ;
      
      //System.out.println(s) ;
      
      if(!s.equals(Message.DEMANDE_ID)){
        clientSocket.close();
        return false ;
      }
      else {
        buffer = Utilitaires.stringToBuffer(aEnvoyer.idGlobal) ;
        clientSocket.write(buffer) ;
        buffer.clear() ;
        clientSocket.read(buffer) ;
        buffer.flip() ;
        s = Utilitaires.buffToString(buffer) ;
  
        if (s.equals(Message.REPONSE_EXCHANGE)){
          //exchange can begin : send its package
          aEnvoyer.envoyerPaquet(clientSocket);
          clientSocket.close();
          return true ;
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

  

  public void run() {
    //if(aEnvoyer.askForlock()){
      boolean success = false ;
      while(!success){
        success = initEtEnvoiePaquet() ;
        try {
          Thread.sleep(Message.TIME_TO_SLEEP_1) ;  //TODO : ?
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    //}
  }


}

