package Task;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import Utilitaires.Global;


public class taskServeurExchange implements Runnable {

  SocketChannel socket ;
  
  taskServeurExchange(SocketChannel s) {
    socket = s ;
  }
  
  public void exchange() throws IOException{
    ByteBuffer buffer = ByteBuffer.allocateDirect(Global.BUFFER_LENGTH);
    buffer.clear() ;
    // TODO : créer nouveau paquet à partir de la lecture du buffer
    socket.read(buffer) ;
    
  }
  
  public void run() {
    // TODO Auto-generated method stub
    
  }


}
