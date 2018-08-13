package hxt;

import hve.HVEIP08;
import hxt.entity.HXTEDB;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;

import bloomfilter.BloomFilter;

import oxt.entity.OXTTSetTuple;

import sks.entity.FileKeywords;
import sks.entity.KeywordFiles;
import util.AES;
import util.Hash;
import util.IntAndByte;

public class HXTProtocol {
	public KeywordFiles[] kfs;
	public Pairing pairing = PairingFactory.getPairing("params/curves/a.properties");
	public Element g = pairing.getG1().newRandomElement().getImmutable();
	public AsymmetricCipherKeyPair keyPair = HVEIP08.setup(512);
	
	public HXTProtocol(){
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream("KeywordFiles.dat"));
			kfs = (KeywordFiles[]) in.readObject();
			in.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public byte[] getRind(byte ind[], byte K_P){
		byte[] rind = new byte[ind.length];
		
		for(int i=0; i<ind.length; i++)
			rind[i] = (byte)(K_P^ind[i]);
		
		return rind;	//ind xor 3
	}
	
	public HXTEDB Setup(){
		HXTEDB hxtedb = new HXTEDB(kfs.length);
		
		hxtedb.K_P = 3;
		byte[] K_S = {3, 3, 3};
		byte[] K_X = {4, 4, 4};
		byte[] K_I = {5, 5, 5};
		byte[] K_Z = {6, 6, 6};
		byte[] K_T = {7, 7, 7};
		
		hxtedb.K_S = K_S;
		hxtedb.K_X = K_X;
		hxtedb.K_I = K_I;
		hxtedb.K_Z = K_Z;
		hxtedb.K_T = K_T;
		
		int[] b = BloomFilter.init_b_0();
		
		for(int i=0; i<kfs.length; i++){
			hxtedb.TSet[i].keyword_enc = AES.encrypt(kfs[i].keyword.getBytes(), hxtedb.K_T);
	
			byte[] K_e = AES.encrypt(kfs[i].keyword.getBytes(), hxtedb.K_S);
			int c = 1;
			hxtedb.TSet[i].t = new OXTTSetTuple[kfs[i].files.length];
			
			for(int j=0; j<kfs[i].files.length; j++){ //设置简单的permutation，异或某个数
				byte rind[] = this.getRind(IntAndByte.toByteArray(kfs[i].files[j]), hxtedb.K_P);
				byte[] xind = AES.encrypt(rind, hxtedb.K_I);
				byte[] z = AES.encrypt(kfs[i].keyword.concat(String.valueOf(c)).getBytes(), hxtedb.K_Z);
				Element y = Hash.HashToZr(pairing, xind).div(Hash.HashToZr(pairing, z)).getImmutable();
				
				hxtedb.TSet[i].t[j] = new OXTTSetTuple();
				hxtedb.TSet[i].t[j].e = AES.encrypt(rind, K_e);
				hxtedb.TSet[i].t[j].y = y.duplicate().getImmutable();
				
				byte[] kxw = AES.encrypt(kfs[i].keyword.getBytes(), hxtedb.K_X);
				Element xtag = g.powZn(Hash.HashToZr(pairing, kxw).mul(Hash.HashToZr(pairing, xind)));
				
				BloomFilter.BF(xtag.toBytes(), b);
			
				c++;
			}
		}
		
		hxtedb.c = HVEIP08.enc(keyPair.getPublic(), b);
		
		return hxtedb;
	}
	
	public byte[] ClientGenStag(String w1, byte[] K_T){
		return AES.encrypt(w1.getBytes(), K_T);
	}
	
	public OXTTSetTuple[] SearchStag(byte[] stag, HXTEDB hxtedb){
		for(int i=0; i<hxtedb.TSet.length; i++)
			if(Arrays.equals(stag, hxtedb.TSet[i].keyword_enc))
				return hxtedb.TSet[i].t;
		return null;
	}
	
	public Element[][] ClientGenxtoken(String[] keywords, int tsize, HXTEDB hxtedb){
		Element[][] xtoken = new Element[tsize][keywords.length-1]; 
		for(int i=1; i<=tsize; i++){
			byte[] z = AES.encrypt(keywords[0].concat(String.valueOf(i)).getBytes(), hxtedb.K_Z);
			Element e_z = Hash.HashToZr(pairing, z).getImmutable();
			
			for(int j=1; j<keywords.length; j++){
				byte[] kxw = AES.encrypt(keywords[j].getBytes(), hxtedb.K_X);
				Element e_kxw = Hash.HashToZr(pairing, kxw);
				xtoken[i-1][j-1] = g.powZn(e_z.mul(e_kxw)).getImmutable();
			}
		}
		
		return xtoken;
	}
	
	public boolean IsInXSet(Element xtag, Vector<Element> XSet){
		for(int i=0; i<XSet.size(); i++)
			if(xtag.isEqual(XSet.get(i)))
				return true;
		return false;
	}
	
	public Vector<int[]> ServerComputeB(Element[][] xtoken, OXTTSetTuple[] tuple, HXTEDB hxtedb){
		Vector<int[]> bs = new Vector<int[]>();
		
		for(int i=0; i<tuple.length; i++){
			int[] b = BloomFilter.init_b_minues1();
			for(int j=0; j<xtoken[i].length; j++){
				Element xtag = xtoken[i][j].powZn(tuple[i].y).getImmutable();
				BloomFilter.BF(xtag.toBytes(), b);
			}
			
			bs.add(b);
		}
		
		return bs;
	}
	
