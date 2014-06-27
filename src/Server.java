import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

import Stockage.Donnees;
import Stockage.Machine;
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
      ByteBuffer buffer = ByteBuffer.allocate(tailleBuffer);
      buffer.clear() ;
      client.read(buffer) ;
      buffer.flip() ;
      System.out.println("TCPServer a recu: " + new String(buffer.array(),StandardCharsets.UTF_16BE)) ;
      buffer = Utilitaires.stringToBuffer(Message.DEMANDE_ID) ;
      client.write(buffer) ;
      System.out.println("Serveur : demande envoyée") ;
      buffer = ByteBuffer.allocate(Message.BUFFER_LENGTH) ;
      buffer.clear() ;
      int i = client.read(buffer) ;
      buffer.flip() ;
      if( true ) {System.out.println("TCPServer a recu: " + new String(buffer.array(),StandardCharsets.UTF_16BE)) ; }
      if(true){
        buffer = Utilitaires.stringToBuffer(Message.REPONSE_EXCHANGE) ;
        client.write(buffer) ;
        System.out.println("Serveur pret à recevoir Paquet") ;
        Paquet receivedPaquet = Paquet.recoitPaquet(client) ;
        System.out.println("Serveur a reçu Paquet") ;
        Machine otherMachine = Machine.otherMachineFromSocket(client) ;
        Donnees.receptionPaquet(otherMachine, receivedPaquet);
        
       
      }
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