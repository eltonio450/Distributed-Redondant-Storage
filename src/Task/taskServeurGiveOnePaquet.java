package Task;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

import Stockage.Donnees;
import Stockage.Paquet;
import Utilitaires.Message;
import Utilitaires.Utilitaires;

public class taskServeurGiveOnePaquet implements Runnable {
  
SocketChannel socket ;
  
  public taskServeurGiveOnePaquet(SocketChannel s) {
    socket = s ;
  }
  
  public void run(){
    try{
      init() ;
    }
    catch(Exception e){
      //TODO ??
    }
  }
  

  public void init() throws IOException{
    
    if(Donnees.paquetsEnTrop.get() > 0){
      Paquet sentPaquet = envoitPaquet() ;
      if(sentPaquet != null){
        sentPaquet.removePaquet();
        Donnees.paquetsEnTrop.decrementAndGet() ;
      }
    }
    else{
      socket.write(Utilitaires.stringToBuffer(Message.ANNULE_ENVOI)); 
    }
  }
  
  public Paquet envoitPaquet() throws IOException {

    Paquet aEnvoyer = null ;
    ByteBuffer buffer = ByteBuffer.allocateDirect(Message.BUFFER_LENGTH) ;
    String s = "" ;
    
      boolean ok = false ;
      
      //try with toSendASAP
      LinkedList<String> paquets1 = Donnees.chooseManyPaquetToSend1() ;
      
      while(!ok && !paquets1.isEmpty()){
        aEnvoyer = Donnees.removeTemporarlyPaquet(paquets1.pop()) ;
        if(aEnvoyer != null) {
          buffer = Utilitaires.stringToBuffer(aEnvoyer.idGlobal) ;
          socket.write(buffer) ;
          buffer.clear() ;
          socket.read(buffer) ;
          buffer.flip() ;
          s = Utilitaires.buffToString(buffer) ;
          if(s.equals(Message.REPONSE_EXCHANGE)){
            aEnvoyer.envoyerPaquet(socket);
            ok = true ;
          }
          else{ Donnees.putNewPaquet(aEnvoyer) ; }
        }
      }
      
      if(!ok){
        //try with all data
        LinkedList<String> paquets2 = Donnees.chooseManyPaquetToSend2() ;
        
        while(!ok && !paquets2.isEmpty()){
          aEnvoyer = Donnees.removeTemporarlyPaquet(paquets2.pop()) ;
          if(aEnvoyer != null) {
            buffer = Utilitaires.stringToBuffer(aEnvoyer.idGlobal) ;
            socket.write(buffer) ;
            buffer.clear() ;
            socket.read(buffer) ;
            buffer.flip() ;
            s = Utilitaires.buffToString(buffer) ;
            if(s.equals(Message.REPONSE_EXCHANGE)){
              aEnvoyer.envoyerPaquet(socket);
              ok = true ;
            }
            else{ Donnees.putNewPaquet(aEnvoyer) ; }
          }
        }
        
        if(!ok){
          buffer = Utilitaires.stringToBuffer(Message.ANNULE_ENVOI) ;
          socket.write(buffer) ;
          return aEnvoyer ;
        }
        else {
          return aEnvoyer;
        }
        
      }
      else{
        return aEnvoyer ;
      }
       
    }


}
