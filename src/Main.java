


public class Main {

	public static void main(String[] args) {
		//System.out.println("Hello World !");
		String portTexte;

		
		
		Main.main(arguments);
		
		try{
			portTexte = args[0];
		} catch(Exception e) {portTexte = "5040";}
		
		int port = Integer.parseInt(portTexte);
		
		System.out.println("Port de fonctionnement : " + port);
	}

}
