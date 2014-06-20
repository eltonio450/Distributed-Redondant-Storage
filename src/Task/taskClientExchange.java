package Task;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

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
  
  public boolean initEtEnvoiePaquet() { //return true if succeeded
    try (SocketChannel clientSocket = SocketChannel.open()) { 
    
    //init connection
    InetSocketAddress local = new InetSocketAddress(0); 
    clientSocket.bind(local); 
    InetSocketAddress remote = new InetSocketAddress(correspondant.ipAdresse, correspondant.port); 
    clientSocket.connect(remote); 
    
    //ask to exchange
    ByteBuffer buffer = Utilitaires.stringToBuffer(Global.EXCHANGE) ;
    buffer.flip() ;
    clientSocket.write(buffer) ;
    buffer.clear() ;
    clientSocket.read(buffer) ;
    buffer.flip() ;
    String s = Utilitaires.buffToString(buffer) ;
   
    if (s.equals(Global.REPONSE_EXCHANGE)){
      //exchange can begin : send its package
      aEnvoyer.envoyerPaquet(clientSocket);
      buffer.clear();
      buffer = Utilitaires.stringToBuffer(Global.END_ENVOI) ;
      buffer.flip() ;
      clientSocket.write(buffer) ;
      
      //now receive the package in exchange
      Paquet receivedPaquet = Paquet.recoitPaquet(clientSocket) ;
      Machine otherMachine = Machine.otherMachineFromSocket(clientSocket) ;
      Donnees.receptionPaquet(otherMachine, receivedPaquet);
      
      //kill the package we sent before :
      //TODO :
      //aEnvoyer.deleteData() ;
      
      return true ;
    }
    else {
      return false ;
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
