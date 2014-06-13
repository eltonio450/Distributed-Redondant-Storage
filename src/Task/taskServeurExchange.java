package Task;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import Utilitaires.Global;


public class taskServeurExchange implements Runnable {

  SocketChannel socket ;
  
  taskServeurExchange(SocketChannel s) {
    socket = s ;
  }
  
  public void exchange(){
    ByteBuffer buffer = ByteBuffer.allocateDirect(Global.BUFFER_LENGTH);
  }
  
  public void run() {
    // TODO Auto-generated method stub
    
  }


}
