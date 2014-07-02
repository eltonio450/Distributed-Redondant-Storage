package Task;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import Stockage.Donnees;
import Stockage.Paquet;
import Utilitaires.Global;
import Utilitaires.Message;
import Utilitaires.Utilitaires;

public class taskSendRequestedPaquet implements Runnable {

  SocketChannel s;

  public taskSendRequestedPaquet(SocketChannel socket) {
    s = socket;
  }

  public void run() {
    try {
      // Etape 1 : définir de quel paquet l'autre a besoin : il faut envoyer OK en premier.
      ByteBuffer b = Utilitaires.stringToBuffer(Message.OK);
      s.write(b);
      Utilitaires.out("SendRequested, j ai envoye OK");
      b = ByteBuffer.allocateDirect(Message.BUFFER_LENGTH);
      b.clear();
      s.read(b);
      b.flip();
      String id = Utilitaires.buffToString(b);

      // Etape 2 : on envoit le paquet
      Paquet p = Donnees.getHostedPaquet(id);
      Donnees.printMyData();
      if (p != null) {
        p.fichier.transferTo(0, Global.PAQUET_SIZE, s);
      } else {
        Utilitaires.out("Erreur, je n'ai pas le paquet !! " + id);
      }
    } catch (IOException e) {
        e.printStackTrace();
    }

  }

}
