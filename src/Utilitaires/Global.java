package Utilitaires;

/**
 * 
 * @author Simon
 * Chaque appli choisit son port MAIS SERVERPRPORT = CLIENTPRPORT + 1
 */

public class Global {
	// TCP Connections
	public static String EXCHANGE = "exchange" ;
	public static String REPONSE_EXCHANGE = "ok-exchange" ;
	public static String MONITOR = "monitor";
	public static String END_ENVOI = "fin-1er-envoi" ;
	public static int BUFFER_LENGTH = 1000 ;


	// Relations Publiques
	public static String PREFIXE_BONJOUR = "WESH";
	public static String PREFIXE_REPONSE_BONJOUR = "YO";
	public static String SELF_WAKE_UP = "DEBOUT";
	public static long TIMEOUT = 60000;
	public static long SLEEPTIME = 10000;
	public static int NOMBRESOUSPAQUETS = 5;
	public static long MAXIMUM_SIZE = 500000000000L ;  //TODO : set a reasonable size

	// GestionnaireMort
	public static String VERIFY_DEATH = "YOUDEADBRO?";
	public static String NOT_DEAD = "NOPE";
	public static int DEATH_TIMEOUT = 10000;

	//RP de debug
	public static boolean DEBUG = false;

	// CONFIGURATION AU LANCEMENT
	public static int CLIENTPRPORT;
	public static int SERVERPRPORT; // ClientPRPort +1
	public static int TCP_PORT; // ClientPRPort +2
	public static int FIRST_PORT;
	public static String MY_IP;
	public static String FIRST_IP;
	
	
	

	public static String NOM = MY_IP + " " + Integer.valueOf(CLIENTPRPORT).toString();
	
	public static String PATHTOMYDATA = NOM+"/myOwnData/";
	public static String PATHTODATA = NOM+"/data/";
	// Threads Serveurs
	public static RelationsPubliques.ServerPR serverPR;
	public static  RelationsPubliques.ClientPR clientPR;
	public static TCPConnections.ServeurTCP serveurTCP;
}
