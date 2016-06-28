package SimiFinder;

import java.io.*;
import java.util.*;

public class StopWords {
	private ArrayList<String> words;
	public StopWords(String fileLoc){
		words = new ArrayList<String>();
		try{
			
			String line;
			BufferedReader br = new BufferedReader(new FileReader(fileLoc));
			
			while ((line = br.readLine()) != null){
				if (!line.equals(" "))words.add(line);
			}
			br.close();
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
	}
	
	public boolean isStopWord(String str){
		if (this.words.contains(str)){
			return true;
		}
		else{
			return false;	
		}
		
	}
}