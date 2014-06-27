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
	static final int NB_SLOWSLAVES = 100;
	static final int NB_FASTSLAVES = 20;
	static SlaveThread [] slowSlaves, fastSlaves;
	
	static final void initialize () {
		slowSlaves = new SlaveThread[NB_SLOWSLAVES];
		fastSlaves = new SlaveThread[NB_FASTSLAVES];
		for (int i=0; i<NB_SLOWSLAVES; i++) {
			slowSlaves[i] = new SlaveThread();
			slowSlaves[i].run();
		}
		for (int i=0; i<NB_FASTSLAVES; i++) {
			fastSlaves[i] = new SlaveThread();
			fastSlaves[i].run();
		}
	}
	
	public static final void giveTask (Runnable r, int estimatedLoad) {
		int minLoad = Integer.MAX_VALUE;
		SlaveThread best = null;
		for (SlaveThread s : slowSlaves) {
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
	
	public static final void giveUrgentTask(Runnable r, int estimatedLoad) {
		int minLoad = Integer.MAX_VALUE;
		SlaveThread best = null;
		for (SlaveThread s : fastSlaves) {
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
