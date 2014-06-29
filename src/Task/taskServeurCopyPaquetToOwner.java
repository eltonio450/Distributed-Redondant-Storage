package Task;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import Stockage.Donnees;
import Stockage.Paquet;
import Utilitaires.Message;
import Utilitaires.Utilitaires;

public class taskServeurCopyPaquetToOwner implements Runnable {
  SocketChannel socket ;
  
  public taskServeurCopyPaquetToOwner(SocketChannel s) {
    socket = s ;
  }
  public void run(){
    try{
    socket.write(Utilitaires.stringToBuffer(Message.DEMANDE_ID));
    ByteBuffer buffer = ByteBuffer.allocateDirect(Message.BUFFER_LENGTH) ;
    buffer.clear() ;
    socket.read(buffer) ;
    buffer.flip() ;
    String s = Utilitaires.buffToString(buffer) ;
    
    Paquet p = Donnees.getHostedPaquet(s);
    p.envoyerPaquetReellement(socket);
    
    socket.close();
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }

}
