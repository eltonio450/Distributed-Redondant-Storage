package Task;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

import Stockage.Donnees;
import Stockage.Machine;
import Stockage.Paquet;
import Utilitaires.Message;
import Utilitaires.Utilitaires;

public class taskServeurReceiveOnePaquet implements Runnable {

  SocketChannel socket ;
  
  public taskServeurReceiveOnePaquet(SocketChannel s) {
    socket = s ;
  }
  
  //when this task is called, the server has already answer to the client with DEMANDE_ID
  
  public void recoitPaquet() throws IOException{
    
    socket.write(Utilitaires.stringToBuffer(Message.DEMANDE_ID));
    ByteBuffer buffer = ByteBuffer.allocateDirect(Message.BUFFER_LENGTH) ;
    buffer.clear() ;
    socket.read(buffer) ;
    buffer.flip() ;
    String s = Utilitaires.buffToString(buffer) ;
    
    //Utilitaires.out("Serveur : " + s) ;
    

    if(Donnees.acceptePaquet(s)){
      buffer = Utilitaires.stringToBuffer(Message.REPONSE_EXCHANGE) ;
      socket.write(buffer) ;
    
      Paquet receivedPaquet = Paquet.recoitPaquet(socket) ;
      Machine otherMachine = Machine.otherMachineFromSocket(socket) ;
      Donnees.receptionPaquet(otherMachine, receivedPaquet);
      //TODO : put the paquet in toSendASAP ??
    }

  }
  
 
  
  public void run() {
    try{
      recoitPaquet() ;
    }
    catch(IOException e){
      //traiter l'erreur - recommencer l'envoie ?
      //a priori le client s'est rendu compte du probl�me et va recommencer tout seul
    }
  }


}

