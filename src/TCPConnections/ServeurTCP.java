package TCPConnections;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import RelationsPubliques.Global;

public class ServeurTCP extends Thread {
	public GeneralPurposeRequestAnalyzer GPRA;
	
	public void run () {
		try {
			ServerSocketChannel serverSocket = ServerSocketChannel.open();
			InetSocketAddress local = new InetSocketAddress(Global.TCP_SERVEUR_PORT);
			serverSocket.bind(local);
			while (true) {
				SocketChannel client = serverSocket.accept();
				GPRA.addRequester(new Requester(client));
			}
		} catch (IOException e) {
			return;
		}
	}
}