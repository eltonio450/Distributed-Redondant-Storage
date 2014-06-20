
public class ServerDebug extends Thread {
	
	int identifiantDebug;
	int port;
	String [] args;
	
	
	public ServerDebug(int id, int port)
	{
		args = new String[6];
		args[0]="";
		args[1]="";
		args[2]="-d";
		args[3]=Integer.valueOf(identifiantDebug).toString();
		args[4]="-p";
		args[5]=Integer.valueOf(port).toString();
		
	}

	public void run(){
		System.out.println("Lancement du serveur " + identifiantDebug);
		Main.main(args);		
	}
}
