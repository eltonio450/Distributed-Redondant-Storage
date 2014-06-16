package Task;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import Stockage.Donnees;
import Stockage.Paquet;
import Utilitaires.Global;
import Utilitaires.Utilitaires;


public class taskServeurExchange implements Runnable {

  SocketChannel socket ;
  
  public taskServeurExchange(SocketChannel s) {
    socket = s ;
  }
  
  //when this task is called, the server has already answer to the client with RESPONSE_EXCHANGE
  
  public void exchange() throws IOException{
    ByteBuffer buffer = ByteBuffer.allocateDirect(Global.BUFFER_LENGTH);
    buffer.clear() ;
    // TODO : créer nouveau paquet à partir de la lecture du buffer
    socket.read(buffer) ;
    buffer.flip() ;
    while(Utilitaires.buffToString(buffer) != Global.END_ENVOI){
      buffer.clear() ;
      socket.read(buffer) ; // TODO : do something smart to analyze the message in order to create a "Paquet"
    }
    buffer.clear() ;
    Paquet toSend = Donnees.selectPaquetToSend() ;
    toSend.envoyerPaquet(socket);
    //TODO : add a final statement like Global.END_ENVOI to show exchange has finished,
    // and modify taskClientExchange so that it takes it into account
  }
  
  public void run() {
    try{
      exchange() ;
    }
    catch(IOException e){
      // TODO : traiter l'erreur - recommencer l'envoie ?
    }
  }


}
