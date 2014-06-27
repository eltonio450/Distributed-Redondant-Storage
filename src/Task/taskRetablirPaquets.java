package Task;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import Stockage.Paquet;
import Utilitaires.Global;
import Utilitaires.Message;
import Utilitaires.Utilitaires;

public class taskRetablirPaquets implements Runnable {
	
	Paquet frere;
	int numeroMort;
	SocketChannel[] clientSocket = new SocketChannel[Global.NOMBRESOUSPAQUETS];
	ByteBuffer[] b;
	public taskRetablirPaquets (Paquet f, int num) {
		frere = f;
		numeroMort = num;
		for(int i = 0; i<Global.NOMBRESOUSPAQUETS;i++)
			b[i] = ByteBuffer.allocate((int) (Global.PAQUET_SIZE+3));
		
	
	}
	
	@Override
	public void run() {
		//Etape 1: se connecter sur les autres paquets
		
		for(int i =0;i<Global.NOMBRESOUSPAQUETS;i++)
			if(i!=numeroMort){
				
				try {
					InetSocketAddress local = new InetSocketAddress(0); 
					clientSocket[i].bind(local); 
					InetSocketAddress remote = new InetSocketAddress(frere.otherHosts.get(i).ipAdresse, frere.otherHosts.get(i).port); 
					clientSocket[i].connect(remote);
					clientSocket[i].write(Utilitaires.stringToBuffer(Message.DEMANDE_PAQUET));
					
					//Etape 2 : attendre que le monsieur réponde qu'il veut bien nous envoyer le paquet
					
					
					//Etape 3 : Envoyer le numero du paquet
				
				
					//ETpae 5 : remercier
				}
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
		
		//Etape 2 : reconstruire le paquet
		
		
	}
}
