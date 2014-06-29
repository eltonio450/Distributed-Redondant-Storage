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
    //Utilitaires.out("-------------task warn host changed--------------------"); 
    prevenirHostChanged(ID) ;
  }
  
  public static void prevenirHostChanged(String id){
    //pr�viens une machine que cette machine remplace m pour le paquet d'id Id
    Paquet p = Donnees.getHostedPaquet(id) ;
    int placeToModify = p.power ;
    for (int i =0 ; i< 5 ; i++) {
      Machine m ;
      if (i!=placeToModify){
        m = p.otherHosts.get(i) ;
      }
      else {
        m = p.owner ;
      }
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
            //Utilitaires.out("Envoy� : " + s + " a : " + m.toString()) ;
          }
          
        }
        catch(IOException e){
          //TODO : on a pas pu pr�venir m !
          //Utilitaires.out("echec") ;
        }
      }
    }
  }

}
