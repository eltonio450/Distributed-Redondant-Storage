package Task;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

import Stockage.Donnees;
import Stockage.Machine;
import Stockage.Paquet;
import Utilitaires.Global;
import Utilitaires.Message;
import Utilitaires.Utilitaires;


public class taskServeurExchange implements Runnable {

  SocketChannel socket ;
  
  public taskServeurExchange(SocketChannel s) {
    socket = s ;
  }
  
  //when this task is called, the server has already answer to the client with DEMANDE_ID
  
  public void recoitPaquet() throws IOException{
    
    socket.write(Utilitaires.stringToBuffer(Message.DEMANDE_ID));
    ByteBuffer buffer = ByteBuffer.allocateDirect(Message.BUFFER_LENGTH) ;
    buffer.clear() ;
    socket.read(buffer) ;
    buffer.flip() ;
    String s = Utilitaires.buffToString(buffer) ;
    

    if(Donnees.acceptePaquet(s)){
      buffer = Utilitaires.stringToBuffer(Message.REPONSE_EXCHANGE) ;
      socket.write(buffer) ;
    
      Paquet receivedPaquet = Paquet.recoitPaquet(socket) ;
      Machine otherMachine = Machine.otherMachineFromSocket(socket) ;
      Donnees.receptionPaquet(otherMachine, receivedPaquet);
      
      envoitPaquet() ;
    }

  }
  
  public boolean envoitPaquet() throws IOException {
   
    ByteBuffer buffer = ByteBuffer.allocateDirect(Message.BUFFER_LENGTH) ;
    buffer.clear() ;
    socket.read(buffer) ;
    buffer.flip() ;
    String s = Utilitaires.buffToString(buffer) ;
    
    if(s.equals(Message.END_ENVOI)){
      boolean ok = false ;
      
      //try with toSendASAP
      LinkedList<String> paquets1 = Donnees.chooseManyPaquetToSend1() ;
      
      while(!ok && !paquets1.isEmpty()){
        Paquet aEnvoyer = Donnees.getPaquet(paquets1.pop()) ;
        buffer = Utilitaires.stringToBuffer(aEnvoyer.idGlobal) ;
        socket.write(buffer) ;
        buffer.clear() ;
        socket.read(buffer) ;
        buffer.flip() ;
        s = Utilitaires.buffToString(buffer) ;
        if(s.equals(Message.REPONSE_EXCHANGE)){
          Paquet toSend = Donnees.choosePaquetToSend() ;
          toSend.envoyerPaquet(socket);
          ok = true ;
        }
      }
      
      if(!ok){
        //try with all data
        LinkedList<String> paquets2 = Donnees.chooseManyPaquetToSend2() ;
        
        while(!ok && !paquets2.isEmpty()){
          Paquet aEnvoyer = Donnees.getPaquet(paquets2.pop()) ;
          buffer = Utilitaires.stringToBuffer(aEnvoyer.idGlobal) ;
          socket.write(buffer) ;
          buffer.clear() ;
          socket.read(buffer) ;
          buffer.flip() ;
          s = Utilitaires.buffToString(buffer) ;
          if(s.equals(Message.REPONSE_EXCHANGE)){
            Paquet toSend = Donnees.choosePaquetToSend() ;
            toSend.envoyerPaquet(socket);
            ok = true ;
          }
        }
        
        if(!ok){
          buffer = Utilitaires.stringToBuffer(Message.ANNULE_ENVOI) ;
          socket.write(buffer) ;
          return false ;
        }
        else {
          return true ;
        }
        
      }
      else{
        return true ;
      }
       
    }
    else {
      return false ;
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
