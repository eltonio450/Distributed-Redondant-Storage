



public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String cheminVersDossier;
		int nombreDeDebug = 3;
		
		ServerDebug [] listeServeur = new ServerDebug [nombreDeDebug]; 
		
		
		for(int i = 0;i<nombreDeDebug;i++)
			listeServeur[i].start();
		
		
	}

}
