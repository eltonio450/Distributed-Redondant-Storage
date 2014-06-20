import StartUpRoutine.IniServer;
import StartUpRoutine.IniDonnees;
import StartUpRoutine.IniTCP;
import StartUpRoutine.IniUDP;


public class Main{


	public static void main(String[] args)  {
		System.out.println("Modal launched...");
		IniServer.iniServer(args);
		System.out.println("Arguments parsed...");
		IniDonnees.iniDonnees();
		System.out.println("Data initialized...");
		IniUDP.iniUDP();
		System.out.println("UDP server ready...");
		IniTCP.iniTCP();
		System.out.println("TCP server ready...");
		System.out.println("All set and ready to go !");
	}
}
