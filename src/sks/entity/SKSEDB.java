package sks.entity;

public class SKSEDB {
	
	public byte[] K_S;
	public byte K_P;
	public byte[] K_T;
	public TSetBlock[] TSet;
	
	public SKSEDB(int n){
		TSet = new TSetBlock[n];
		for(int i=0; i<n; i++)
			TSet[i] = new TSetBlock();
	}
}
