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

public class taskWarnHostChanged implements Runnable {
  
  String ID ;
  
  public taskWarnHostChanged (String id){
    ID = id ;
  }
  
  public void run(){
    System.out.println("-------------task warn host changed--------------------"); 
    prevenirHostChanged(ID) ;
  }
  
  public static void prevenirHostChanged(String id){
    //pr�viens une machine que cette machine remplace m pour le paquet d'id Id
    Paquet p = Donnees.getHostedPaquet(id) ;
    int placeToModify = p.power ;
    for (int i =0 ; i< 5 ; i++) {
      if (i!=placeToModify){
        Machine m = p.otherHosts.get(i) ;
        if(m != null) {
          try (SocketChannel clientSocket = SocketChannel.open()) { 
            
            //init connection
            InetSocketAddress local = new InetSocketAddress(0); 
            clientSocket.bind(local); 
            InetSocketAddress remote = new InetSocketAddress(m.ipAdresse, m.port); 
            clientSocket.connect(remote); 
            
            //message
            ByteBuffer buffer = Utilitaires.stringToBuffer(Message.HOST_CHANGED) ;
            clientSocket.write(buffer) ;
            buffer.clear() ;
            clientSocket.read(buffer) ;
            buffer.flip() ;
            String response = Utilitaires.buffToString(buffer) ;
            if(response.equals(Message.OK)){
              String s = id +" " + placeToModify + " " + Message.END_ENVOI ;
              buffer = Utilitaires.stringToBuffer(s) ;
              clientSocket.write(buffer) ; 
              System.out.println(s) ;
            }
          }
          catch(IOException e){
            //TODO : on a pas pu pr�venir m !
          }
        }
      }
    }
  }

}
