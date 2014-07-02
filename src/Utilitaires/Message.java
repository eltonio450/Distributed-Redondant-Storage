package Utilitaires;

import java.net.InetSocketAddress;

public class Message {
	
    // TCP Connections
	  public static String HOST_CHANGED = "host_changed#" ;
		public static String EXCHANGE = "EXCHANGE#" ;
		public static String REPONSE_EXCHANGE = "OK_EXCHANGE#" ;
		public static String FAIL ="FAIL#";
		public static String MONITOR = "MONITOR#";
		public static String END_ENVOI = "FIN#" ;
		public static String NEXT_BUFFER = "NEXT#";
		public static String BEGIN = "BEGIN#";
		public static String GET_LIST = "GET_LIST#";
		public static String NEW_SERVER = "NEW_SERVER#";
		public static String ASK_FOR_LOCK = "ASK_FOR_LOCK#";
		public static String ASK_FOR_UNLOCK = "ASK_FOR_UNLOCK#";
		public static int BUFFER_LENGTH = 1000 ;
		public static final String DEMANDE_ID = "DEMANDE_ID#";
		public static final String DEMANDE_PAQUET = "DEMANDE_PAQUET#";
    public static final String AskForPaquet = "ASK_FOR_PAQUET";
    public static final String GiveMeMyPaquet = "GIVE_MY_PAQUET";
		public static String ANNULE_ENVOI = "STOP#" ;
		public static String DO_NOT_ACCEPT = "NON#" ;
		public static String OK = "OK#" ;
		public static int TIME_TO_SLEEP_1 = 500 ; //TODO : make a reasonable choice
		public static String SendOne = "SENDONE#" ;
		public static String SELF_WAKE_UP = "DEBOUT#";
		//PR
		public static String PREFIXE_BONJOUR = "PING#";
		public static String PREFIXE_REPONSE_BONJOUR = "RPING#";
		public static String VERIFY_DEATH = "VERIFY_DEATH?#";
		public static String NOT_DEAD = "IMNOTDEAD!#";
		public static String IS_DEAD = "DEATH#";
		
		public String body;
		public InetSocketAddress dest;

		public Message (String body, InetSocketAddress dest) {
			this.body = body;
			this.dest = dest;
		}
}
