package Stockage;

import java.util.ArrayList;
 
 public class SendPaquet {
 
   public static void envoieData(Donnees d,ArrayList<Paquet> listPaquets, ArrayList<Machine> hosts){
     //envoie un groupe de paquets � un groupe d'hosts
     //stock les paquets re�us en �change dans d
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
     //Envoie UN paquet � m et re�oit UN paquet en �change
     return null ;
   }
   
   public static void prevenirHostChanged(Machine m , long Id){  //TODO
     //pr�viens une machine que cette machine remplace m pour le paquet d'id Id
   }
   
  
 }