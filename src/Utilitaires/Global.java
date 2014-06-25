package Utilitaires;

import Stockage.Machine;

/**
 * 
 * @author Simon
 * Chaque appli choisit son port MAIS SERVERPRPORT = CLIENTPRPORT + 1
 */

public class Global {
	// TCP Connections
  public static String HOST_CHANGED = "host-changed" ;
	public static String EXCHANGE = "exchange" ;
	public static String REPONSE_EXCHANGE = "ok-exchange" ;
	public static String MONITOR = "monitor";
	public static String END_ENVOI = "fin-1er-envoi" ;
	public static int BUFFER_LENGTH = 1000 ;
	public static String NEXT_BUFFER = "NEXT";
	public static String BEGIN = "BEGIN";
	public static String GET_LIST = "GET_LIST";


	// Relations Publiques
	public static String PREFIXE_BONJOUR = "WESH";
	public static String PREFIXE_REPONSE_BONJOUR = "YO";
	public static String SELF_WAKE_UP = "DEBOUT";
	public static long TIMEOUT = 60000;
	public static long SLEEPTIME = 10000;
	public static int NOMBRESOUSPAQUETS = 5;
	public static int NOMBRESOUSPAQUETSSIGNIFICATIFS = 4;
		public static long MAXIMUM_SIZE = 500000000000L ;  //TODO : set a reasonable size
	public static long PAQUET_SIZE = 100 ;  //TODO : set a reasonable size

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
	//public static String MY_IP = "127.0.0.1";
	public static Machine MYSELF;
	
	
	
	
	public static String FIRST_IP;
	
	
	
	//NOM est un identifiant unique lorsque le serveur est executé en mode debug (donc avec plusieurs serveurs sur le meme PC)
	public static int NOM;
	
	
	//pathToMyData dépend du mode : si le mode debug est activé, un dossier prefixe avec l'id du programme est créé. Sinon elle est juste dans myOwnData/ 
	public static String PATHTOMYDATA;
	public static String PATHTODATA;
	// Threads Serveurs
	public static RelationsPubliques.ServerPR serverPR;
	public static  RelationsPubliques.ClientPR clientPR;
	public static TCPConnections.ServeurTCP serveurTCP;
}
