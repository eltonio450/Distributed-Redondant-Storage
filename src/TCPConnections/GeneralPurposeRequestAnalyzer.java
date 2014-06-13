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
				//Blablabla
			}
			aTraiter.addAll(aAjouter);
		}
	}
	
	public void addRequester(SocketChannel requester) {
		aAjouter.add(requester);
	}
}
