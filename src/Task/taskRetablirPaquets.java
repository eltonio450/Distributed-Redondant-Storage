package Task;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import Stockage.Donnees;
import Stockage.Paquet;
import Utilitaires.Global;
import Utilitaires.Message;
import Utilitaires.Utilitaires;

/**
 * 
 * @author antoine
 * @param f
 *            : paquet dont on cherche les frères pour reconstruire le paquet
 *            manquant.
 * @param num
 *            : le numéro du paquet manquant
 */
public class taskRetablirPaquets implements Runnable {

	Paquet frere;
	int numeroMort;
	SocketChannel[] clientSocket = new SocketChannel[Global.NOMBRESOUSPAQUETS];
	ByteBuffer[] b = new ByteBuffer[Global.NOMBRESOUSPAQUETS];
	// ByteBuffer temp;
	Paquet reconstruit;
	int newByte = 0;

	public taskRetablirPaquets(Paquet f, int num) {
		this.frere = f;
		this.numeroMort = num;
		for (int i = 0; i < Global.NOMBRESOUSPAQUETS; i++)
			b[i] = ByteBuffer.allocate((int) (Global.PAQUET_SIZE + 3));

		this.reconstruit = new Paquet(frere.idMachine - frere.idInterne
				+ numeroMort, frere.owner);
		try{
			for(int i = 0 ; i< Global.NOMBRESOUSPAQUETS;i++){
				if(i != numeroMort){
					clientSocket[i] = SocketChannel.open() ;
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}

	}

	@Override
	public void run() {

		// Etape 1: se connecter sur les autres paquets et récupérer le buffer
		// correspond.

		while (frere.askForlock(numeroMort)) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		for (int i = 0; i < Global.NOMBRESOUSPAQUETS; i++) {
			if (i != numeroMort && i != frere.idInterne) {
				try {
					InetSocketAddress local = new InetSocketAddress(0);
					clientSocket[i].bind(local);
					InetSocketAddress remote = new InetSocketAddress(
							frere.otherHosts.get(i).ipAdresse,
							frere.otherHosts.get(i).port);
					clientSocket[i].connect(remote);
					clientSocket[i].write(Utilitaires
							.stringToBuffer(Message.DEMANDE_PAQUET));

					// Etape 2 : attendre que le monsieur réponde qu'il veut
					// bien nous envoyer le paquet
					ByteBuffer buffer = ByteBuffer.allocateDirect(Message.BUFFER_LENGTH) ;
					buffer.clear() ;
					clientSocket[i].read(buffer) ;
					buffer.flip() ;
					if(!Utilitaires.buffToString(buffer).equals(Message.OK)) {
					  Utilitaires.out("Erreur reconstruction paquet ");
					}

					// Etape 3 : Envoyer le numero du paquet
					clientSocket[i].write(Utilitaires
							.stringToBuffer(frere.owner.toString()
									+ "-"
									+ (frere.idMachine - frere.idInterne + i)));

					// Etage 4 : recevoir le paquet dans le buffer
					b[i].clear();
					while (b[i].position() != Global.PAQUET_SIZE)
						clientSocket[i].read(b[i]);
					b[i].flip();

					clientSocket[i].close();
					// Etape 5 : remercier
					// nan en fait on s'en fout
					// clientSocket[i].write(Utilitaires.stringToBuffer(Message.OK));

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		try {
			frere.fichier.read(b[frere.idInterne]);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		frere.remettrePositionZero();

		for (int j = 0; j < Global.PAQUET_SIZE; j++) {
			b[reconstruit.idInterne].clear();

			if (numeroMort >= Global.NOMBRESOUSPAQUETSSIGNIFICATIFS) {
				for (int i = 0; i < Global.NOMBRESOUSPAQUETSSIGNIFICATIFS; i++)
					newByte += (int) b[i].get(j);
				newByte %= 256;
				b[numeroMort].put((byte) newByte);
			} else {
				for (int i = 0; i < Global.NOMBRESOUSPAQUETSSIGNIFICATIFS; i++) {
					if (i != numeroMort)
						newByte += (int) b[i].get(j);

				}
				newByte = (b[Global.NOMBRESOUSPAQUETSSIGNIFICATIFS - 1].get(j) - newByte) % 256;
				b[numeroMort].put((byte) newByte);
			}

			b[reconstruit.idInterne].flip();

		}
		try {
			reconstruit.fichier.write(b[numeroMort]);
			reconstruit.remettrePositionZero();
		} catch (IOException e) {
			e.printStackTrace();
		}
		reconstruit.putOtherHosts(frere.otherHosts);
		reconstruit.otherHosts.set(numeroMort, Global.MYSELF) ;
		Donnees.receptionPaquet(reconstruit);
		Donnees.addPaquetToSendAsap(reconstruit.idGlobal);
	}
}
