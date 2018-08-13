package bxt.entity;

public class BXTClientToServer {
	public byte[] stag;
	public byte[] K_e;
	public byte[][] xtrap;
	
	public BXTClientToServer(int n){
		xtrap = new byte[n][];
	}
}
