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
      LinkedList<Paquet> paquets = Donnees.chooseManyPaquetToSend() ;
      
      while(!ok && !paquets.isEmpty()){
        Paquet aEnvoyer = paquets.pop() ;
        buffer = Utilitaires.stringToBuffer(aEnvoyer.id) ;
        socket.write(buffer) ;
        buffer.clear() ;
        socket.read(buffer) ;
        buffer.flip() ;
        s = Utilitaires.buffToString(buffer) ;
        if(s.equals(Message.REPONSE_EXCHANGE)){
          Paquet toSend = Donnees.selectPaquetToSend() ;
          toSend.envoyerPaquet(socket);
          ok = true ;
        }
      }
      
      if(!ok && paquets.isEmpty()){
        buffer = Utilitaires.stringToBuffer(Message.ANNULE_ENVOI) ;
        socket.write(buffer) ;
        return false ;
      }
      else {
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
      //a priori le client s'est rendu compte du problème et va recommencer tout seul
    }
  }


}
