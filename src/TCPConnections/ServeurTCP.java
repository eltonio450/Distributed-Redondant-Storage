package TCPConnections;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import Utilitaires.Global;
import Utilitaires.Utilitaires;

public class ServeurTCP extends Thread {
	
	public void run () {
		try {
			ServerSocketChannel serverSocket = ServerSocketChannel.open();
			Utilitaires.out("TCP init sur : " + Global.TCP_PORT);
			InetSocketAddress local = new InetSocketAddress(Global.TCP_PORT);
			serverSocket.bind(local);
			while (true) {
				SocketChannel client = serverSocket.accept();
				Utilitaires.out("New connexion");
				Global.GPRA.addRequester(new Requester(client));
			}
		} catch (IOException e) {
			return;
		}
	}
}