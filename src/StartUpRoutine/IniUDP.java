package StartUpRoutine;

import Utilitaires.Global;

public class IniUDP {
	public static void iniUDP () {
		try {
		Global.clientPR = new RelationsPubliques.ClientPR();
		Global.serverPR = new RelationsPubliques.ServerPR();
		} catch (Exception e) {
			System.out.println("Fatal exception in IniUDP");
			e.printStackTrace();
			System.exit(-1);
		}
		
		Global.clientPR.start();
		Global.serverPR.start();
	}
}
