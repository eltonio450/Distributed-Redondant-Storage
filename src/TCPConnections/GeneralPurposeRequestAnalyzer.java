package TCPConnections;

import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

public class GeneralPurposeRequestAnalyzer extends Thread {
	LinkedList<SocketChannel> aTraiter;
	LinkedList<SocketChannel> aAjouter;
	LinkedList<SocketChannel> aEnlever;
	ReentrantLock lock;
	
	public void run () {
		aTraiter = new LinkedList<SocketChannel> ();
		aAjouter = new LinkedList<SocketChannel> ();
		aEnlever = new LinkedList<SocketChannel> ();
		lock = new ReentrantLock ();
		
		while (true) {
			for (SocketChannel s : aTraiter) {
				// Traitement
			}
			
			lock.lock();
			aTraiter.addAll(aAjouter);
			aTraiter.clear();
			lock.unlock();
			for (SocketChannel s : aEnlever) {
				aTraiter.remove(s);
			}
		}
	}
	
	public void addRequester(SocketChannel requester) {
		lock.lock();
		aAjouter.add(requester);
		lock.unlock();
	}
}
