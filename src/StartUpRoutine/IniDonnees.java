package StartUpRoutine;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.StandardOpenOption;

import Utilitaires.Global;

public class IniDonnees {
	
	static FileChannel myOwnData;
	
	
	public static void iniDonnees () {
		
		iniPaths();
		
		System.out.println(System.getProperty("user.dir").toString());
		try {
			myOwnData = FileChannel.open(FileSystems.getDefault().getPath(Global.PATHTOMYDATA));
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
			Global.PATHTOMYDATA = "/myOwnData/fichier.txt";
			Global.PATHTODATA = "/data/";
		}
	}
 
}
