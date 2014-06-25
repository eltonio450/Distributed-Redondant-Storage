package Task;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import Stockage.Donnees;
import Stockage.Machine;
import Stockage.Paquet;
import Utilitaires.Global;
import Utilitaires.Message;
import Utilitaires.Utilitaires;


public class taskServeurExchange implements Runnable {

  SocketChannel socket ;
  
  public taskServeurExchange(SocketChannel s) {
    socket = s ;
  }
  
  //when this task is called, the server has already answer to the client with RESPONSE_EXCHANGE
  
  public void exchange() throws IOException{
    
    ByteBuffer buffer = ByteBuffer.allocateDirect(Message.BUFFER_LENGTH) ;
    buffer.clear() ;
    socket.read(buffer) ;
    buffer.flip() ;
    String s = Utilitaires.buffToString(buffer) ;
    
    if(Donnees.acceptePaquet(s)){
      
    }
    
    Paquet receivedPaquet = Paquet.recoitPaquet(socket) ;
    Machine otherMachine = Machine.otherMachineFromSocket(socket) ;
    Donnees.receptionPaquet(otherMachine, receivedPaquet);
    
    Paquet toSend = Donnees.selectPaquetToSend() ;
    toSend.envoyerPaquet(socket);
  }
  
  public void run() {
    try{
      exchange() ;
    }
    catch(IOException e){
      //traiter l'erreur - recommencer l'envoie ?
      //a priori le client s'est rendu compte du problème et va recommencer tout seul
    }
  }


}
