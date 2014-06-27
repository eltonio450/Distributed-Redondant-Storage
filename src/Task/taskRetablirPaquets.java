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
	ByteBuffer[] b = new ByteBuffer[Global.NOMBRESOUSPAQUETS];
	ByteBuffer temp;
	Paquet reconstruit;
	
	
	
	public taskRetablirPaquets (Paquet f, int num) {
		frere = f;
		numeroMort = num;
		for(int i = 0; i<Global.NOMBRESOUSPAQUETS;i++)
			b[i] = ByteBuffer.allocate((int) (Global.PAQUET_SIZE+3));
		
		Paquet reconstruit = new Paquet(frere.idMachine-frere.idInterne+numeroMort,frere.owner);
	
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
					//Il faut utiliser la super fonction de simon.
					
					//Etape 3 : Envoyer le numero du paquet
					clientSocket[i].write(Utilitaires.stringToBuffer(frere.otherHosts.get(i).toString()+"-"+(frere.idMachine-frere.idInterne+i)));
					//Etage 4 : recevoir le paquet dans le buffer
					
					clientSocket[i].read(b[i],0,Global.PAQUET_SIZE);
					
					
					//Etape 5 : remercier
					clientSocket[i].write(Utilitaires.stringToBuffer(Message.OK));
					
					
					
					
				}
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				for(int j = 0;j<Global.PAQUET_SIZE;j++)
				{
					temp.clear();
					
					
					temp.flip();
					try {
						reconstruit.fichier.write(temp);
					}
					catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				reconstruit.remettrePositionZero();
			}
		
		//Etape 2 : reconstruire le paquet
		
		
	}
}
