package Task;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

import Stockage.Donnees;
import Stockage.Machine;
import Stockage.Paquet;
import Utilitaires.Global;
import Utilitaires.Message;
import Utilitaires.Utilitaires;

public class taskDumpToMachine implements Runnable {
	
  private LinkedList<Machine> allServers ;
  
  public taskDumpToMachine(LinkedList<Machine> serveurs){
    allServers = serveurs ;
  }
	
	public void run() {
	  boolean continuer = true;
	  while(continuer){
	    
	    for(Machine m : allServers){
	      if(m != Global.MYSELF){
	      SocketChannel socket = init(m) ;
	      if(socket != null){
	        
	        boolean iLikeThisMachine = true ;
	        String toSend = Donnees.toSendASAP.peekFirst() ;
	        
	        while(!toSend.equals(null) && iLikeThisMachine){ 
	          Paquet aEnvoyer = Donnees.getHostedPaquet(toSend) ;
	          if(envoiePaquet(aEnvoyer,m,socket)){
	            toSend = Donnees.toSendASAP.peekFirst() ;
	          }
	          else {
	            Donnees.toSendASAP.addLast(toSend) ;
	            iLikeThisMachine = false ;
	          }
	        }
	        
	        if(iLikeThisMachine){ //means toSendASAP is empty
	          continuer = false ;
	        }
	        
	      }
	        //we have finished with Machine m
  	      try {
            socket.close();
          } catch (IOException e) {
          }
	     }
	    }
	    
	    try {
        Thread.sleep(Global.TIME_TO_SLEEP_Dumping);
      } catch (InterruptedException e) {
      }
	  }

	}
	
	public SocketChannel init(Machine correspondant) { 
	  //return true if succeeded
	  
    try (SocketChannel clientSocket = SocketChannel.open()) { 
      
      //init connection
      InetSocketAddress local = new InetSocketAddress(0); 
      clientSocket.bind(local); 
      InetSocketAddress remote = new InetSocketAddress(correspondant.ipAdresse, correspondant.port); 
      clientSocket.connect(remote); 
      return(clientSocket);
    }
    catch(IOException e){
      return null;
    }
	}
      
   public boolean envoiePaquet(Paquet aEnvoyer, Machine correspondant,SocketChannel clientSocket) {
     try { 
     //ask to exchange
       ByteBuffer buffer = Utilitaires.stringToBuffer(Message.EXCHANGE) ;
       clientSocket.write(buffer) ;
       buffer.clear() ;
       clientSocket.read(buffer) ;
       buffer.flip() ;
       String s = Utilitaires.buffToString(buffer) ;

       if(!s.equals(Message.DEMANDE_ID)){
         return false ;
       }
       
       else{
         buffer = Utilitaires.stringToBuffer(aEnvoyer.idGlobal) ;
         clientSocket.write(buffer) ;
         buffer.clear() ;
         clientSocket.read(buffer) ;
         buffer.flip() ;
         s = Utilitaires.buffToString(buffer) ;
   
         if (s.equals(Message.REPONSE_EXCHANGE)){
           //exchange can begin : send its package
           aEnvoyer.envoyerPaquet(clientSocket);
   
           if(recoitPaquet(clientSocket)){
             aEnvoyer.removePaquet();
             return true ;
           }
           else { return false ; }
         }
   
         else {
           return false ;
         }
       }

     }
     catch(IOException e){
       return false ;
     }
   } 

   public boolean recoitPaquet(SocketChannel clientSocket) {
     try {
       //say I have finished, what Paquet do you want to send to me ?
       ByteBuffer buffer = Utilitaires.stringToBuffer(Message.END_ENVOI) ;
       clientSocket.write(buffer) ;
       buffer.clear() ;
       clientSocket.read(buffer) ;
       buffer.flip() ;
       String s = Utilitaires.buffToString(buffer) ;

       while(!Donnees.acceptePaquet(s) && !s.equals(Message.ANNULE_ENVOI)){
         buffer = Utilitaires.stringToBuffer(Message.DO_NOT_ACCEPT) ;
         clientSocket.write(buffer) ;
         buffer.clear() ;
         clientSocket.read(buffer) ;
         buffer.flip() ;
         s = Utilitaires.buffToString(buffer) ;
       }
       if(s.equals(Message.ANNULE_ENVOI)) {
         return false ;
       }
       else {
         buffer = Utilitaires.stringToBuffer(Message.REPONSE_EXCHANGE) ;
         clientSocket.write(buffer) ;

         //now receive the package in exchange
         Paquet receivedPaquet = Paquet.recoitPaquet(clientSocket) ;
         Machine otherMachine = Machine.otherMachineFromSocket(clientSocket) ;
         Donnees.receptionPaquet(otherMachine, receivedPaquet);
         return true ;
       }
     }
     catch(IOException e){
       return false ;
     }
   }
}