package RelationsPubliques;

import Stockage.Donnees;

public class gestionToSendASAP extends Thread {
	public void run(){
	  //toSendASAPLock.lock() ;
	  while(Donnees.toSendAsapEmpty()){
	    
	  }
	}
	
	
}

//il prend le premier paquet de la file et essaie de l'envoyer quelque part
//je pense que ça ne sert à rien de se battre pour essayer de se débarrasser d'un paquet. Pluot attendre
//que ce paquet puisse effectivement être envoyé un jour (reload toutes les 10 secondes de la liste des 
//serveurs par exemple.

/*
  while(true)
{
  if(trySend(Donnees.toSendASAP.getFirst()))
      Donnees.toSendASAP.remove();
  else
    try {
      Thread.sleep(10000);
    }
    catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
}*/