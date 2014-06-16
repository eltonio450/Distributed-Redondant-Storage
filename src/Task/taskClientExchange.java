package Task;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

import Utilitaires.Global;
import Stockage.Donnees;
import Stockage.Machine;
import Stockage.Paquet;
import Utilitaires.Utilitaires;

public class taskClientExchange implements Runnable {

  Machine correspondant ;
  Paquet aEnvoyer ;
  
  public taskClientExchange(Machine m, Paquet p){
    correspondant = m ;
    aEnvoyer = p ;
  }
  
  public void initEtEnvoiePaquet() throws IOException { 
    try (SocketChannel clientSocket = SocketChannel.open()) { 
    InetSocketAddress local = new InetSocketAddress(0); 
    clientSocket.bind(local); 
    InetSocketAddress remote = new InetSocketAddress(correspondant.ipAdresse, correspondant.port); 
    clientSocket.connect(remote); 
    
    ByteBuffer buffer = Utilitaires.stringToBuffer(Global.EXCHANGE) ;
    buffer.flip() ;
    clientSocket.write(buffer) ;
    buffer.clear() ;
    clientSocket.read(buffer) ;
    buffer.flip() ;
    String s = Utilitaires.buffToString(buffer) ;
    
    if (s.equals(Global.REPONSE_EXCHANGE)){
      aEnvoyer.envoyerPaquet(clientSocket);
      buffer.clear();
      buffer = Utilitaires.stringToBuffer(Global.END_COMMUNICATION) ;
      buffer.flip() ;
      clientSocket.write(buffer) ;
      Paquet receivedPaquet = Paquet.recoitPaquet(clientSocket) ;
      Machine otherMachine = Machine.otherMachineFromSocket(clientSocket) ;
      Donnees.receptionPaquet(otherMachine, receivedPaquet);
    }
    else {
      //TODO : renvoyer une erreur - la machine ne veut pas recevoir de
    }
    
   }
   } 
    
  public void run() {
    try{
      initEtEnvoiePaquet() ;
      
    }
    catch(IOException e){
      // TODO : traiter l'erreur - recommencer l'envoie ?
    }
  }

  
}
