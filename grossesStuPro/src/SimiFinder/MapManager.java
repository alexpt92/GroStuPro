package SimiFinder;

import java.util.*;

public class MapManager {
	//globalTerms sind alle Terms zusammen in einer Map. Der Key ist dabei der Term
	private static Map<String, Term> globalMap = new HashMap<String, Term>();
	
	//localTerms sind alle Terms pro Stream(Journal oder Conference). Der Key ist dabei der Journal/ConferenceName. Die zweite Map enthält den Term als Schlüssel.
	private static Map<String, Map<String, LinkedTerm>> localMap = new HashMap<String, Map<String, LinkedTerm>>();
	
	/* Struktur localMap: 
	 * 	(Schlüssel(Journal/ConferenceName)	,(Schlüssel(TermName),(Term(lokaler Term),Term(globaler Term)))
	 * 										,(Schlüssel(TermName),(Term(lokaler Term),Term(globaler Term)))
	 * 										,(Schlüssel(TermName),(Term(lokaler Term),Term(globaler Term)))
	 * 										,...)
	 * */
	
	static void addTerm(String str, String stream){
		if (!StopWords.isStopWord(str)){
			if(!globalMap.containsKey(str)){
				createAllNewEntry(str, stream);						
			}
			else{
				globalMap.get(str).counter.inc();
				if(!localMap.containsKey(stream)){
					createNewLocalEntry(str, stream);
				}
				else{
					if(!localMap.get(stream).containsKey(str)){
						createNewTermEntry(str, stream);
					}
					else{
						localMap.get(stream).get(str).localTerm.counter.inc();
					}
				}
			}
		}
	}
	//die kommenden Methoden kann man sicher noch zusammenfassen, ich fand es vorerst einfacher für den Überblick
	
	//Wenn der Term noch nie vorgekommen und kein StopWort ist wir diese Methode ausgeführt
	static  void createAllNewEntry (String str, String stream){		
		Map<String, LinkedTerm> tmpMap = new HashMap<String, LinkedTerm>();
		Term glblTerm = new Term(str);

		LinkedTerm tmpLTerm = new LinkedTerm();
		tmpLTerm.setLocalTerm(new Term(str));
		tmpLTerm.setGlobalTerm(glblTerm);
		tmpMap.put(str, tmpLTerm);	
		
		globalMap.put(str, new Term(str));
		localMap.put(stream, tmpMap);
		
		tmpMap.clear();
	}
	//Wenn der Term global schon vorgekommen ist, aber der Stream noch nicht
	static void createNewLocalEntry(String str, String stream){
		Map<String, LinkedTerm> tmpMap = new HashMap<String, LinkedTerm>();

		LinkedTerm tmpLTerm = new LinkedTerm();
		tmpLTerm.setLocalTerm(new Term(str));
		tmpLTerm.setGlobalTerm(globalMap.get(str));
		tmpMap.put(str, tmpLTerm);
		
		localMap.put(stream, tmpMap);
		
		tmpMap.clear();
	}
	//Wenn der Term global schon vorgekommen ist und der Stream bereits existiert, der Term aber noch nicht im Stream eingetragen
	static void createNewTermEntry(String str, String stream){
		Map<String, LinkedTerm> tmpMap = new HashMap<String, LinkedTerm>();
		
		LinkedTerm tmpLTerm = new LinkedTerm();
		tmpLTerm.setLocalTerm(new Term(str));
		tmpLTerm.setGlobalTerm(globalMap.get(str));
		tmpMap.put(str, tmpLTerm);
		
		localMap.get(stream).put(str, tmpLTerm);
		
		tmpMap.clear();
		
	}
	

}
class LinkedTerm{
	Term localTerm;
	Term globalTerm;		
	
	public Term getLocalTerm() {
		return localTerm;
	}
	public void setLocalTerm(Term localTerm) {
		this.localTerm = localTerm;
	}
	public Term getGlobalTerm() {
		return globalTerm;
	}
	public void setGlobalTerm(Term globalTerm) {
		this.globalTerm = globalTerm;
	}
}
class Term{
	String term;
	Counter counter;
	Term(String str){
		counter = new Counter();
		term = str;
	}
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
