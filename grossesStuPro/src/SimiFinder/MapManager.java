package SimiFinder;

import java.util.*;

public class MapManager {
	private static Map<String, Counter> terms = new HashMap<String, Counter>();
	private static Map<String, Counter> journals = new HashMap<String, Counter>();
	private static Map<String, Counter> conferences = new HashMap<String, Counter>();
}

class Counter{
	int val;
	Counter(){
		val = 1;
	}
	public void inc(){
		val++;
	}
	public int getVal(){
		return val;
	}
}
