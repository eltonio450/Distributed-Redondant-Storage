package TCPConnections;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import Utilitaires.Global;

public class ServeurTCP extends Thread {
	
	public void run () {
		try {
			ServerSocketChannel serverSocket = ServerSocketChannel.open();
			InetSocketAddress local = new InetSocketAddress(Global.TCP_PORT);
			serverSocket.bind(local);
			while (true) {
				SocketChannel client = serverSocket.accept();
				System.out.println("New connexion");
				Global.GPRA.addRequester(new Requester(client));
			}
		} catch (IOException e) {
			return;
		}
	}
}