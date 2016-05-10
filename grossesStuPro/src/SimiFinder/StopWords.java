package SimiFinder;

import java.io.*;
import java.util.*;

public class StopWords {
	static ArrayList<String> words = new ArrayList<String>();
	public StopWords(){
		try{
			
			String line;
			BufferedReader br = new BufferedReader(new FileReader("StopWordsList.txt"));
			
			while ((line = br.readLine()) != null){
				words.add(line);
			}
			br.close();
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
	}
	
	public static boolean isStopWord(String str){
		if (words.contains(str)){
			return true;
		}
		else{
			return false;	
		}
		
	}
}
