package sks.entity;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class KeywordFilesGen {
	
	public KeywordFiles[] Gen(FileKeywords[] fks){
		Map<String, Vector<Integer>> keywordfile = new HashMap<String, Vector<Integer>>();
		
		for(int i=0; i<fks.length; i++){
			for(int j=0; j<fks[i].keywords.length; j++){
				Vector<Integer> vc = keywordfile.get(fks[i].keywords[j]);
				if( vc == null){
					vc = new Vector<Integer>();
					vc.add(fks[i].index);
					keywordfile.put(fks[i].keywords[j], vc);
				}
				else{
					vc.add(fks[i].index);
					keywordfile.put(fks[i].keywords[j], vc);
				}
			}		
		}
		
		Set<String> keywordsSet = keywordfile.keySet();
		Object[] keywords = keywordsSet.toArray();
		KeywordFiles[] kfs = new KeywordFiles[keywords.length];
		for(int i=0; i<keywords.length; i++){
			kfs[i] = new KeywordFiles();
			kfs[i].keyword = (String) keywords[i];
			Vector<Integer> files = keywordfile.get(keywords[i]);
			kfs[i].files = new int[files.size()];
			for(int j=0; j<files.size(); j++)
				kfs[i].files[j] = files.get(j);
		}
		
		return kfs;
	}
	
	public static void main(String[] args){
		
		try {
			
			ObjectInputStream in = new ObjectInputStream(new FileInputStream("FileKeywords.dat"));
			FileKeywords[] fks = (FileKeywords[]) in.readObject();
			in.close();
			
			KeywordFiles[] kfs = new KeywordFilesGen().Gen(fks);
			
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("KeywordFiles.dat"));
			out.writeObject(kfs);
			out.close();
			
			for(int i=0; i<kfs.length; i++){
				System.out.print("\nKeyword: " + kfs[i].keyword + " files: ");
				for(int j=0; j<kfs[i].files.length; j++)
					System.out.print(kfs[i].files[j] + " ");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
