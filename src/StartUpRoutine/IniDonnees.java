package StartUpRoutine;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

import Stockage.Donnees;
import Stockage.Paquet;
import Utilitaires.Global;
import Utilitaires.Utilitaires;

public class IniDonnees {

	static FileChannel myOwnFile;
	static FileChannel onDisk;
	static boolean finDepassee = false;
	static int id;
	static Paquet paquetTemp;
	static ArrayList<Paquet> paquetsEnConstruction;

	static ByteBuffer bufferUnByte = ByteBuffer.allocateDirect(1);

	public static void iniDonnees() {

		iniPaths();

		bufferUnByte.put((byte) 'a');

		//System.out.println(System.getProperty("user.dir").toString());
		try {
			myOwnFile = FileChannel.open(FileSystems.getDefault().getPath(Global.PATHTOMYDATA), StandardOpenOption.READ, StandardOpenOption.CREATE);
			Donnees.longueur = myOwnFile.size();
			Utilitaires.out("Longueur des données :" +Donnees.longueur);

			while (!finDepassee) {
				paquetsEnConstruction = new ArrayList<Paquet>(Global.NOMBRESOUSPAQUETS);
				//System.out.println("Blah !2");
				for (int i = 0; i < Global.NOMBRESOUSPAQUETSSIGNIFICATIFS; i++) {
					paquetTemp = new Paquet(id, Global.MYSELF);
					onDisk = paquetTemp.fichier;
					
					
					if (!finDepassee && Donnees.longueur > myOwnFile.position() + Global.PAQUET_SIZE) {
						onDisk.transferFrom(myOwnFile, 0, Global.PAQUET_SIZE);
					}
					else if (!finDepassee) {

						onDisk.transferFrom(myOwnFile, 0, Donnees.longueur - myOwnFile.position());
						onDisk.position(Donnees.longueur % Global.PAQUET_SIZE);

						for (long l = 0; l < Global.PAQUET_SIZE - Donnees.longueur % Global.PAQUET_SIZE; l++) {
	
							bufferUnByte.flip();
							onDisk.write(bufferUnByte);
						}

						finDepassee = true;
					}
					else {
						for (int j = 0; j < Global.PAQUET_SIZE; j++) {
							bufferUnByte.flip();
							onDisk.write(bufferUnByte);
						}
					}
					
					id++;
					paquetsEnConstruction.add(paquetTemp);
					paquetTemp.remettrePositionZero();


				}
				
				Donnees.genererPaquetsSecurite(paquetsEnConstruction);
				id++;

				for (int i = 0; i < paquetsEnConstruction.size(); i++)
					Donnees.toSendASAP.push(paquetsEnConstruction.get(i).idGlobal);

			}
			myOwnFile.close();

		}
		catch (IOException e) {
			System.out.println("Il n'y a pas de fichier à l'endroit correct");
			e.printStackTrace();
		}

	}

	public static void iniPaths() {
		if (Global.DEBUG) {
			System.out.println(System.getProperty("user.dir"));
			
			Global.PATHTOMYDATA=System.getProperty("user.dir")+"/../debug/"+Integer.valueOf(Global.NOM).toString()+"/myOwnData/fichier.txt";
			Global.PATHTODATA=System.getProperty("user.dir")+"/../debug/"+Integer.valueOf(Global.NOM).toString()+"/data/";
			
		}
		else {
			Global.PATHTOMYDATA = "myOwnData/fichier.txt";
			Global.PATHTODATA = "data/";
		}
	}

}
