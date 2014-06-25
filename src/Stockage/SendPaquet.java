package Stockage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

import Utilitaires.Global;
import Utilitaires.Utilitaires;

public class SendPaquet {

  public static void sendMyOwnData(ArrayList<Paquet> listPaquets) throws IOException {
    
    ArrayList<Machine> hosts = new ArrayList<Machine>(5) ;
    boolean hostsFound = false ;
     for(int i =0 ; i<5; i++) {
        while (!hostsFound){
          Machine correspondant = Stockage.chooseMachine();
          if (!hosts.contains(correspondant)) {
            //il ne faut pas qu'un même serveur possède 2 paquets du même groupe
          try (SocketChannel clientSocket = SocketChannel.open()) {
          
          //init connection
          InetSocketAddress local = new InetSocketAddress(0); 
          clientSocket.bind(local); 
          InetSocketAddress remote = new InetSocketAddress(correspondant.ipAdresse, correspondant.port); 
          clientSocket.connect(remote); 
          
          //ask to exchange
          ByteBuffer buffer = Utilitaires.stringToBuffer(Global.EXCHANGE) ;
          buffer.flip() ;
          clientSocket.write(buffer) ;
          buffer.clear() ;
          clientSocket.read(buffer) ;
          buffer.flip() ;
          String s = Utilitaires.buffToString(buffer) ;
         
          if (s.equals(Global.REPONSE_EXCHANGE)){
            hostsFound = true ;
            hosts.set(i, correspondant);
            Paquet toSend = listPaquets.get(i) ;
            toSend.putOtherHosts(hosts) ;
            toSend.envoyerPaquet(clientSocket);
            buffer.clear();
            buffer = Utilitaires.stringToBuffer(Global.END_ENVOI) ;
            buffer.flip() ;
            clientSocket.write(buffer) ;
            
            //now receive the package in exchange
            Paquet receivedPaquet = Paquet.recoitPaquet(clientSocket) ;
            Machine otherMachine = Machine.otherMachineFromSocket(clientSocket) ;
            Donnees.receptionPaquet(otherMachine, receivedPaquet);
            
            //kill the package we sent before :
            //TODO :
            //toSend.deleteData() ;
          }
          
          }
         }
        hostsFound = false ;
    }
    }
    
  }
  
  
  public static void putAndGet(Machine m, Paquet p){ 
    //TODO
    //cette méthode actualise le champ myData de Donnees
    //Envoie UN paquet à m et reçoit UN paquet en échange
  }
  
  
  public static void prevenirHostChanged(String Id){
    //préviens une machine que cette machine remplace m pour le paquet d'id Id
    Paquet p = Donnees.getHostedPaquet(Id) ;
    int placeToModify = p.power ;
    for (int i =0 ; i< 5 ; i++) {
      if (i!=placeToModify){
        Machine m = p.otherHosts.get(i) ;
        if(m != null) {
          try (SocketChannel clientSocket = SocketChannel.open()) { 
            
            //init connection
            InetSocketAddress local = new InetSocketAddress(0); 
            clientSocket.bind(local); 
            InetSocketAddress remote = new InetSocketAddress(m.ipAdresse, m.port); 
            clientSocket.connect(remote); 
            
            //message
            ByteBuffer buffer = Utilitaires.stringToBuffer(Global.HOST_CHANGED + " " + Id +" " + placeToModify) ;
            buffer.flip() ;
            clientSocket.write(buffer) ;
          }
          catch(IOException e){
            //TODO : on a pas pu prévenir m !
          }
        }
      }
    }
  }
  
}


