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
  public static int TCP_SERVEUR_PORT = 6060;

  // DeathVerifier
  public static String VERIFY_DEATH = "CAVA?";
  
  // Relations Publiques
  public static String PREFIXE_BONJOUR = "WESH";
  public static String PREFIXE_REPONSE_BONJOUR = "YO";
  public static String SELF_WAKE_UP = "DEBOUT";
  public static int CLIENTPRPORT = 5050;
  public static int SERVERPRPORT = 5051;
  public static long TIMEOUT = 60000;
  public static long SLEEPTIME = 10000;
  public static String FIRSTIP = "";
  public static int NOMBRESOUSPAQUETS = 5;
  
  //RP de debug
  public static boolean DEBUG = false;
}
