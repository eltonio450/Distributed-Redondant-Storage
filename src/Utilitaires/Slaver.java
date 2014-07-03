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
	static final int NB_FASTSLAVES = 50;
	static int indexSlow, indexFast;
	static SlaveThread [] slowSlaves, fastSlaves;
	
	public static final void initialize () {
		slowSlaves = new SlaveThread[NB_SLOWSLAVES];
		fastSlaves = new SlaveThread[NB_FASTSLAVES];
		for (int i=0; i<NB_SLOWSLAVES; i++) {
			slowSlaves[i] = new SlaveThread();
			slowSlaves[i].start();
		}
		for (int i=0; i<NB_FASTSLAVES; i++) {
			fastSlaves[i] = new SlaveThread();
			fastSlaves[i].start();
		}
		
		indexSlow = 0;
		indexFast = 0;
	}
	
	public static final void giveTask (Runnable r, int estimatedLoad) {
		slowSlaves[(indexSlow++) % NB_SLOWSLAVES].doThat(r, estimatedLoad);
	}
	
	public static final void giveUrgentTask(Runnable r, int estimatedLoad) {
		fastSlaves[(indexFast++) % NB_FASTSLAVES].doThat(r, estimatedLoad);

	}
}
