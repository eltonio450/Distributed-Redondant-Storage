package Utilitaires;

import java.nio.ByteBuffer;

import Stockage.Machine;

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
	 * Le buffer est flipp√©
	 */
	public static String buffToString(ByteBuffer b) {
		String s = new String ();
		while (b.hasRemaining())
			s += (char) b.get();
		b.flip();
		return s;
	}
	
	public static ByteBuffer createBufferForPaquetInformation(long id, int power, Machine owner) {
	  //create a buffer and flip it at the end
	  
	  ByteBuffer res = ByteBuffer.allocateDirect(Global.BUFFER_LENGTH);
	  //TODO : implement in a smart way : il faut mettre id du paquet, sa power, owner.ipAdresse et owner.port
	  //‡ voir avec Paquet.createPaquetFromBuffer
	  return res ;
	}
	
	
}
