package Task;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

import Stockage.Donnees;
import Stockage.Paquet;
import Utilitaires.Message;
import Utilitaires.Utilitaires;

public class taskServeurExchange implements Runnable {

	SocketChannel socket;

	public taskServeurExchange(SocketChannel s) {
		socket = s;
	}

	public void recoitPaquet() throws IOException {
		try {
			socket.write(Utilitaires.stringToBuffer(Message.DEMANDE_ID));
			ByteBuffer buffer = ByteBuffer.allocateDirect(Message.BUFFER_LENGTH);
			buffer.clear();
			socket.read(buffer);
			buffer.flip();
			String s = Utilitaires.buffToString(buffer);

			if (Donnees.acceptePaquet(s)) {

				buffer = Utilitaires.stringToBuffer(Message.REPONSE_EXCHANGE);
				socket.write(buffer);

				Paquet receivedPaquet = Paquet.recoitPaquetReellement(socket);

				Paquet sentPaquet = envoitPaquet();
				if (sentPaquet != null) {
					Donnees.receptionPaquet(receivedPaquet);
					sentPaquet.removePaquet();
				}
				else {
					receivedPaquet.deleteData();
				}
			}
			else {
				//Donnees.printMyData();
				buffer = Utilitaires.stringToBuffer(Message.ANNULE_ENVOI);
				socket.write(buffer);

			}
		}
		finally {
			socket.close();
		}

	}

	public Paquet envoitPaquet() throws IOException {
		Utilitaires.out("ARF 0");
		ByteBuffer buffer = ByteBuffer.allocateDirect(Message.BUFFER_LENGTH);
		buffer.clear();
		socket.read(buffer);
		Utilitaires.out("ARF 1");
		buffer.flip();
		String s = Utilitaires.buffToString(buffer);
		Paquet aEnvoyer = null;
		Utilitaires.out("ARF 2 "+s);
		if (s.equals(Message.END_ENVOI)) {
			boolean ok = false;
			Utilitaires.out("ARF 4 ");
			// try with toSendASAP
			LinkedList<String> paquets1 = Donnees.chooseManyPaquetToSend1();

			while (!ok && !paquets1.isEmpty()) {
				aEnvoyer = Donnees.removeTemporarlyPaquet(paquets1.pop());
				//Utilitaires.out("Paquet choisi : " + aEnvoyer.idGlobal,1,true);
				Utilitaires.out("ARF 5");
				if (aEnvoyer != null && aEnvoyer.askForlock()) {
					Utilitaires.out("ARF 6");
					buffer = Utilitaires.stringToBuffer(aEnvoyer.idGlobal);
					socket.write(buffer);
					buffer.clear();
					socket.read(buffer);
					buffer.flip();
					s = Utilitaires.buffToString(buffer);
					if (s.equals(Message.REPONSE_EXCHANGE)) {
						Utilitaires.out("60.2");
						aEnvoyer.envoyerPaquetReellement(socket);
						ok = true;
					}

					else {
						Utilitaires.out("ARf 60.5");
						Donnees.putNewPaquet(aEnvoyer);
						aEnvoyer.spreadUnlock();
						Utilitaires.out("ARf 60.8");
						aEnvoyer = null;
					}

				}
				else if (aEnvoyer != null) {
					Utilitaires.out("ARF 61");
					Donnees.putNewPaquet(aEnvoyer);

					aEnvoyer.spreadUnlock();
					aEnvoyer = null;
				}
			}
			Utilitaires.out("ARf 60.85");
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
							aEnvoyer = null;
						}
					}

					else if (aEnvoyer != null) {
						Donnees.putNewPaquet(aEnvoyer);

						aEnvoyer.spreadUnlock();
						aEnvoyer = null;
					}
				}
				Utilitaires.out("ARf 60.88");

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

		}
	}

}
