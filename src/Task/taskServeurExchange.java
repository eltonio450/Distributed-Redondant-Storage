package Task;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

import Stockage.Donnees;
import Stockage.Machine;
import Stockage.Paquet;
import Utilitaires.Global;
import Utilitaires.Message;
import Utilitaires.Utilitaires;

public class taskServeurExchange implements Runnable {

	SocketChannel socket;

	public taskServeurExchange(SocketChannel s) {
		socket = s;
	}

	public void recoitPaquet() throws IOException {
		// Utilitaires.out("Recu depuis celui qui doit recevoir",1,true);
		socket.write(Utilitaires.stringToBuffer(Message.DEMANDE_ID));
		ByteBuffer buffer = ByteBuffer.allocateDirect(Message.BUFFER_LENGTH);
		buffer.clear();
		socket.read(buffer);
		buffer.flip();
		String s = Utilitaires.buffToString(buffer);

		if (Donnees.acceptePaquet(s)) {
			//Utilitaires.out("J'accepte de recevoir le paquet que tu me proposes.", 1, true);
			buffer = Utilitaires.stringToBuffer(Message.REPONSE_EXCHANGE);
			socket.write(buffer);
			// Utilitaires.out("J'ai accepté d'échanger ce paquet",1,true);

			Paquet receivedPaquet = Paquet.recoitPaquetReellement(socket);
			//Utilitaires.out("J'ai bien reçu le paquet que tu me proposais", 1, true);

			Paquet sentPaquet = envoitPaquet();
			if (sentPaquet != null) {
				Donnees.receptionPaquet(receivedPaquet);
				sentPaquet.removePaquet();
			}
			else {
				Donnees.removePaquet(receivedPaquet);
			}
		}
		else {
			buffer = Utilitaires.stringToBuffer(Message.ANNULE_ENVOI);
			socket.write(buffer);
		}

	}

	public Paquet envoitPaquet() throws IOException {

		ByteBuffer buffer = ByteBuffer.allocateDirect(Message.BUFFER_LENGTH);
		buffer.clear();
		socket.read(buffer);
		buffer.flip();
		String s = Utilitaires.buffToString(buffer);
		Paquet aEnvoyer = null;

		if (s.equals(Message.END_ENVOI)) {
			boolean ok = false;

			// try with toSendASAP
			LinkedList<String> paquets1 = Donnees.chooseManyPaquetToSend1();

			while (!ok && !paquets1.isEmpty()) {
				aEnvoyer = Donnees.removeTemporarlyPaquet(paquets1.pop());
				if (aEnvoyer != null && aEnvoyer.askForlock()) {
					buffer = Utilitaires.stringToBuffer(aEnvoyer.idGlobal);
					socket.write(buffer);
					buffer.clear();
					socket.read(buffer);
					buffer.flip();
					s = Utilitaires.buffToString(buffer);
					if (s.equals(Message.REPONSE_EXCHANGE)) {
						aEnvoyer.envoyerPaquetReellement(socket);
						ok = true;
					}
					
					else {
						Donnees.putNewPaquet(aEnvoyer);
						aEnvoyer.spreadUnlock();
					}
				}
				else if(aEnvoyer !=null)
				{
					aEnvoyer.spreadUnlock();
				}
			}

			if (!ok) {
				// try with all data
				LinkedList<String> paquets2 = Donnees.chooseManyPaquetToSend2();

				while (!ok && !paquets2.isEmpty()) {
					aEnvoyer = Donnees.removeTemporarlyPaquet(paquets2.pop());
					if (aEnvoyer != null && aEnvoyer.askForlock()) {
						buffer = Utilitaires.stringToBuffer(aEnvoyer.idGlobal);
						socket.write(buffer);
						buffer.clear();
						socket.read(buffer);
						buffer.flip();
						s = Utilitaires.buffToString(buffer);
						if (s.equals(Message.REPONSE_EXCHANGE)) {
							aEnvoyer.envoyerPaquetReellement(socket);
							ok = true;
						}
						else {
							Donnees.putNewPaquet(aEnvoyer);
							aEnvoyer.spreadUnlock();
						}
					}
					else if(aEnvoyer !=null)
					{
						aEnvoyer.spreadUnlock();
					}
				}

				if (!ok) {
					buffer = Utilitaires.stringToBuffer(Message.ANNULE_ENVOI);
					socket.write(buffer);
					return aEnvoyer;
				}
				else {
					return aEnvoyer;
				}

			}
			else {
				return aEnvoyer;
			}

		}
		else {
			return aEnvoyer;
		}
	}

	public void run() {
		try {
			recoitPaquet();
		}
		catch (IOException e) {
			// traiter l'erreur - recommencer l'envoie ?
			// a priori le client s'est rendu compte du probl�me et va
			// recommencer tout seul
		}
	}

}
