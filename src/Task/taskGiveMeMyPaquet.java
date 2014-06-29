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

public class taskGiveMeMyPaquet implements Runnable {
  
  String id ;
  Machine correspondant ;
  
  public taskGiveMeMyPaquet(String ID,Machine host){
    id = ID ;
    correspondant = host ;
  }
  
  public void run(){
    initEtRecoit() ;
  }
  
  public void initEtRecoit() {
    try { 
      SocketChannel clientSocket = SocketChannel.open() ;
      //init connection
      InetSocketAddress local = new InetSocketAddress(0); 
      clientSocket.bind(local); 
      InetSocketAddress remote = new InetSocketAddress(correspondant.ipAdresse, correspondant.port); 
      clientSocket.connect(remote); 
      
      //ask to exchange
      ByteBuffer buffer = Utilitaires.stringToBuffer(Message.GiveMeMyPaquet) ;
      clientSocket.write(buffer) ;
      buffer.clear() ;
      clientSocket.read(buffer) ;
      buffer.flip() ;
      String s = Utilitaires.buffToString(buffer) ;

      if(!s.equals(Message.DEMANDE_ID)){
        clientSocket.close();
      }
      
      else{
        buffer = Utilitaires.stringToBuffer(id) ;
        clientSocket.write(buffer) ;
        clientSocket.write(buffer) ;

        //now receive the package
        Paquet receivedPaquet = Paquet.recoitPaquetReellement(clientSocket) ;
        Donnees.putNewPaquet(receivedPaquet);
        clientSocket.close();
      }

    }
    catch(IOException e){
      e.printStackTrace();
    }
  } 

}
