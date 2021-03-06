package Task;

import java.io.IOException;
import java.net.ConnectException;
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
	LinkedList<String> toSendASAP;
	public taskDumpToMachine() {
		allServers = Donnees.getAllServeurs();

		
	}

	public void run() {

		dump();
	}

	public void dump() {
		boolean continuer = true;
		while (continuer) {
			int nbrMachines = allServers.size() ;
			if (nbrMachines == 0) {
				continuer = false ;
			}
			Machine m ;
			
			for (int j = 0 ; j < nbrMachines ; j++) {
				m = Donnees.chooseMachine() ;

					
				if (m != Global.MYSELF && m != null) {

					boolean changeMachine = false;
					toSendASAP = Donnees.chooseManyPaquetToSend1();
					while (!toSendASAP.isEmpty() && !changeMachine) {

						for (String s : toSendASAP) {
						}
						Paquet aEnvoyer = Donnees.removeTemporarlyPaquet(toSendASAP.poll());

						if (aEnvoyer != null && !aEnvoyer.lockLogique) {
							//Donnees.printMyData();
							SocketChannel socket = null;
							
							if (aEnvoyer.askForlock()) {
								try {
									socket = init(m);
									if (socket != null) {


										if (!envoiePaquet(aEnvoyer, m, socket)) {
											
											aEnvoyer.spreadTotalUnlock();
											
											
											
											//Donnees.printMyData();

										}
										else {
											Utilitaires.out("Paquet " + aEnvoyer.idGlobal + " envoyé vers " + m.toString(), 2, true);
											//Donnees.printMyData();

										}
									}
									else
									{
										aEnvoyer.spreadTotalUnlock();
									}
								}
								finally {
									try {
										socket.close();
									}
									catch (IOException e) {
										Utilitaires.out("Erreur dans la fermeture de la socket pour échange de fichier.");
									}
								}
							}

							else {
								
							}

							
						}
						else if (aEnvoyer != null) {
							Utilitaires.out("Paquet " + aEnvoyer.idGlobal + " NON envoyé : lock non obtenu.", 1, true);
							

						}

						if (toSendASAP.isEmpty()) {
							changeMachine = true;
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
		catch (ConnectException e) {
			Donnees.printServerList();
			Utilitaires.out("Poblème avec " + correspondant.toString());
			return null;
		}

		catch (IOException e) {
			Utilitaires.out("Problème dans l'initialisation de la socket pour échanger un paquet avec " + correspondant.port);
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


			if (!s.equals(Message.DEMANDE_ID)) {
				Utilitaires.out("Didnt answer with DEMANDE ID", 5, true);
				Utilitaires.out("Message was : " + s, 5, true);
				return false;
			}

			else {
				//Utilitaires.out("Envoi de l'ID pour l'échange...  " + aEnvoyer.idGlobal, 1, true);
				buffer = Utilitaires.stringToBuffer(aEnvoyer.idGlobal);
				clientSocket.write(buffer);
				buffer.clear();
				clientSocket.read(buffer);
				buffer.flip();
				s = Utilitaires.buffToString(buffer);
				
				if (s.equals(Message.REPONSE_EXCHANGE)) {
					aEnvoyer.envoyerPaquetReellement(clientSocket);

					// la ligne suivant n'a pas l'air de terminer...
					if (recoitPaquet(clientSocket)) {
						aEnvoyer.deleteData();
						aEnvoyer.removePaquet();
						return true;
					}
					else {
						Utilitaires.out("Paquet refusé");
						return false;
					}
				}

				else {
					
					return false;
				}
			}

		}
		catch (IOException e) {
			Utilitaires.out("Exception levée avec la machine " + clientSocket.socket().getPort());
			return false;
		}
		finally {
			try {
				clientSocket.close();
			}
			catch (IOException e) {
			}
		}
	}

	public boolean recoitPaquet(SocketChannel clientSocket) {
		try {
			// Utilitaires.out("Test 1");
			// say I have finished, what Paquet do you want to send to me ?
			ByteBuffer buffer = Utilitaires.stringToBuffer(Message.END_ENVOI);
			clientSocket.write(buffer);
			buffer.clear();
			clientSocket.read(buffer);
			buffer.flip();
			String s = Utilitaires.buffToString(buffer);
			while (!Donnees.acceptePaquet(s) && !s.equals(Message.ANNULE_ENVOI)) {
			
				buffer = Utilitaires.stringToBuffer(Message.DO_NOT_ACCEPT);
				clientSocket.write(buffer);
		
				buffer.clear();
				clientSocket.read(buffer);

				buffer.flip();
				s = Utilitaires.buffToString(buffer);

			}
			if (s.equals(Message.ANNULE_ENVOI)) {
				Utilitaires.out("Recu demande d'annulation");

				return false;
			}
			else {

				buffer = Utilitaires.stringToBuffer(Message.REPONSE_EXCHANGE);
				clientSocket.write(buffer);

				// now receive the package in exchange
				Paquet receivedPaquet = Paquet.recoitPaquetReellement(clientSocket);
				Donnees.securedLock(receivedPaquet.idGlobal);
				Donnees.receptionPaquet(receivedPaquet);

				return true;
			}
		}
		catch (IOException e) {
			return false;
		}
		finally {
			try {
				clientSocket.close();
			}
			catch (IOException e) {
			}

		}
	}
}
