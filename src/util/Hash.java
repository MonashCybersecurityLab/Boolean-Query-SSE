package util;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;

public class Hash {
	public static Element HashToZr(Pairing pairing, byte[] m){
		Element result = pairing.getZr().newElementFromHash(m, 0, m.length);
		return result;
	}
	
	public static Element HashToG1(Pairing pairing, byte[] m){
		Element result = pairing.getG1().newElementFromHash(m, 0, m.length);
		return result;
	}
}
