import StartUpRoutine.IniServer;
import StartUpRoutine.IniDonnees;


public class Main{


	public static void main(String[] args)  {
		
		IniServer.iniServer(args);
		IniDonnees.iniDonnees();
		IniUDP.iniUDP();
		
		
		//Etape 1: Définition des options
		
	}

}
