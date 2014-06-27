
import StartUpRoutine.IniServer;
import StartUpRoutine.IniDonnees;
import StartUpRoutine.IniTCP;
import StartUpRoutine.IniUDP;
import StartUpRoutine.ServerGetter;
import Stockage.Paquet;
import Utilitaires.Global;
import Utilitaires.Slaver;


public class Main {


	public static void main(String[] args)  {

		System.out.println("Modal launched !");
		System.out.print("Parsing arguments...............");
		IniServer.iniServer(args);
		System.out.println("Arguments parsed...");
		System.out.println("Ok");

		System.out.print("Initializing data...............");
		IniDonnees.iniDonnees();
		System.out.println("Data initialized...");
		System.out.println("Ok");
		
    System.out.print("Enslaving innocent threads......");
    Slaver.initialize();
    System.out.println("Ok");
    System.out.println("All set and ready to go !");

		
		System.out.print("Starting TCP Server.............");
		IniTCP.iniTCP();
		System.out.println("Ok");

		System.out.print("Getting server list.............");
		ServerGetter.getServerList();
		System.out.println("Ok");

		System.out.print("Starting UDP server.............");
		IniUDP.iniUDP();
		System.out.println("Ok");
		
		System.out.println("All set and ready to go !");
	
  int port = 5656 ;
  int tailleBuffer = 100 ;
 
  
  
   Thread thread2 = new Thread( 
      new ThreadTcpServer(port,tailleBuffer),"TcpServer"); 
     thread2.start();
        

		int port = 5656 ;
		int tailleBuffer = 100 ;



		Thread thread2 = new Thread( 
				new ThreadTcpServer(port,tailleBuffer),"TcpServer"); 
		thread2.start();


		System.out.println("Serveurs lanc�s") ;
		new Thread(new ThreadClient()).start();

	}
}
