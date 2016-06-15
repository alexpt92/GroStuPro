package SimiFinder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

public class MapManager {
	int authorCounter = 0, termCounter = 0;
	private Map<String, Term> globalMap;
	// globalTerms sind alle Terms zusammen in einer Map. Der Key ist dabei der
	// Term

	// localTerms sind alle Terms pro Stream(Journal oder Conference). Der Key
	// ist dabei der Journal/ConferenceName. Die zweite Map enth�lt den Term
	// als
	// Schl�ssel.
	private Map<String, Map<String, LinkedTerm>> localMap;

	/*
	 * Struktur localMap: (Schl�ssel(Journal/ConferenceName)
	 * ,(Schl�ssel(TermName),(Term(lokaler Term),Term(globaler Term)))
	 * ,(Schl�ssel(TermName),(Term(lokaler Term),Term(globaler Term)))
	 * ,(Schl�ssel(TermName),(Term(lokaler Term),Term(globaler Term))) ,...)
	 */

	private Map<String, Author> authorMap;

	private Map<String, String[]> aliasMap;

	private Map<String, String> coAuthorMap;
	
	private AuthorGraph authorGraph;
	
	MapManager(Map<String, Term> inputGlobal,
			Map<String, Map<String, LinkedTerm>> inputLocal,
			Map<String, Author> inputAuthor, Map<String, String[]> inputAlias,
			Map<String, String> inputCoAuthor) {
		this.globalMap = inputGlobal;
		this.localMap = inputLocal;
		this.authorMap = inputAuthor;
		this.aliasMap = inputAlias;
		this.coAuthorMap = inputCoAuthor;
		this.authorGraph = new AuthorGraph();

	}

	void addAuthor(String str, String stream, boolean isCoAuthor, String mainAuthorName) {
		if(authorMap.containsKey(str)){
				authorMap.get(str).addStream(stream, isCoAuthor);
				if(isCoAuthor){
					authorMap.get(mainAuthorName).coAuthors.add(authorMap.get(str));
				}
		}
		else{
			authorMap.put(str, new Author(str));
			authorMap.get(str).addStream(stream, isCoAuthor);
			if(isCoAuthor){
				authorMap.get(mainAuthorName).coAuthors.add(authorMap.get(str));
			}
		}
	}

