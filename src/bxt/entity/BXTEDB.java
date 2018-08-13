package bxt.entity;

import java.util.Vector;

import sks.entity.TSetBlock;

public class BXTEDB {
	
	public byte[] K_S;
	public byte[] K_X;
	public byte[] K_T;
	public byte K_P;
	public TSetBlock[] TSet;
	public Vector<String> XSet;
	
	public BXTEDB(int n){
		TSet = new TSetBlock[n];
		for(int i=0; i<n; i++)
			TSet[i] = new TSetBlock();
		XSet = new Vector<String>();
	}
}
