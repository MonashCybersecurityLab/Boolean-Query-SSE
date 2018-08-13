package hxt.entity;

import oxt.entity.OXTTSetBlock;

public class HXTEDB {
	
	public byte[] K_S;
	public byte[] K_X;
	public byte[] K_I;
	public byte[] K_Z;
	public byte[] K_T;
	public byte K_P;
	public OXTTSetBlock[] TSet;
	public byte[] c;
	
	public HXTEDB(int n){
		TSet = new OXTTSetBlock[n];
		for(int i=0; i<n; i++)
			TSet[i] = new OXTTSetBlock();
	}
}
