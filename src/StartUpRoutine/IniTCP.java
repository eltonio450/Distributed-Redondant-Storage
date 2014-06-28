package StartUpRoutine;

import Utilitaires.Global;

public class IniTCP {
	public static void iniTCP () {
		Global.serveurTCP.
		Global.serveurTCP = new TCPConnections.ServeurTCP();
		Global.serveurTCP.start();
		
		Global.GPRA = new TCPConnections.GeneralPurposeRequestAnalyzer();
		Global.GPRA.start();
	}
}
