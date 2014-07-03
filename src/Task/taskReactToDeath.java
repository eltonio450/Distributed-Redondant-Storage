package Task;

public class taskReactToDeath implements Runnable {
	String ip;
	int port;
	
	public taskReactToDeath (String ip, int port) {
		this.ip = ip;
		this.port = port;
	}
	
	public void run () {
		Stockage.Donnees.traiteUnMort(new Stockage.Machine(ip, port));
	}
}
