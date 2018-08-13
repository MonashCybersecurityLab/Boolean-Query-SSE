package sks;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;

import sks.entity.SKSEDB;
import sks.entity.KeywordFiles;
import util.AES;
import util.IntAndByte;

public class SKSProtocol {
	public KeywordFiles[] kfs;
	
	public SKSProtocol(){
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
	
	public SKSEDB Setup(){
		SKSEDB sksedb = new SKSEDB(kfs.length);
		
		sksedb.K_P = 3;
		byte[] K_S = {3, 3, 3};
		byte[] K_T = {4, 4, 4};
		sksedb.K_S = K_S;
		sksedb.K_T = K_T;
		
		for(int i=0; i<kfs.length; i++){
			sksedb.TSet[i].keyword_enc = AES.encrypt(kfs[i].keyword.getBytes(), sksedb.K_T);
	
			byte[] K_e = AES.encrypt(kfs[i].keyword.getBytes(), sksedb.K_S);
			
			sksedb.TSet[i].files_enc = new byte[kfs[i].files.length][];
			
			for(int j=0; j<kfs[i].files.length; j++){ //设置简单的permutation，异或某个数
				byte rind[] = this.getRind(IntAndByte.toByteArray(kfs[i].files[j]), sksedb.K_P);
				sksedb.TSet[i].files_enc[j] = AES.encrypt(rind, K_e);
			}
		}
		
		return sksedb;
	}
	
	public byte[][] Search(byte[] stag, SKSEDB sksedb){
		for(int i=0; i<sksedb.TSet.length; i++){
			if(Arrays.equals(stag, sksedb.TSet[i].keyword_enc))
				return sksedb.TSet[i].files_enc;
		}
		return null;
	}
	
	public byte[][] ClientGetRind(byte[][] t, String keyword, byte[] K_S){
		byte[][] rind = new byte[t.length][];
		
		byte[] K_e = AES.encrypt(keyword.getBytes(), K_S);
		
		for(int i=0; i<t.length; i++)
			rind[i] = AES.decrypt(t[i], K_e);
		
		return rind;
	}
	
	public static void main(String args[]){
		SKSProtocol sksp = new SKSProtocol();
		
		SKSEDB sksedb = sksp.Setup();
		
		String keyword = "2";
		System.out.println("Keyword: " + keyword);
		
		byte[] stag = AES.encrypt(keyword.getBytes(), sksedb.K_T);
		System.out.println("Client is generating stag ... \nStag: " + AES.parseByte2HexStr(stag));
		
		System.out.println("Server is searching EDB ...");
		byte[][] t = sksp.Search(stag, sksedb);
		
		System.out.println("Client gets t.");
		if(t == null)
			System.out.println("There is no result matching the keyword.");
		else{
			for(int i=0; i<t.length; i++)
				System.out.println("Rind_enc" + (i+1) + ": " + AES.parseByte2HexStr(t[i]));
			
			System.out.println("Client is decrypting t ...");
			byte[][] rinds = sksp.ClientGetRind(t, keyword, sksedb.K_S);
			for(int i=0; i<rinds.length; i++)
				System.out.println("Rind" + (i+1) + ": " + AES.parseByte2HexStr(rinds[i]));
			
			System.out.println("Client is getting ind");
			for(int i=0; i<rinds.length; i++){
				byte[] ind = sksp.getRind(rinds[i], sksedb.K_P);
				System.out.println("ind" + (i+1) + ": " + AES.parseByte2HexStr(ind));
			}
		}	
	}
}
