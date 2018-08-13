package oxt;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Vector;

import oxt.entity.OXTEDB;
import oxt.entity.OXTTSetTuple;

import sks.entity.KeywordFiles;
import util.AES;
import util.Hash;
import util.IntAndByte;

public class OXTProtocol {
	public KeywordFiles[] kfs;
	public Pairing pairing = PairingFactory.getPairing("params/curves/a.properties");
	public Element g = pairing.getG1().newRandomElement().getImmutable();
	
	public OXTProtocol(){
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
	
	public OXTEDB Setup(){
		OXTEDB oxtedb = new OXTEDB(kfs.length);
		
		oxtedb.K_P = 3;
		byte[] K_S = {3, 3, 3};
		byte[] K_X = {4, 4, 4};
		byte[] K_I = {5, 5, 5};
		byte[] K_Z = {6, 6, 6};
		byte[] K_T = {7, 7, 7};
		
		oxtedb.K_S = K_S;
		oxtedb.K_X = K_X;
		oxtedb.K_I = K_I;
		oxtedb.K_Z = K_Z;
		oxtedb.K_T = K_T;
		
		for(int i=0; i<kfs.length; i++){
			oxtedb.TSet[i].keyword_enc = AES.encrypt(kfs[i].keyword.getBytes(), oxtedb.K_T);
	
			byte[] K_e = AES.encrypt(kfs[i].keyword.getBytes(), oxtedb.K_S);
			int c = 1;
			oxtedb.TSet[i].t = new OXTTSetTuple[kfs[i].files.length];
			
			for(int j=0; j<kfs[i].files.length; j++){ //设置简单的permutation，异或某个数
				byte rind[] = this.getRind(IntAndByte.toByteArray(kfs[i].files[j]), oxtedb.K_P);
				byte[] xind = AES.encrypt(rind, oxtedb.K_I);
				byte[] z = AES.encrypt(kfs[i].keyword.concat(String.valueOf(c)).getBytes(), oxtedb.K_Z);
				Element y = Hash.HashToZr(pairing, xind).div(Hash.HashToZr(pairing, z)).getImmutable();
				
				oxtedb.TSet[i].t[j] = new OXTTSetTuple();
				oxtedb.TSet[i].t[j].e = AES.encrypt(rind, K_e);
				oxtedb.TSet[i].t[j].y = y.duplicate().getImmutable();
				
				byte[] kxw = AES.encrypt(kfs[i].keyword.getBytes(), oxtedb.K_X);
				Element xtag = g.powZn(Hash.HashToZr(pairing, kxw).mul(Hash.HashToZr(pairing, xind)));
				oxtedb.XSet.add(xtag.toString());
			
				c++;
			}
		}
		
		return oxtedb;
	}
	
	public byte[] ClientGenStag(String w1, byte[] K_T){
		return AES.encrypt(w1.getBytes(), K_T);
	}
	
	public OXTTSetTuple[] SearchStag(byte[] stag, OXTEDB oxtedb){
		for(int i=0; i<oxtedb.TSet.length; i++)
			if(Arrays.equals(stag, oxtedb.TSet[i].keyword_enc))
				return oxtedb.TSet[i].t;
		return null;
	}
	
	public Element[][] ClientGenxtoken(String[] keywords, int tsize, OXTEDB oxtedb){
		Element[][] xtoken = new Element[tsize][keywords.length-1]; 
		for(int i=1; i<=tsize; i++){
			byte[] z = AES.encrypt(keywords[0].concat(String.valueOf(i)).getBytes(), oxtedb.K_Z);
			Element e_z = Hash.HashToZr(pairing, z).getImmutable();
			
			for(int j=1; j<keywords.length; j++){
				byte[] kxw = AES.encrypt(keywords[j].getBytes(), oxtedb.K_X);
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
	
	public Vector<byte[]> Search(Element[][] xtoken, OXTTSetTuple[] tuple, OXTEDB oxtedb){
		Vector<byte[]> es = new Vector<byte[]>();
		
		for(int i=0; i<tuple.length; i++){
			boolean flag = true;
			for(int j=0; j<xtoken[i].length; j++){
				Element xtag = xtoken[i][j].powZn(tuple[i].y).getImmutable();
				//flag = flag && this.IsInXSet(xtag, oxtedb.XSet);
				flag = flag && oxtedb.XSet.contains(xtag.toString());
			}
			if(flag)
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
	
	public static void main(String args[]){
		long start, end;
		
		OXTProtocol oxtp = new OXTProtocol();
		start = System.nanoTime();
		OXTEDB oxtedb = oxtp.Setup();
		end = System.nanoTime();
		System.out.println("OXTEDB generate time " + (end - start));
		
		String keywords[] = {"1", "2", "3"};
		System.out.print("Keywords: ");
		for(int i=0; i<keywords.length; i++)
			System.out.print(keywords[i] + " ");
		
		System.out.println("\nClient is generating stag ... ");
		start = System.nanoTime();
		byte[] stag = oxtp.ClientGenStag(keywords[0], oxtedb.K_T);
		end = System.nanoTime();
		System.out.println("Stag generate time " + (end - start));
		
		System.out.println("Server is searching stag in EDB ...");
		start = System.nanoTime();
		OXTTSetTuple[] tuple = oxtp.SearchStag(stag, oxtedb);
		end = System.nanoTime();
		System.out.println("Stag search time " + (end - start));
		
		System.out.println("\nClient is generating xtoken ... ");
		start = System.nanoTime();
		Element[][] xtoken = oxtp.ClientGenxtoken(keywords, tuple.length, oxtedb);
		end = System.nanoTime();
		System.out.println("xtoken generate time " + (end - start));
		
		System.out.println("Server is searching XSet ...");
		start = System.nanoTime();
		Vector<byte[]> es = oxtp.Search(xtoken, tuple, oxtedb);
		end = System.nanoTime();
		System.out.println("XSet search time " + (end - start));
		
		System.out.println("Client gets rinds.");
		start = System.nanoTime();
		byte[][] rinds = oxtp.ClientGetRinds(es, keywords[0], oxtedb.K_S);
		end = System.nanoTime();
		System.out.println("Client gets rinds time " + (end - start));
		for(int i=0; i<rinds.length; i++)
			System.out.println("Rind" + (i+1) + ": " + AES.parseByte2HexStr(rinds[i]));
		
		System.out.println("Client is getting ind");
		start = System.nanoTime();
		for(int i=0; i<rinds.length; i++){
			byte[] ind = oxtp.getRind(rinds[i], oxtedb.K_P);
			System.out.println("ind" + (i+1) + ": " + AES.parseByte2HexStr(ind));
		}
		end = System.nanoTime();
		System.out.println("Client gets ind time " + (end - start));	
	}
}
