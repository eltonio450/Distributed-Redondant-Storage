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

public class IniDonnees {

	static FileChannel myOwnFile;
	static FileChannel onDisk;
	static boolean finDepassee = false;
	static int id;
	static Paquet paquetTemp;
	static ArrayList<Paquet> paquetsEnConstruction;

	static ByteBuffer bufferUnByte = ByteBuffer.allocateDirect(1);

	public static void iniDonnees () {

		iniPaths();

		bufferUnByte.put((byte) 'a');



		System.out.println(System.getProperty("user.dir").toString());
		try {
			//System.out.println("Blah !3");
			myOwnFile = FileChannel.open(FileSystems.getDefault().getPath(Global.PATHTOMYDATA),StandardOpenOption.READ, StandardOpenOption.CREATE);
			Donnees.longueur = myOwnFile.size();
			System.out.println(Donnees.longueur);


			

			while(!finDepassee)
			{
				//System.out.println("Blah !4");

				paquetsEnConstruction = new ArrayList<Paquet>(Global.NOMBRESOUSPAQUETS);
				//System.out.println("Blah !4");
				for(int i = 0;i<Global.NOMBRESOUSPAQUETSSIGNIFICATIFS;i++)
				{
					//System.out.println("Blah !5");
					//System.out.println("Blah !1");
					onDisk = FileChannel.open(FileSystems.getDefault().getPath(Global.PATHTODATA+"/"+Global.MYSELF.toString()+"-"+id+".txt"),StandardOpenOption.WRITE, StandardOpenOption.CREATE);
					//System.out.println("Blah !2");
					if(!finDepassee && Donnees.longueur > myOwnFile.position()+Global.PAQUET_SIZE)
					{
						if(i==0)
							paquetTemp = new Paquet(id, Global.MYSELF);//, false, 0);
						else
							paquetTemp = new Paquet(id, Global.MYSELF);//, false, 0);

						onDisk.transferFrom(myOwnFile, 0, Global.PAQUET_SIZE);
					}
					else if(!finDepassee)
					{

						paquetTemp = new Paquet(id, Global.MYSELF);//, true, Global.PAQUET_SIZE - Donnees.longueur%Global.PAQUET_SIZE);

						onDisk.transferFrom(myOwnFile, 0, Donnees.longueur - myOwnFile.position());
						onDisk.position(Donnees.longueur%Global.PAQUET_SIZE);

						for(long l = 0;l<Global.PAQUET_SIZE - Donnees.longueur%Global.PAQUET_SIZE;l++)
						{
							//System.out.println("Blah !");
							bufferUnByte.flip();

							onDisk.write(bufferUnByte);
						}



						finDepassee = true;
					}
					else
					{
						paquetTemp = new Paquet(id, Global.MYSELF);//, false, 0);

						for(int j=0;j<Global.PAQUET_SIZE;j++)
						{
							bufferUnByte.flip();

							onDisk.write(bufferUnByte);
						}
					}
					id++;
					paquetsEnConstruction.add(paquetTemp);
					onDisk.close();

				}
				Donnees.genererPaquetsSecurite(paquetsEnConstruction);
				id++;
				
				for(int i = 0;i<paquetsEnConstruction.size();i++)
					Donnees.toSendASAP.push(paquetsEnConstruction.get(i).idGlobal);
			

			}
			myOwnFile.close();

		} catch (IOException e) {
			System.out.println("Il n'y a pas de fichier à l'endroit correct");
			e.printStackTrace();
		}



	}


	public static void iniPaths()
	{
		if(Global.DEBUG)
		{
			;


			//pathToMyData dépend du mode : si le mode debug est activé, un dossier prefixe avec l'id du programme est créé. Sinon elle est juste dans myOwnData/ 

		}
		else
		{
			Global.PATHTOMYDATA = "myOwnData/fichier.txt";
			Global.PATHTODATA = "data/";
		}
	}

}
