package Task;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

import RelationsPubliques.Global;
import Stockage.Machine;
import Stockage.Paquet;
import Utilitaires.Utilitaires;

public class taskClientExchange implements Runnable {

  Machine correspondant ;
  Paquet aEnvoyer ;
  
  taskClientExchange(Machine m, Paquet p){
    correspondant = m ;
    aEnvoyer = p ;
  }
  
  public void initConnection() throws IOException { 
    try (SocketChannel clientSocket = SocketChannel.open()) { 
    InetSocketAddress local = new InetSocketAddress(0); 
    clientSocket.bind(local); 
    InetSocketAddress remote = new InetSocketAddress(correspondant.ipAdresse, correspondant.port); 
    clientSocket.connect(remote); 
    
    ByteBuffer buffer = Utilitaires.stringToBuffer(Global.EXCHANGE) ;
    buffer.flip() ;
    clientSocket.write(buffer) ;
    clientSocket.read(buffer) ;
    String s = Utilitaires.buffToString(buffer) ;
    
    if (s.equals(Global.REPONSE_EXCHANGE)){
      
    }
    else {
      //TODO : renvoyer une erreur - la machine ne veut pas recevoir de 
    }
    }
    } 
    
  public void run() {
    
  }

  
}
