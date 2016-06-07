package SimiFinder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class MapManager {

	private Map<String, Term> globalMap;
	// globalTerms sind alle Terms zusammen in einer Map. Der Key ist dabei der
	// Term

	// localTerms sind alle Terms pro Stream(Journal oder Conference). Der Key
	// ist dabei der Journal/ConferenceName. Die zweite Map enthï¿½lt den Term
	// als
	// Schlï¿½ssel.
	private Map<String, Map<String, LinkedTerm>> localMap;

	/*
	 * Struktur localMap: (Schlï¿½ssel(Journal/ConferenceName)
	 * ,(Schlï¿½ssel(TermName),(Term(lokaler Term),Term(globaler Term)))
	 * ,(Schlï¿½ssel(TermName),(Term(lokaler Term),Term(globaler Term)))
	 * ,(Schlï¿½ssel(TermName),(Term(lokaler Term),Term(globaler Term))) ,...)
	 */

	private Map<String, Author> authorMap;

	private Map<String, String[]> aliasMap;
	
	private Map<String, String> coAuthorMap;
	
	MapManager(Map<String, Term> inputGlobal,
			Map<String, Map<String, LinkedTerm>> inputLocal,
			Map<String, Author> inputAuthor,
			Map<String, String[]> inputAlias,
			Map<String, String> inputCoAuthor) {
		this.globalMap = inputGlobal;
		this.localMap = inputLocal;
		this.authorMap = inputAuthor;
		this.aliasMap = inputAlias;
		this.coAuthorMap = inputCoAuthor;

	}


	void addAuthor(String str, String stream) {
		if (!authorMap.containsKey(str)) {
			authorMap.put(str, new Author(str));
			authorMap.get(str).addStream(stream);
		} else {
			if (authorMap.get(str).streams.containsKey(stream)) {
				authorMap.get(str).streams.get(stream).inc();
			} else {
				authorMap.get(str).addStream(stream);
			}
		}

	}

	void addTerm(String str, String stream) {
		if (!StopWords.isStopWord(str)) {
			if (!globalMap.containsKey(str)) {
				// Methode 1
				createAllNewEntry(str, stream);
			} else {
				globalMap.get(str).counter.inc();
				if (!localMap.containsKey(stream)) {
					// Methode 2
					createNewLocalEntry(str, stream);

				} else {
					if (!localMap.get(stream).containsKey(str)) {
						// Methode 3
						createNewTermEntry(str, stream);

					} else {
						// Term ist global und lokal vorgekommen und der counter
						// wird inc()
						localMap.get(stream).get(str).localTerm.counter.inc();
					}
				}
			}
		}
	}
	
	void addAlias(String str){
		String[] names = str.split(",_,");
		aliasMap.put(names[1], names);		
		
	}
	String findAlias(String name){
		//wenn der Name ein Alias ist, wird der Hauptname ausgegeben.
		for(Map.Entry<String, String[]> entry : aliasMap.entrySet()){
			if(Arrays.asList(entry.getValue()).contains(name)){
				return entry.getKey();
			}
		}
			
		return null;
	}
	void checkAliases(){
		//Aliase werden eleminiert in authorMap, nachdem die Daten im "Hauptartikel" gespeichert werden
		Map<String, Counter> tmpStreams = new HashMap<String, Counter>();
		for (Map.Entry<String, String[]> entry : aliasMap.entrySet()){
			for(int i = 1; i < entry.getValue().length; i++){
				//iterator fängt bei 2 an, da [0] leer und [1] der "Hauptname des Autors ist"
				tmpStreams = authorMap.get(entry.getValue()[i]).streams;
				authorMap.get(entry.getKey()).streams.putAll(tmpStreams);
				//merged die Liste der Streams
				
				for (Author a: authorMap.get(entry.getValue()[i]).coAuthors){
				//fügt die liste der coauthoren der Hauptliste zu
					authorMap.get(entry.getKey()).coAuthors.add(a);
				}				
				authorMap.remove(entry.getValue()[i]);
				
			}
		}
								
	}
	
	void fillAuthorLookup(){
		for (Map.Entry<String, Author> authorEntry : authorMap.entrySet()){
			for (Map.Entry<String, Counter> streamEntry : authorEntry.getValue().streams.entrySet()){
				if (streamEntry.getValue().getVal() > 5){
					
					
				}
			}
		}
	}
	
	void createCoauthors(){
		//nachdem die authorMap bereinigt wurde werden die coauthoren zu authorMap hinzugefugt. Mit Hilfe von checkAliases() werden Aliase direkt ersetzt
		String tmpAuthor = "", tmpCoAuthor = "";
		for (Map.Entry<String, String> entry : coAuthorMap.entrySet()){
			tmpAuthor = entry.getKey();
			String[] coAuthors = entry.getValue().split(",_,");
			tmpAuthor = findAlias(entry.getKey());
			if (tmpAuthor!=null){
				for (int i = 2; i < coAuthors.length; i++){
					tmpCoAuthor = findAlias(coAuthors[i]);
					if (tmpCoAuthor!= null){
						authorMap.get(tmpAuthor).addCoAuthor(authorMap.get(tmpCoAuthor));
					}
				}
			}
		}
	}
	

	

	void filterMap() {
		//schmeisst alle uberflussigen Terme, also die, mit vorkommen 1 aus localMap und globalMap
		System.out.println("Start filtering");
		for (Iterator<Entry<String, Map<String, LinkedTerm>>> it = localMap
				.entrySet().iterator(); it.hasNext();) {
			Map.Entry<String, Map<String, LinkedTerm>> entry = it.next();

			for (Iterator<Map.Entry<String, LinkedTerm>> it2 = entry.getValue()
					.entrySet().iterator(); it2.hasNext();) {
				Entry<String, LinkedTerm> entry2 = it2.next();
				if(entry2.getValue().getGlobalTerm().counter.getVal() == 1 ||entry2.getValue().getGlobalTerm()==null ){					
					globalMap.remove(entry2.getValue().globalTerm.term);
					it2.remove();
				}
			}
		}
		System.out.println("Done filtering");
	}

	// die kommenden Methoden kann man sicher noch zusammenfassen, ich fand es
	// vorerst einfacher fï¿½r den ï¿½berblick


	void createAllNewEntry(String str, String stream) {
		// Methode 1: Wenn der Term noch nie vorgekommen ist wird
		// diese Methode ausgefï¿½hrt
		Map<String, LinkedTerm> tmpMap = new HashMap<String, LinkedTerm>();
		Term glblTerm = new Term(str);

		LinkedTerm tmpLTerm = new LinkedTerm();
		tmpLTerm.setLocalTerm(new Term(str));
		tmpLTerm.setGlobalTerm(glblTerm);
		tmpMap.put(str, tmpLTerm);
		
		
		globalMap.put(str, glblTerm);
		localMap.put(stream, tmpMap);


		
	}


	void createNewLocalEntry(String str, String stream) {
		// Methode 2: Wenn der Term global schon vorgekommen ist, aber der Stream
		// noch nicht
		Map<String, LinkedTerm> tmpMap = new HashMap<String, LinkedTerm>();

		LinkedTerm tmpLTerm = new LinkedTerm();
		tmpLTerm.setLocalTerm(new Term(str));
		tmpLTerm.setGlobalTerm(globalMap.get(str));
		tmpMap.put(str, tmpLTerm);

		
		localMap.put(stream, tmpMap);


	}


	void createNewTermEntry(String str, String stream) {
		// Methode 3: Wenn der Term global schon vorgekommen ist und der Stream
		// bereits existiert, der Term aber noch nicht im Stream eingetragen
		Map<String, LinkedTerm> tmpMap = new HashMap<String, LinkedTerm>();

		LinkedTerm tmpLTerm = new LinkedTerm();
		tmpLTerm.setLocalTerm(new Term(str));
		tmpLTerm.setGlobalTerm(globalMap.get(str));
		tmpMap.put(str, tmpLTerm);

		localMap.get(stream).put(str, tmpLTerm);

	}
	
}
class Node{
	String name;
}
class Edge{
	Node nodeA, nodeB;
	Edge(Node a, Node b){
		this.nodeA = a;
		this.nodeB = b;
	}
}
class AuthorGraph{
	ArrayList<Edge> edges = new ArrayList<Edge>();	
}

