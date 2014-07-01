package Utilitaires;

import java.util.concurrent.LinkedBlockingQueue;

public class SlaveThread extends Thread{
	private LinkedBlockingQueue<Runnable> tasks;	// Vérifier si l'attente sur une file vide ne prend pas trop de temps

	public void run () {
		Runnable r = null;

		while (true) {
			try {
				r = tasks.take();
				r.run();
			} catch (Exception e) {
				Utilitaires.out("Exception in SlaverThread. Task was " + r.getClass());
				e.printStackTrace();
				// We don't want to crash the slave if the runnable is wrong.
			}
		}
	}

	public boolean doThat(Runnable r, int estimatedLoad) {
		tasks.add(r);
		return true;
	}


	public SlaveThread () {
		tasks = new LinkedBlockingQueue<Runnable>();
	}
}
