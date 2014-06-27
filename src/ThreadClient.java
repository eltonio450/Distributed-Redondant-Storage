import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import Stockage.Machine;
import Stockage.Paquet;
import Utilitaires.Global;
import Utilitaires.Message;
import Utilitaires.Utilitaires;


public class ThreadClient implements Runnable { 

  public void run() { 
    try { 
      try (SocketChannel clientSocket = SocketChannel.open()) {
        System.out.println("coucou") ;
        Paquet aEnvoyer = new Paquet(1,Global.MYSELF) ;
        System.out.println("coucou2") ;
        Machine correspondant = new Machine("localhost",5656) ;
        //init connection
        InetSocketAddress local = new InetSocketAddress(0); 
        clientSocket.bind(local); 
        InetSocketAddress remote = new InetSocketAddress(correspondant.ipAdresse, correspondant.port); 
        clientSocket.connect(remote); 

        //ask to exchange
        ByteBuffer buffer = Utilitaires.stringToBuffer(Message.EXCHANGE) ;
        clientSocket.write(buffer) ;
        buffer.clear() ;
        System.out.println("Client attend réponse") ;
        clientSocket.read(buffer) ;
        System.out.println("Client a reçu réponse") ;
        buffer.flip() ;
        String s = Utilitaires.buffToString(buffer) ;
        System.out.println(s) ;
    
        if(!s.equals(Message.DEMANDE_ID)){
          System.out.println(false) ;
        }

        buffer = Utilitaires.stringToBuffer(aEnvoyer.idGlobal) ;
        buffer.flip() ;
        clientSocket.write(buffer) ;
        buffer.clear() ;
        clientSocket.read(buffer) ;
        buffer.flip() ;
        s = Utilitaires.buffToString(buffer) ;
        System.out.println(s) ;
        

        if (s.equals(Message.REPONSE_EXCHANGE)){
          //exchange can begin : send its package
          aEnvoyer.envoyerPaquet(clientSocket);

          /*if(recoitPaquet(clientSocket)){
            aEnvoyer.removePaquet();
            System.out.println(true) ;
          }
          else {} */
        }

        else {
         
        }
      }
      catch(Exception e){

      }
    }
    catch (Exception e) {throw new RuntimeException(e); }
  } 


}
