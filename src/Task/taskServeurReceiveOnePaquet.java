package Task;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import Stockage.Donnees;
import Stockage.Paquet;
import Utilitaires.Message;
import Utilitaires.Utilitaires;

public class taskServeurReceiveOnePaquet implements Runnable {

  SocketChannel socket ;
  
  public taskServeurReceiveOnePaquet(SocketChannel s) {
    socket = s ;
  }
  

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
    
      Paquet receivedPaquet = Paquet.recoitPaquetReellement(socket) ;
      Donnees.receptionPaquet(receivedPaquet);
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

