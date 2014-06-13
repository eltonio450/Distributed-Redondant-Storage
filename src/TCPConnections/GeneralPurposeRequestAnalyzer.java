package TCPConnections;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

import Utilitaires.Utilitaires;

public class GeneralPurposeRequestAnalyzer extends Thread {
	LinkedList<Requester> aTraiter;
	LinkedList<Requester> aAjouter;
	LinkedList<Requester> aEnlever;
	ReentrantLock lock;
	
	public void run () {
		aTraiter = new LinkedList<Requester> ();
		aAjouter = new LinkedList<Requester> ();
		aEnlever = new LinkedList<Requester> ();
		lock = new ReentrantLock ();
		ByteBuffer buff = ByteBuffer.allocateDirect(10000);
		
		while (true) {
			for (Requester r : aTraiter) {
				try {
					r.socket.read(buff);
				} catch (IOException e) {
					aEnlever.add(r);
				}
				buff.flip();
				r.recu += Utilitaires.buffToString(buff);
				try {
					traiter(r.recu);
				} catch (Exception e) {
					
				}
			}
			
			lock.lock();
			aTraiter.addAll(aAjouter);
			aTraiter.clear();
			lock.unlock();
			for (Requester r : aEnlever) {
				aTraiter.remove(r);
			}
		}
	}
	
	public void addRequester(Requester requester) {
		lock.lock();
		try {
			requester.socket.configureBlocking(false);
		} catch (IOException e) {
			System.out.println("Problème de changement Blocking / Non blocking");
			return;
		}
		aAjouter.add(requester);
		lock.unlock();
	}
	
	private void traiter (String messages) {
		
	}
}
