package RelationsPubliques;

import Stockage.Donnees;
import Task.taskDumpToMachine;
import Utilitaires.Utilitaires;

public class gestionToSendASAP extends Thread {
	public void run() {
		while (true) {

			Donnees.printMyData();
			
			
			/*if (Donnees.toSendAsapEmpty()) {
				Utilitaires.out("Pas Chack", 0, true);
				Donnees.notEmpty.awaitUninterruptibly();
				
			}
			Donnees.toSendASAPLock.unlock();*/
			try {

				Thread.sleep((long)(Math.abs(Math.random()) * 10000));
				//Utilitaires.out("Répartition des paquets", 2, true);

			}
			catch (InterruptedException e) {
			}
			if(!Donnees.toSendAsapEmpty())
			{
				Runnable task = new taskDumpToMachine();
				task.run();
			}
			else {
				Utilitaires.out("------------J'ai fini de vider toSendASAP------------");
				Donnees.printAllServeur();
			}
		}

	}

}

// il prend le premier paquet de la file et essaie de l'envoyer quelque part
// je pense que ça ne sert à rien de se battre pour essayer de se débarrasser
// d'un paquet. Pluot attendre
// que ce paquet puisse effectivement être envoyé un jour (reload toutes les 10
// secondes de la liste des
// serveurs par exemple.

/*
 * while(true) { if(trySend(Donnees.toSendASAP.getFirst()))
 * Donnees.toSendASAP.remove(); else try { Thread.sleep(10000); } catch
 * (InterruptedException e) { // TODO Auto-generated catch block
 * e.printStackTrace(); } }
 */