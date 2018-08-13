package bloomfilter;

import hve.HVEIP08;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;

import util.AES;

public class BloomFilter {
	
	public static byte[] F(int k, byte[] message){
		
		byte[] a = new byte[1];
		a[0] = (byte)k;
		return AES.encrypt(message, a);
	}
	
	public static int[] init_b_0(int m){
		int b[] = new int[m];
		
		for(int i=0; i<m; i++)
			b[i] = 0;
		
		return b;
	}
	
	public static int[] init_b_minues1(int m){
		int b[] = new int[m];
		
		for(int i=0; i<m; i++)
			b[i] = -1;
		
		return b;
	}
	
	public static int[] init_b_0(){
		int m = 512;		//2^9
		return init_b_0(m);
	}
	
	public static int[] init_b_minues1(){
		int m = 512;		//2^9
		return init_b_minues1(m);
	}
	
	public static int[] BF(int k, byte[] message, int[] b){
		
		for(int i=0; i<k; i++){
			byte[] a = F(i+1, message);
			
			int b_index = a[0]&0x0FF;
			b_index = b_index + (a[1]&0x01)*256;
			
			b[b_index] = 1;
			//b[a[0]&0x3F] = 1;
		}
		
		return b;
	}
	
	public static int[] BF(byte[] message, int[] b){
		
		int k = 14;
		
		return BF(k, message, b);
	}
	
	public static void main(String[] args){
		int b[] = init_b_0();
		
		long start, end;
		
		start = System.nanoTime();
		AsymmetricCipherKeyPair keyPair = HVEIP08.setup(64);
		end = System.nanoTime();
		System.out.println("HVE Setup time = " + (end -start)/1000000000);
		
		start = System.nanoTime();
		CipherParameters sk = HVEIP08.keyGen(keyPair.getPrivate(), b);
		end = System.nanoTime();
		System.out.println("HVE KeyGen time = " + (end -start)/1000000000);
		
		start = System.nanoTime();
		byte[] c = HVEIP08.enc(keyPair.getPublic(), b);
		end = System.nanoTime();
		System.out.println("HVE enc time = " + (end -start)/1000000000);
		
		start = System.nanoTime();
		boolean result = HVEIP08.evaluate(sk, c);
		end = System.nanoTime();
		System.out.println("HVE dec time = " + (end -start)/1000000000);
		
		System.out.println("HVE result is " + result);
		
//		start = System.nanoTime();
//		System.out.println(HVEIP08.evaluate(
//				HVEIP08.keyGen(keyPair.getPrivate(), b),
//				HVEIP08.enc(keyPair.getPublic(), b)));
//		end = System.nanoTime();
//		
//		System.out.println(end-start);
	}
}
