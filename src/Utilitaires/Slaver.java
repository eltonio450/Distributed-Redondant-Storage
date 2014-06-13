package Utilitaires;

/**
 * 
 * @author Simon
 * 
 * Permet de sous-traiter des tâches à des threads esclaves
 * Pas encore utilisé mais sera sans doute utile
 *
 */
public class Slaver {
	static final int NB_SLAVES = 4;
	static SlaveThread [] slaves;
	
	static final void initialize () {
		slaves = new SlaveThread[NB_SLAVES];
		for (int i=0; i<NB_SLAVES; i++) {
			slaves[i] = new SlaveThread();
			slaves[i].run();
		}
	}
	
	static final void giveTask (Runnable r, int estimatedLoad) {
		int minLoad = Integer.MAX_VALUE;
		SlaveThread best = null;
		for (SlaveThread s : slaves) {
			if (s.getLoad() < minLoad) {
				minLoad = s.getLoad();
				best = s;
			}
		}
		if (!best.doThat(r, estimatedLoad)) {
			System.out.println("System overloaded : no more slave thread available.");
			System.exit(-3);
		}
	}
}
