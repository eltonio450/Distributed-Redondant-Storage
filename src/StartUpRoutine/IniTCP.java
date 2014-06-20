package StartUpRoutine;

import Utilitaires.Global;

public class IniTCP {
	public static void iniTCP () {
		Global.serveurTCP = new TCPConnections.ServeurTCP();
		Global.serveurTCP.start();
	}
}
