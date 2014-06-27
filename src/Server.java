import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

import Stockage.Paquet;
import Utilitaires.Global;
import Utilitaires.Message;
import Utilitaires.Utilitaires;


public class Server {
  
  public static void udpServer(int port, int tailleBuffer) throws
  IOException {
    try (DatagramChannel serverSocket = DatagramChannel.open() ) {
      InetSocketAddress local = new InetSocketAddress(port) ;
      serverSocket.bind(local) ;
      for(;;) {
        ByteBuffer buffer = ByteBuffer.allocate(tailleBuffer
            //serverSocket.getOption(StandardSocketOptions.SO_RCVBUF)
            );
        InetSocketAddress remote = (InetSocketAddress) serverSocket.receive(buffer) ;
        buffer.flip() ;
        System.out.println("UDPServer a recu: " + new String(buffer.array(),StandardCharsets.UTF_16BE)) ;
        buffer.clear();
        buffer.putChar('O');
        buffer.putChar('k');
        buffer.flip();
        serverSocket.send(buffer,remote);
      }
  }
  }

  public static void tcpServer(int port, int tailleBuffer) throws IOException { 
    try (ServerSocketChannel socket = ServerSocketChannel.open()) { 
    InetSocketAddress local = new InetSocketAddress(port); 
    socket.bind(local); 
    try (SocketChannel client = socket.accept()) { 
    //for (int i = 0 ; i<5 ; i++) {
      ByteBuffer buffer = ByteBuffer.allocate(tailleBuffer
          //serverSocket.getOption(StandardSocketOptions.SO_RCVBUF)
          );
      buffer.clear() ;
      client.read(buffer) ;
      buffer.flip() ;
      System.out.println("TCPServer a recu: " + new String(buffer.array(),StandardCharsets.UTF_16BE)) ;
      client.write(Utilitaires.stringToBuffer(Message.DEMANDE_ID));
      System.out.println("Serveur : demande envoyée") ;
      /*buffer = ByteBuffer.allocateDirect(Message.BUFFER_LENGTH) ;
      buffer.clear() ;
      client.read(buffer) ;
      buffer.flip() ;
      String s = Utilitaires.buffToString(buffer) ;
      System.out.println(s) ;*/
    //}
    } 
    }
  }
  
  public static void main(String[] args) {
    int port = 5656 ;
    int tailleBuffer = 100 ;
    
    Paquet aEnvoyer = new Paquet(1,Global.MYSELF) ;
    
    
     Thread thread2 = new Thread( 
        new ThreadTcpServer(port,tailleBuffer),"TcpServer"); 
       thread2.start();
          

     System.out.println("Serveurs lancés") ;
     new Thread(new ThreadClient()).start();
     
  }
   }