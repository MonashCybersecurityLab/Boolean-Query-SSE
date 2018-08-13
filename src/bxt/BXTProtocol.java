package bxt;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Vector;

import bxt.entity.BXTClientToServer;
import bxt.entity.BXTEDB;

import sks.entity.KeywordFiles;
import util.AES;
import util.IntAndByte;

public class BXTProtocol {
	public KeywordFiles[] kfs;
	
	public BXTProtocol(){
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream("KeywordFiles.dat"));
			kfs = (KeywordFiles[]) in.readObject();
			in.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public byte[] getRind(byte ind[], byte K_P){
		byte[] rind = new byte[ind.length];
		
		for(int i=0; i<ind.length; i++)
			rind[i] = (byte)(K_P^ind[i]);
		
		return rind;	//ind xor 3
	}
	
	public BXTEDB Setup(){
		BXTEDB bxtedb = new BXTEDB(kfs.length);
		
		bxtedb.K_P = 3;
		byte[] K_S = {3, 3, 3};
		byte[] K_X = {4, 4, 4};
		byte[] K_T = {5, 5, 5};
		bxtedb.K_S = K_S;
		bxtedb.K_X = K_X;
		bxtedb.K_T = K_T;
		
		for(int i=0; i<kfs.length; i++){
			bxtedb.TSet[i].keyword_enc = AES.encrypt(kfs[i].keyword.getBytes(), bxtedb.K_T);
	
			byte[] K_e = AES.encrypt(kfs[i].keyword.getBytes(), bxtedb.K_S);
			
			byte[] xtrap = AES.encrypt(kfs[i].keyword.getBytes(), bxtedb.K_X);
			
			bxtedb.TSet[i].files_enc = new byte[kfs[i].files.length][];
			
			for(int j=0; j<kfs[i].files.length; j++){ //设置简单的permutation，异或某个数
				byte rind[] = this.getRind(IntAndByte.toByteArray(kfs[i].files[j]), bxtedb.K_P);
				bxtedb.TSet[i].files_enc[j] = AES.encrypt(rind, K_e);
				byte[] xtag = AES.encrypt(rind, xtrap);
				bxtedb.XSet.add(new String(xtag));
			}
		}
		
		return bxtedb;
	}
	
	public BXTClientToServer ClientGen(BXTEDB bxtedb, String[] keywords){
		int n = keywords.length;
		BXTClientToServer bxtcts = new BXTClientToServer(n-1);
		
		bxtcts.K_e = AES.encrypt(keywords[0].getBytes(), bxtedb.K_S);
		bxtcts.stag = AES.encrypt(keywords[0].getBytes(), bxtedb.K_T);
		
		for(int i=1; i<n; i++){
			bxtcts.xtrap[i-1] = AES.encrypt(keywords[i].getBytes(), bxtedb.K_X);
		}
		
		return bxtcts;
	}
	
	public boolean IsInXSet(byte[] xtag, Vector<byte[]> XSet){
		for(int i=0; i<XSet.size(); i++)
			if(Arrays.equals(xtag, XSet.get(i)))
				return true;
		return false;
	}
	
	public Vector<byte[]> Search(BXTClientToServer bxtcts, BXTEDB bxtedb){
		
		for(int i=0; i<bxtedb.TSet.length; i++){
			if(Arrays.equals(bxtcts.stag, bxtedb.TSet[i].keyword_enc)){
				Vector<byte[]> rinds = new Vector<byte[]>();
				
				for(int j=0; j<bxtedb.TSet[i].files_enc.length; j++){
					byte[] rind = AES.decrypt(bxtedb.TSet[i].files_enc[j], bxtcts.K_e);
					
					boolean flag = true;
					for(int k=0; k<bxtcts.xtrap.length; k++)
						flag = flag && bxtedb.XSet.contains(new String(AES.encrypt(rind, bxtcts.xtrap[k])));
						//flag = flag && this.IsInXSet(AES.encrypt(rind, bxtcts.xtrap[k]), bxtedb.XSet);
					if(flag)
						rinds.add(rind);
				}
				
				return rinds;
			}
		}
		return null;
	}
	
	public static void main(String args[]){
		BXTProtocol bxtp = new BXTProtocol();
		
		BXTEDB bxtedb = bxtp.Setup();
		
		String keywords[] = {"10"};
		System.out.print("Keywords: ");
		for(int i=0; i<keywords.length; i++)
			System.out.print(keywords[i] + " ");
		
		System.out.println("\nClient is generating search token ... ");
		BXTClientToServer bxtcts = bxtp.ClientGen(bxtedb, keywords);
		
		System.out.println("Server is searching EDB ...");
		Vector<byte[]> rinds = bxtp.Search(bxtcts, bxtedb);
		
		if(rinds == null){
			System.out.println("There is no result matching the keywords.");
		}
		else{
			System.out.println("Client gets rinds.");
			for(int i=0; i<rinds.size(); i++)
				System.out.println("Rind" + (i+1) + ": " + AES.parseByte2HexStr(rinds.get(i)));
			
			System.out.println("Client is getting ind");
			for(int i=0; i<rinds.size(); i++){
				byte[] ind = bxtp.getRind(rinds.get(i), bxtedb.K_P);
				System.out.println("ind" + (i+1) + ": " + AES.parseByte2HexStr(ind));
			}
		}	
	}
}
