import StartUpRoutine.IniDonnees;
import StartUpRoutine.IniServer;
import StartUpRoutine.IniTCP;
import Stockage.Machine;
import Stockage.Paquet;
import Task.taskClientSendOnePaquet;
import Task.taskDumpToMachine;
import Utilitaires.Global;
import Utilitaires.Slaver;
import Utilitaires.Utilitaires;

// NB : - dans Donnees il faut d�commenter la 1ere ligne de chooseMachine
//      - dans taskServeurReceiveOnePaquet il faut changer Donnees.accepte(s) en true

public class test1 {

    public static void main(String[] args)  {
      
      Thread machine1 = new Thread(new ThreadTcpServer(5004,200)) ;
          
      Thread machine2 = new Thread(){
        public void run() {
          String[] args = new String[1] ;
          args[0] = "-p5000" ;
          
          Utilitaires.out("Modal launched !");
          Utilitaires.out("Parsing arguments...............");
          IniServer.iniServer(args);
          Utilitaires.out("Arguments parsed...");
          Utilitaires.out("Ok");

          Utilitaires.out("Initializing data...............");
          IniDonnees.iniDonnees();
          Utilitaires.out("Data initialized...");
          Utilitaires.out("Ok");
          
          Utilitaires.out("Enslaving innocent threads......");
          Slaver.initialize();
          Utilitaires.out("Ok");
          Utilitaires.out("All set and ready to go !");

          Utilitaires.out("Starting TCP Server.............");
          IniTCP.iniTCP();
          Utilitaires.out("Ok");
          
          try {
            Thread.sleep(500);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          Paquet aEnvoyer = new Paquet(1,Global.MYSELF) ;
          aEnvoyer.changeOwner(new Machine("localhost",5656));
          for(int i = 0 ; i< 5 ; i++) {
            aEnvoyer.otherHosts.add(i,Global.MYSELF);
          }
          Runnable task = new taskClientSendOnePaquet(aEnvoyer) ;
          Utilitaires.out("machine2 lance la tache") ;
          task.run();
          
          Paquet aEnvoyer2 = new Paquet(3,Global.MYSELF) ;
          aEnvoyer2.changeOwner(new Machine("localhost",5858));
          for(int i = 0 ; i< 5 ; i++) {
            aEnvoyer2.otherHosts.add(i,Global.MYSELF);
          }
          try {
            Thread.sleep(500);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          Runnable task2 = new taskDumpToMachine() ;
          task2.run();
          
        }
        } ;
        
        machine1.start();
        machine2.start();
      
    }
}
