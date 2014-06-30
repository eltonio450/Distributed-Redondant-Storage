package Stockage;


public class SendPaquet {
/*
  public static void prevenirHostChanged(String id){
    //pr�viens une machine que cette machine remplace m pour le paquet d'id Id
    Paquet p = Donnees.getHostedPaquet(id) ;
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
            ByteBuffer buffer = Utilitaires.stringToBuffer(Message.HOST_CHANGED) ;
            clientSocket.write(buffer) ;
            buffer.clear() ;
            clientSocket.read(buffer) ;
            buffer.flip() ;
            String response = Utilitaires.buffToString(buffer) ;
            if(response.equals(Message.OK)){
              buffer = Utilitaires.stringToBuffer(id +" " + placeToModify) ;
              clientSocket.write(buffer) ; 
            }
          }
          catch(IOException e){
            //TODO : on a pas pu pr�venir m !
          }
        }
      }
    }
  }
  
  //useless now
  /*
    public static void sendMyOwnData(Paquet p) throws IOException {
    
    //TODO : reimplement this !!
    ArrayList<Machine> hosts = new ArrayList<Machine>(5) ;
    boolean hostsFound = false ;
     for(int i =0 ; i<5; i++) {
        while (!hostsFound){
          Machine correspondant = Stockage.chooseMachine();
          if (!hosts.contains(correspondant)) {
            //il ne faut pas qu'un m�me serveur poss�de 2 paquets du m�me groupe
          try (SocketChannel clientSocket = SocketChannel.open()) {
          
          //init connection
          InetSocketAddress local = new InetSocketAddress(0); 
          clientSocket.bind(local); 
          InetSocketAddress remote = new InetSocketAddress(correspondant.ipAdresse, correspondant.port); 
          clientSocket.connect(remote); 
          
          //ask to exchange
          ByteBuffer buffer = Utilitaires.stringToBuffer(Message.EXCHANGE) ;
          buffer.flip() ;
          clientSocket.write(buffer) ;
          buffer.clear() ;
          clientSocket.read(buffer) ;
          buffer.flip() ;
          String s = Utilitaires.buffToString(buffer) ;
         
          if (s.equals(Message.REPONSE_EXCHANGE)){
            hostsFound = true ;
            hosts.set(i, correspondant);
            Paquet toSend = p.get(i) ;
            toSend.putOtherHosts(hosts) ;
            toSend.envoyerPaquet(clientSocket);
            buffer.clear();
            buffer = Utilitaires.stringToBuffer(Message.END_ENVOI) ;
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
    
  } */
  
  /*
  public static void putAndGet(Machine m, Paquet p){ 
    //TODO
    //cette m�thode actualise le champ myData de Donnees
    //Envoie UN paquet � m et re�oit UN paquet en �change
  }
  */
  
 
  
}


