
public class ServerDebug extends Thread {
	
	int identifiantDebug;
	int port;
	String [] args;
	
	
	public ServerDebug(int id, int port)
	{
		args = new String[5];
		args[0]="";
		args[1]="";
		args[2]="-d";
		args[3]=Integer.valueOf(identifiantDebug).toString();
		args[4]="-p";
		args[5]=Integer.valueOf(port).toString();
		
	}

	public void run(String[] args){
		Main.main(args);		
	}
}
