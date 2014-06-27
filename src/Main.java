
import java.nio.ByteBuffer;

import StartUpRoutine.IniDonnees;
import StartUpRoutine.IniServer;
import StartUpRoutine.IniTCP;
import StartUpRoutine.IniUDP;
import StartUpRoutine.ServerGetter;


public class Main {


	public static void main(String[] args)  {
		String fuck = "fuck la de";
		System.out.println("Modal launched !");
		System.out.print("Parsing arguments...............");
		IniServer.iniServer(args);
		System.out.println("Arguments parsed...");
		System.out.println("Ok");
		
		System.out.print("Initializing data...............");
		IniDonnees.iniDonnees();
		System.out.println("Data initialized...");
		System.out.println("Ok");
		
		System.out.print("Starting TCP Server.............");
		IniTCP.iniTCP();
		System.out.println("Ok");
		
		System.out.print("Getting server list.............");
		ServerGetter.getServerList();
		System.out.println("Ok");
		
		System.out.print("Starting UDP server.............");
		IniUDP.iniUDP();
		System.out.println("Ok");
		
		System.out.println("Enslaving innocent threads....");
		Utilitaires.Slaver.initialize();
		
		System.out.println("All set and ready to go !");
	}
}
