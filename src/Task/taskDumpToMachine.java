package Task;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

import Stockage.Donnees;
import Stockage.Machine;
import Stockage.Paquet;
import Utilitaires.Global;
import Utilitaires.Message;
import Utilitaires.Utilitaires;

public class taskDumpToMachine implements Runnable {

	private LinkedList<Machine> allServers;

	public taskDumpToMachine() {
		allServers = Donnees.getAllServeurs();
	}

	public void run() {
		LinkedList<String> toSendASAP = Donnees.chooseManyPaquetToSend1();
		dump(toSendASAP);
	}

	public void dump(LinkedList<String> toSendASAP) {
		Utilitaires.out("Entree dans la fonction DUMP");
		boolean continuer = true;
		while (continuer) {
			// Utilitaires.out("Test 0");
			for (Machine m : allServers) {
				if (m != Global.MYSELF) {
					// Utilitaires.out("Test 1");

					boolean changeMachine = false;

					while (!toSendASAP.isEmpty() && !changeMachine) {

						Paquet aEnvoyer = Donnees.removeTemporarlyPaquet(toSendASAP.poll());

						if (aEnvoyer != null && !aEnvoyer.lockLogique) {
							Utilitaires.out("J'ai choisi d'envoyer ce paquet : " + aEnvoyer.idGlobal, 1, true);
							if (aEnvoyer.askForlock()) {
								SocketChannel socket = init(m);
								if (socket != null) {
									if (!envoiePaquet(aEnvoyer, m, socket)) {
										Utilitaires.out("Paquet " + aEnvoyer.idGlobal + " NON envoyé vers " + m.toString(), 1, true);
										Donnees.putNewPaquet(aEnvoyer);
										aEnvoyer.spreadUnlock();
										try {
											Thread.sleep(5000);
										}
										catch (InterruptedException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										Donnees.printMyData();
										Donnees.printUnlockedInMyData();
									}
									else {
										Utilitaires.out("Paquet " + aEnvoyer.idGlobal + " envoyé vers " + m.toString(), 2, true);
										Donnees.printMyData();
										Donnees.printUnlockedInMyData();
									}
									try {
										socket.close();
									}
									catch (IOException e) {
										Utilitaires.out("Erreur dans la fermeture de la socket pour échange de fichier.");
										e.printStackTrace();
									}
								}
							}
							else
							{
								Donnees.putNewPaquet(aEnvoyer);
								aEnvoyer.spreadUnlock();
							}
							
							changeMachine = true;
						}
						else if(aEnvoyer != null)
						{
							Donnees.putNewPaquet(aEnvoyer);
							//aEnvoyer.spreadUnlock();
						}

						if (toSendASAP.isEmpty()) {
							continuer = false;
						}
						

					}
				}
			}

			try {
				Thread.sleep(Global.TIME_TO_SLEEP_Dumping);
			}
			catch (InterruptedException e) {
			}
		}

	}

	public SocketChannel init(Machine correspondant) {
		// return true if succeeded

		try {
			SocketChannel clientSocket = SocketChannel.open();
			// init connection
			InetSocketAddress local = new InetSocketAddress(0);
			clientSocket.bind(local);
			InetSocketAddress remote = new InetSocketAddress(correspondant.ipAdresse, correspondant.port);
			clientSocket.connect(remote);
			clientSocket.configureBlocking(true);

			// Utilitaires.out("Succès dans l'initialisation de la socket avec "
			// + correspondant.port);
			return (clientSocket);
		}
		catch (IOException e) {
			Utilitaires.out("Problème dans l'initialisation de la socket pour échanger un paquet avec " + correspondant.port);
			e.printStackTrace();
			return null;

		}
	}

	public boolean envoiePaquet(Paquet aEnvoyer, Machine correspondant, SocketChannel clientSocket) {
		try {
			// ask to exchange
			ByteBuffer buffer = Utilitaires.stringToBuffer(Message.EXCHANGE);
			clientSocket.write(buffer);
			buffer.clear();
			clientSocket.read(buffer);
			buffer.flip();
			String s = Utilitaires.buffToString(buffer);
			// Utilitaires.out("Test 1");

			if (!s.equals(Message.DEMANDE_ID)) {
				clientSocket.close();
				return false;
			}

			else {
				Utilitaires.out("J'aimerais envoyer ce fichier : " + aEnvoyer.idGlobal, 1, true);
				buffer = Utilitaires.stringToBuffer(aEnvoyer.idGlobal);
				clientSocket.write(buffer);
				buffer.clear();
				clientSocket.read(buffer);
				buffer.flip();
				s = Utilitaires.buffToString(buffer);

				if (s.equals(Message.REPONSE_EXCHANGE)) {
					// Utilitaires.out("Test 3");
					// exchange can begin : send its package
					Utilitaires.out("Merci d'avoir accepté, je te l'envoie réellement", 1, true);
					aEnvoyer.envoyerPaquetReellement(clientSocket);

					// la ligne suivant n'a pas l'air de terminer...
					if (recoitPaquet(clientSocket)) {
						//Utilitaires.out("Ici tout a fonctionné");
						aEnvoyer.removePaquet();
						return true;
					}
					else {
						Utilitaires.out("Paquet refusé");
						return false;
					}
				}

				else {
					// Utilitaires.out("Test 3 Echec");
					clientSocket.close();
					return false;
				}
			}

		}
		catch (IOException e) {
			Utilitaires.out("Exception levée avec la machine " + clientSocket.socket().getPort());
			e.printStackTrace();
			return false;
		}
	}

	public boolean recoitPaquet(SocketChannel clientSocket) {
		try {
			// Utilitaires.out("Test 1");
			// say I have finished, what Paquet do you want to send to me ?
			ByteBuffer buffer = Utilitaires.stringToBuffer(Message.END_ENVOI);
			clientSocket.write(buffer);
			Utilitaires.out("J'ai fini de te l'envoyer réellement, maintenant à toi !", 1, true);
			// Utilitaires.out("J'ai fini d'envoyer le paquet");
			buffer.clear();
			clientSocket.read(buffer);
			// Utilitaires.out("Test 2");
			buffer.flip();
			String s = Utilitaires.buffToString(buffer);
			// Utilitaires.out("Message reçu : " +s,1,true);
			while (!Donnees.acceptePaquet(s) && !s.equals(Message.ANNULE_ENVOI)) {
				Utilitaires.out("Test 2");
				buffer = Utilitaires.stringToBuffer(Message.DO_NOT_ACCEPT);
				clientSocket.write(buffer);
				buffer.clear();
				clientSocket.read(buffer);
				buffer.flip();
				s = Utilitaires.buffToString(buffer);
			}
			if (s.equals(Message.ANNULE_ENVOI)) {
				// Utilitaires.out("Test 3");
				clientSocket.close();
				return false;
			}
			else {
				//Utilitaires.out("Test 4");
				buffer = Utilitaires.stringToBuffer(Message.REPONSE_EXCHANGE);
				clientSocket.write(buffer);

				// now receive the package in exchange
				Paquet receivedPaquet = Paquet.recoitPaquetReellement(clientSocket);
				receivedPaquet.lock();
				Donnees.receptionPaquet(receivedPaquet);
				//receivedPaquet.unlock();
				// receivedPaquet.spreadUnlock();
				clientSocket.close();
				return true;
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
}