	public Vector<CipherParameters> ClientGenSK(Vector<int[]> bs, CipherParameters msk){
		Vector<CipherParameters> sks = new Vector<CipherParameters>();
		
		for(int i=0; i<bs.size(); i++){
			CipherParameters sk = HVEIP08.keyGen(msk, bs.get(i));
			sks.add(sk);
		}
		
		return sks;
	}
	
	public Vector<byte[]> Search(Vector<CipherParameters> sks, OXTTSetTuple[] tuple, HXTEDB hxtedb){
		Vector<byte[]> es = new Vector<byte[]>();
		
		for(int i=0; i<sks.size(); i++){
			if(HVEIP08.evaluate(sks.get(i), hxtedb.c))
				es.add(tuple[i].e);
		}
		
		return es;
	}
	
	public byte[][] ClientGetRinds(Vector<byte[]> es, String keyword, byte[] K_S){
		byte[][] rinds = new byte[es.size()][];
		byte[] K_e = AES.encrypt(keyword.getBytes(), K_S);
		for(int i=0; i<es.size(); i++)
			rinds[i] = AES.decrypt(es.get(i), K_e);
		return rinds;
	}
	
	public String[] GenRandomString(int n){
		Set<Integer> keywords_int = new HashSet<Integer>();
		Random random = new Random();
		for(int i=0; i<n; i++){
			int a = random.nextInt(11);
			if(a != 0)
				keywords_int.add(a);
		}
		
		Object[] obj = keywords_int.toArray();
		String[] keywords  = new String[obj.length];
		for(int i=0; i<obj.length; i++)
			keywords[i] = String.valueOf(obj[i]);
		
		return keywords;
	}
	
	public boolean IsInFile(String[] keywords, FileKeywords fks){
		for(int i=0; i<fks.keywords.length; i++)
			for(int j=0; j<keywords.length; j++)
				if(!keywords[j].equals(fks.keywords[i]))
					return false;
		return true;
	}
	
	public static void main(String args[]){
		
		int count = 0;
		FileKeywords[] fks = new FileKeywords[6];
		
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream("FileKeywords.dat"));
			fks = (FileKeywords[]) in.readObject();
			in.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		
		long start, end;
		HXTProtocol hxtp = new HXTProtocol();
		start = System.nanoTime();
		HXTEDB hxtedb = hxtp.Setup();
		end = System.nanoTime();
		System.out.println("HXTEDB generates time " + (end - start));
		
		
		for(int j=0; j<1000; j++){
			System.out.println("Query " + (j+1));
			String keywords[] = hxtp.GenRandomString(10);
		
			//String keywords[] = {"1", "2"};
			System.out.print("Keywords: ");
			for(int i=0; i<keywords.length; i++)
			System.out.print(keywords[i] + " ");
		
			System.out.println("\nClient is generating stag ... ");
			start = System.nanoTime();
			byte[] stag = hxtp.ClientGenStag(keywords[0], hxtedb.K_T);
			end = System.nanoTime();
			System.out.println("Client generates stag time " + (end - start));
		
			System.out.println("Server is searching stag in EDB ...");
			start = System.nanoTime();
			OXTTSetTuple[] tuple = hxtp.SearchStag(stag, hxtedb);
			end = System.nanoTime();
			System.out.println("Server searches stag time " + (end - start));
		
			System.out.println("\nClient is generating xtoken ... ");
			start = System.nanoTime();
			Element[][] xtoken = hxtp.ClientGenxtoken(keywords, tuple.length, hxtedb);
			end = System.nanoTime();
			System.out.println("Client generates xtoken time " + (end - start));
		
			System.out.println("\nServer is generating all vectors (b) ... ");
			start = System.nanoTime();
			Vector<int[]> bs = hxtp.ServerComputeB(xtoken, tuple, hxtedb);
			end = System.nanoTime();
			System.out.println("Server generates all vectors (b) time " + (end - start));
		
			System.out.println("\nClient is generating all sercetkeys ... ");
			start = System.nanoTime();
			Vector<CipherParameters> sks = hxtp.ClientGenSK(bs, hxtp.keyPair.getPrivate());
			end = System.nanoTime();
			System.out.println("Client generates all secretkeys time " + (end - start));
		
			System.out.println("Server is searching c ...");
			start = System.nanoTime();
			Vector<byte[]> es = hxtp.Search(sks, tuple, hxtedb);
			end = System.nanoTime();
			System.out.println("Server decrypts c time " + (end - start));
		
			System.out.println("Client gets rinds.");
			start = System.nanoTime();
			byte[][] rinds = hxtp.ClientGetRinds(es, keywords[0], hxtedb.K_S);
			end = System.nanoTime();
			System.out.println("Client gets rinds time " + (end - start));
			for(int i=0; i<rinds.length; i++)
			System.out.println("Rind" + (i+1) + ": " + AES.parseByte2HexStr(rinds[i]));
		
			System.out.println("Client is getting ind");
			byte[][] inds = new byte[rinds.length][];
			start = System.nanoTime();
			for(int i=0; i<rinds.length; i++){
				inds[i] = hxtp.getRind(rinds[i], hxtedb.K_P);
				System.out.println("ind" + (i+1) + ": " + AES.parseByte2HexStr(inds[i]));
			}
			end = System.nanoTime();
			System.out.println("Client gets inds time " + (end - start));
			
			if(inds.length != 0 ){
			    for(int i=0; i<inds.length; i++)
			    	if(!hxtp.IsInFile(keywords, fks[i]))
			    		count++;
			}
		}
		
		System.out.println("False Count = " + count);
	}
}
