
import StartUpRoutine.IniDonnees;
import StartUpRoutine.IniServer;
import StartUpRoutine.IniTCP;
import StartUpRoutine.IniUDP;
import StartUpRoutine.ServerGetter;
import Utilitaires.Slaver;
import Utilitaires.Utilitaires;


public class Main {

	public static void main(String[] args)  {


		Utilitaires.out("Modal launched !");
		Utilitaires.out("Parsing arguments...............");
		IniServer.iniServer(args);
		Utilitaires.out("Arguments parsed...");
		Utilitaires.out("Ok");

		Utilitaires.out("Initializing data...............");
		IniDonnees.iniDonnees();
		Utilitaires.out("Data initialized...");
		Utilitaires.out("Ok");
		
		Utilitaires.out("Enslaving innocent threads......");
		Slaver.initialize();
		Utilitaires.out("Ok");
		Utilitaires.out("All set and ready to go !");

		Utilitaires.out("Starting TCP Server.............");
		IniTCP.iniTCP();
		Utilitaires.out("Ok");

		Utilitaires.out("Getting server list.............");
		ServerGetter.getServerList();
		Utilitaires.out("Ok");

		Utilitaires.out("Starting UDP server.............");
		IniUDP.iniUDP();
		Utilitaires.out("Ok");

	}
}
