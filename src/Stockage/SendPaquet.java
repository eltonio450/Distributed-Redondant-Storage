package Stockage;

import java.util.ArrayList;

public class SendPaquet {

  public static void envoieData(Donnees d,ArrayList<Paquet> listPaquets, ArrayList<Machine> hosts){
    //envoie un groupe de paquets à un groupe d'hosts
    //stock les paquets reçus en échange dans d
    int n = listPaquets.size() ; //should equal 5
    assert n == hosts.size() ;
    for (int j = 0 ; j< n ; j++){
      Paquet p = listPaquets.get(j) ;
      p.putOtherHosts(hosts);
      Paquet q = putAndGet(hosts.get(j),p) ;
      d.addHost(hosts.get(j)) ;
    }
  }
  
  public static Paquet putAndGet(Machine m, Paquet p){ //TODO
    //Envoie UN paquet à m et reçoit UN paquet en échange
    return null ;
  }
  
  public static void prevenirHostChanged(Machine m , long Id){  //TODO
    //préviens une machine que cette machine remplace m pour le paquet d'id Id
  }
}
