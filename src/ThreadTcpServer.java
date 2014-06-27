
public class ThreadTcpServer implements Runnable { 
  int port = 5555;
  int tailleBuffer = 100 ;
  
  ThreadTcpServer(int p, int taille){
    port = p ;
    tailleBuffer = taille ;
  }
  
  public void run() { 
    try { 
      while(true){
        Server.tcpServer(port, tailleBuffer) ;
        }
      }
    catch (Exception e) {throw new RuntimeException(e); }
  } 
}