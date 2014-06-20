import StartUpRoutine.IniServer;
import StartUpRoutine.IniDonnees;
import StartUpRoutine.IniTCP;
import StartUpRoutine.IniUDP;


public class Main{


	public static void main(String[] args)  {
		
		IniServer.iniServer(args);
		IniDonnees.iniDonnees();
		IniUDP.iniUDP();
		IniTCP.iniTCP();
	}

}
