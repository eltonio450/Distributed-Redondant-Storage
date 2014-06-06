package RelationsPubliques;

/**
 * 
 * @author Simon
 * Chaque appli choisit son port MAIS SERVERPRPORT = CLIENTPRPORT + 1
 */
public class Global {
	public static String PREFIXE_BONJOUR = "WESH";
	public static String PREFIXE_REPONSE_BONJOUR = "YO";
	public static int CLIENTPRPORT = 5050;
	public static int SERVERPRPORT = 5051;
	public static long TIMEOUT = 60000;
	public static long SLEEPTIME = 10000;
}
