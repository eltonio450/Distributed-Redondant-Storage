package Utilitaires;

import java.util.concurrent.LinkedBlockingQueue;

public class SlaveThread extends Thread{
	private LinkedBlockingQueue<Runnable> tasks;	// Vérifier si l'attente sur une file vide ne prend pas trop de temps

	public void run () {
		Runnable r = null;

		while (true) {
			try {Utilitaires.out("BOCK "+ tasks.size());
				r = tasks.take();
				Utilitaires.out("BOCK 2 "+ tasks.size());
				r.run();
				Utilitaires.out("BOCK 3 "+ tasks.size());
			} catch (Exception e) {
				Utilitaires.out("Exception in SlaverThread. Task was " + r.getClass());
				e.printStackTrace();
				// We don't want to crash the slave if the runnable is wrong.
			}
		}
	}

	public boolean doThat(Runnable r, int estimatedLoad) {
		try{
			Utilitaires.out("Dans Slave Thread 1");
			tasks.add(r);
			Utilitaires.out("Dans Slave Thread 2 "+ tasks.size());
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return true;
	}


	public SlaveThread () {
		tasks = new LinkedBlockingQueue<Runnable>();
	}
}
