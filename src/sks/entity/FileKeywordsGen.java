package sks.entity;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class FileKeywordsGen {
	
	public FileKeywords Gen(int index, String[] keywords){
		FileKeywords fk = new FileKeywords();
		fk.index = index;
		fk.keywords = keywords;
		
		return fk;
	}
	
	public static void main(String args[]){
		FileKeywordsGen fkg = new FileKeywordsGen();
		
		FileKeywords[] fks = new FileKeywords[7];
		
		String[] str1= {"1", "2", "6", "7", "8", "9"};
		String[] str2 = {"2", "3", "4", "5"};
		String[] str3 = {"4", "5", "6", "7"};
		String[] str4 = {"1", "2", "3", "4", "5", "6"};
		String[] str5 = {"1", "3", "6", "9"};
		String[] str6 = {"2", "3", "7", "9", "10"};
		String[] str7 = {"1", "4", "7", "8", "9", "10"};
		
		fks[0] = fkg.Gen(1, str1);
		fks[1] = fkg.Gen(2, str2);
		fks[2] = fkg.Gen(3, str3);
		fks[3] = fkg.Gen(4, str4);
		fks[4] = fkg.Gen(5, str5);
		fks[5] = fkg.Gen(6, str6);
		fks[6] = fkg.Gen(7, str7);
		
		try {
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("FileKeywords.dat"));
			out.writeObject(fks);
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
