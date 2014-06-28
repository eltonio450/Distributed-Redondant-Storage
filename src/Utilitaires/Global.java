package Utilitaires;

import Stockage.Machine;

/**
 * 
 * @author Simon
 * Chaque appli choisit son port MAIS SERVERPRPORT = CLIENTPRPORT + 1 = TCPPORT + 2
 */

public class Global {
	
	//TCP
  public static int BUFFER_LENGTH = 1000 ;
	
	// Relations Publiques
	
	public static long TIMEOUT = 6000;
	public static int SLEEPTIME = 4000;
	public static int NOMBRESOUSPAQUETS = 5;
	public static int NOMBRESOUSPAQUETSSIGNIFICATIFS = 4;
	public static long MAXIMUM_SIZE = 500000000000L ;  //TODO : set a reasonable size
	public static long PAQUET_SIZE = 100 ;  //TODO : set a reasonable size

	// Dumping paquets in toSendASAP
	public static final long TIME_TO_SLEEP_Dumping = 2000; //TODO 

	// GestionnaireMort
	public static int DEATH_TIMEOUT = 10000;
	public static long SOCKET_TIMEOUT = 30000;

	//RP de debug
	public static boolean DEBUG = false;

	// CONFIGURATION AU LANCEMENT
	public static int CLIENTPRPORT = 5001;// TCP + 1
	public static int SERVERPRPORT = 5002; // TCP +2
	public static int TCP_PORT = 5000; 
	public static int FIRST_PORT = 5000;
	public static String NO_FIRST_SERVER = "none#";
	public static Machine MYSELF;
	
	
	
	
	public static String FIRST_IP = "127.0.0.1";
	
	
	
	//NOM est un identifiant unique lorsque le serveur est executé en mode debug (donc avec plusieurs serveurs sur le meme PC)
	public static int NOM;
	
	
	//pathToMyData dépend du mode : si le mode debug est activé, un dossier prefixe avec l'id du programme est créé. Sinon elle est juste dans myOwnData/ 
	public static String PATHTOMYDATA;
	public static String PATHTODATA;
	// Threads Serveurs
	public static RelationsPubliques.ServerPR serverPR;
	public static  RelationsPubliques.ClientPR clientPR;
	public static TCPConnections.ServeurTCP serveurTCP;
	public static TCPConnections.GeneralPurposeRequestAnalyzer GPRA;
	
	public static void debug(int i)
	{
		Utilitaires.out("Débuggage "+i);
	}
}
