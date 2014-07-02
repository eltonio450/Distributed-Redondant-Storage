package Task;

import Utilitaires.Utilitaires;

public class taskReactToDeath implements Runnable {
	String ip;
	int port;
	
	public taskReactToDeath (String ip, int port) {
		this.ip = ip;
		this.port = port;
	}
	
	public void run () {
		Utilitaires.out("Badoum taskReactToDeath start", 6, true);
		Utilitaires.out("Badoum " + ip + ":" + port);
		Stockage.Donnees.traiteUnMort(new Stockage.Machine(ip, port));
		Utilitaires.out("Badoum taskReactToDeath done", 6, true);
	}
}