class Author {
	// Author enthï¿½lt 2 Listen: -In streams gibt es fï¿½r jedes Journal/Conference
	// einen Eintrag und die Anzahl an Artikeln in diesem Stream, an denen der
	// Author beteiligt war.
	// -coauthorMap entï¿½hlt alle Coauthoren des authorMap, als rekursiv verschachtelte
	// Struktur
	Map<String, Counter> streams = new HashMap<String, Counter>();
	ArrayList<Author> coAuthors = new ArrayList<Author>();

	String name;
	
	Author(String str) {
		this.name = str;
	}

	void addCoAuthor(Author a) {
		this.coAuthors.add(a);
	}

	void addStream(String str) {
		Counter counter = new Counter();
		this.streams.put(str, counter);
	}
}

class LinkedTerm {
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

class Term {
	String term;
	Counter counter;

	Term(String str) {
		counter = new Counter();
		term = str;
	}
	
}

class Counter {
	private int val;

	Counter() {
		val = 1;
	}

	public void inc() {
		val++;
	}

	public int getVal() {
		return val;
	}
}
class vectorspace {
	
	void matching(int counter, String k, Map<String, Map<String, LinkedTerm>> localMap){ 
		//berechnet die Relevanz (wdk) eines Dokumentes fÃ¼r k
		double N = counter;
		double nk= vorkommenGesamt(k,localMap);
		double tf = vorkommenImDokument(k);
		double Nenner=0;		//Summe von i=1 bis t (tf*log(N/ni))^2
		double wdk = (tf*Math.log(N/nk))/(Math.sqrt(Nenner));
}
	
	public int vorkommenGesamt(String k, Map<String, Map<String, LinkedTerm>> localMap){ 
		//gibt die Anzahl der Dokumente wieder, die k enthalten
		int v= 0;		
		for (Iterator<Entry<String, Map<String, LinkedTerm>>> it = localMap
				.entrySet().iterator(); it.hasNext();) {
			Map.Entry<String, Map<String, LinkedTerm>> entry = it.next();
			if (k==entry.getKey())
			{v=v+1;}
				}
		return v;

	}
	
	public int vorkommenImDokument(String k){ 
			//HÃ¤ufigkeit vom Begriff k im Dokument
		
		return 0;
	}
	
	public int vocabular(Map<String, Map<String, LinkedTerm>> localMap, Map<String, Term> globalMap){		
		//gibt die Anzahl der verschiedenen Begriffe aller Dokumente zurÃ¼ck.
		int v=0;
		for (Iterator<Entry<String, Map<String, LinkedTerm>>> it = localMap
				.entrySet().iterator(); it.hasNext();) {
			Map.Entry<String, Map<String, LinkedTerm>> entry = it.next();
			v = v+1;
			for (Iterator<Map.Entry<String, LinkedTerm>> it2 = entry.getValue()
					.entrySet().iterator(); it2.hasNext();) {
				Entry<String, LinkedTerm> entry2 = it2.next();
				if(entry2.getValue().getGlobalTerm() == entry2.getValue().getGlobalTerm() ){					
					globalMap.remove(entry2.getValue().globalTerm.term);
					it2.remove();}
				}}
		return v;
	}
	
}