	void addTerm(String str, String stream) {
		// fuellt local und globalMap
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
	void createAllNewEntry(String str, String stream) {
		// Methode 1: Wenn der Term noch nie vorgekommen ist wird
		// diese Methode ausgef�hrt
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
		// Methode 2: Wenn der Term global schon vorgekommen ist, aber der
		// Stream
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

	void addAlias(String str) {
		String[] names = str.split(",_,");
		aliasMap.put(names[1], names);
	}

	String findAlias(String name) {
		// wenn der Name ein Alias ist, wird der Hauptname ausgegeben.
		for (Map.Entry<String, String[]> entry : aliasMap.entrySet()) {
			if (Arrays.asList(entry.getValue()).contains(name)) {
				return entry.getKey();
			}
		}

		return null;
	}

	


	void createCoauthors() {
		// nachdem die authorMap bereinigt wurde werden die coauthoren zu
		// authorMap hinzugefugt. Mit Hilfe von checkAliases() werden Aliase
		// direkt ersetzt
	}

	void filterMap() {
		// schmeisst alle uberflussigen Terme, also die, mit vorkommen 1 aus
		// localMap und globalMap
		System.out.println("Start filtering");
		for (Iterator<Entry<String, Map<String, LinkedTerm>>> it = localMap
				.entrySet().iterator(); it.hasNext();) {
			Map.Entry<String, Map<String, LinkedTerm>> entry = it.next();

			for (Iterator<Map.Entry<String, LinkedTerm>> it2 = entry.getValue()
					.entrySet().iterator(); it2.hasNext();) {
				Entry<String, LinkedTerm> entry2 = it2.next();
				if (entry2.getValue().getGlobalTerm().counter.getVal() == 1
						|| entry2.getValue().getGlobalTerm() == null) {
					globalMap.remove(entry2.getValue().globalTerm.term);
					it2.remove();
				}
			}
		}
		System.out.println("Done filtering");
	}

	// die kommenden Methoden kann man sicher noch zusammenfassen, ich fand es
	// vorerst einfacher f�r den �berblick



}

class Author {
	// Author enth�lt 2 Listen: -In streams gibt es f�r jedes
	// Journal/Conference
	// einen Eintrag und die Anzahl an Artikeln in diesem Stream, an denen der
	// Author beteiligt war.
	// -coauthorMap ent�hlt alle Coauthoren des authors, als rekursiv
	// verschachtelte
	// Struktur
	ArrayList<StreamWithCounter> streamsAsAuthor;
	ArrayList<StreamWithCounter> streamsAsCoAuthor;
	ArrayList<Author> coAuthors;
	String name;
	Author(String str) {
		this.streamsAsAuthor = new ArrayList<StreamWithCounter>();
		this.streamsAsCoAuthor = new ArrayList<StreamWithCounter>();
		this.coAuthors = new ArrayList<Author>();
		this.name = str;
	}

	void addStream(String str, boolean isCoAuthor) {
		boolean found = false;
		if(isCoAuthor){
			for(StreamWithCounter s:streamsAsCoAuthor){
				if(s.streamName.equals(str)){
					s.counter.inc();
					found = true;
					break;
				}
			}
			if(!found){
				streamsAsCoAuthor.add(new StreamWithCounter(str));
			}
		}
		else{
			for(StreamWithCounter s:streamsAsAuthor){
				if(s.streamName.equals(str)){
					s.counter.inc();
					found = true;
					break;
				}
			}
			if(!found){
				streamsAsAuthor.add(new StreamWithCounter(str));
			}
		}

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

class StreamWithCounter{
	Counter counter;
	String streamName;
	StreamWithCounter(String s){
		this.counter = new Counter();
		this.streamName = s;
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
	List<String> Vokabular;
	Vector DocVector;

	public Vector getVector(int counter, Map<String, Term> globalMap,
			Map<String, Map<String, LinkedTerm>> localMap, String stream) {
		int N = counter; // Anzahl der Dokumente
		Vector DocVector = new Vector(); // Dokumentenvektor
		int t = vocabular(localMap, globalMap); // Anzahl der verschiedenen
												// Begriffe aller Dokumente
		for (int i = 1; i == t; i++) {
			int nk = vorkommenGesamt(Vokabular.get(i), localMap); // Anzahl der
																	// Dokumente
																	// die den
																	// Begriff
																	// enthalten
			int tf = vorkommenImDokument(Vokabular.get(i), stream, localMap); // Häufigkeit
																				// des
																				// Begriffs
																				// im
																				// Dokument
			DocVector.add(matching(t, localMap, N, nk, tf));

		}
		return DocVector;
	}

	public double matching(int t,
			Map<String, Map<String, LinkedTerm>> localMap, int counter, int nk,
			int tf) {// berechnet die Relevanz (wdk) eines Dokumentes für k
		double Nenner = 0; // Summe von i=1 bis t (tf*log(N/ni))^2

		for (int i = 1; i == t; i++) {
			Nenner = Nenner
					+ Math.pow(
							tf
									* Math.log(counter
											/ vorkommenGesamt(Vokabular.get(i),
													localMap)), 2);
		}
		double wdk = (tf * Math.log(counter / nk)) / (Math.sqrt(Nenner));
		return wdk;
	}

	public int vorkommenGesamt(String k,
			Map<String, Map<String, LinkedTerm>> localMap) {
		// gibt die Anzahl der Dokumente wieder, die k enthalten
		int v = 0;
		for (Iterator<Entry<String, Map<String, LinkedTerm>>> it = localMap
				.entrySet().iterator(); it.hasNext();) {
			Map.Entry<String, Map<String, LinkedTerm>> entry = it.next();
			if (k == entry.getKey()) {
				v++;
			}
		}
		return v;

	}

	public int vorkommenImDokument(String k, String stream,
			Map<String, Map<String, LinkedTerm>> localMap) {
		// Häufigkeit vom Begriff k im Dokument
		int tf = 0;
		for (Iterator<Entry<String, Map<String, LinkedTerm>>> it = localMap
				.entrySet().iterator(); it.hasNext();) {
			Map.Entry<String, Map<String, LinkedTerm>> entry = it.next();
			if (entry.getKey() == stream) {
				for (Iterator<Map.Entry<String, LinkedTerm>> tmpIterator = entry
						.getValue().entrySet().iterator(); tmpIterator
						.hasNext();) {
					Entry<String, LinkedTerm> entry2 = tmpIterator.next();
					if (entry.getKey() == k)
						tf++;
				}
			}
		}

		return tf;
	}

	public int vocabular(Map<String, Map<String, LinkedTerm>> localMap,
			Map<String, Term> globalMap) {
		// gibt die Anzahl der verschiedenen Begriffe aller Dokumente zurück &
		// erstellt das ListArray Vokabular.
		int v = 0;
		for (Iterator<Entry<String, Map<String, LinkedTerm>>> it = localMap
				.entrySet().iterator(); it.hasNext();) {
			Map.Entry<String, Map<String, LinkedTerm>> entry = it.next();
			v++;

			for (Iterator<Map.Entry<String, LinkedTerm>> it2 = entry.getValue()
					.entrySet().iterator(); it2.hasNext();) {
				Entry<String, LinkedTerm> entry2 = it2.next();
				if (entry.getValue() == entry2.getValue().getGlobalTerm()) {
					globalMap.remove(entry2.getValue().globalTerm.term);
					it2.remove();
				}
				Vokabular.add(entry2.getValue().globalTerm.term);
			}
		}
		return v;
	}

}