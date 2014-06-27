package Utilitaires;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class Utilitaires {
	/**
	 * 
	 * @param s string to convert
	 * @return FLIPPED buffer
	 */
	public static ByteBuffer stringToBuffer(String s) {
		ByteBuffer buff = ByteBuffer.allocateDirect(s.length()*4);
		for (char c : s.toCharArray()) {
			buff.putChar(c);
		}
		buff.flip();
		return buff;
	}

	/**
	 * 
	 * @param b buffer to convert (already flipped !)
	 * @return resulting string
	 * Le buffer est flippé
	 */
	public static String buffToString(ByteBuffer b) {
		String s = new String ();
		while (b.hasRemaining())
			s += (char) b.get();
		b.flip();
		return s;
	}

	public static ByteBuffer stringToBuffer(int id) {
		String s = Integer.valueOf(id).toString();
		return stringToBuffer(s);
	}	

	public static String getAFullMessage (String[] finalWords, SocketChannel s) throws IOException {
		ByteBuffer b = ByteBuffer.allocateDirect(5000);
		String retour = "";
		String m;
		String token;
		boolean continuer = true;

		while (continuer) {
			if (s.read(b) == -1) throw new IOException ();
			b.flip();
			m = buffToString(b);
			retour += m;
			b.clear();
			
			Scanner sc = new Scanner (m);
			while (sc.hasNext() && continuer) {
				token = sc.next();
				for (String w : finalWords) {
					if (token.equals(w)) {
						continuer = false;
						break;
					}
				}
			}
		}
		return retour;
	}
}
