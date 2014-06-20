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
	 * Le buffer est flippé
	 */
	public static String buffToString(ByteBuffer b) {
		String s = new String ();
		while (b.hasRemaining())
			s += (char) b.get();
		b.flip();
		return s;
	}
	
	
}
