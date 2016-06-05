package SimiFinder;

import java.util.*;
import java.util.Map.Entry;

public class MapManager {
	// globalTerms sind alle Terms zusammen in einer Map. Der Key ist dabei der
	// Term
	private Map<String, Term> globalMap/* = new HashMap<String, Term>() */;

	// localTerms sind alle Terms pro Stream(Journal oder Conference). Der Key
	// ist dabei der Journal/ConferenceName. Die zweite Map enthï¿½lt den Term
	// als
	// Schlï¿½ssel.
	private Map<String, Map<String, LinkedTerm>> localMap/*
														 * = new HashMap<String,
														 * Map<String,
														 * LinkedTerm>>()
														 */;

	private Map<String, Author> authors;

	private Map<String, String[]> aliases = new HashMap<String, String[]>();
	
	MapManager(Map<String, Term> inputGlobal,
			Map<String, Map<String, LinkedTerm>> inputLocal,
			Map<String, Author> inputAuthor) {
		this.globalMap = inputGlobal;
		this.localMap = inputLocal;
		this.authors = inputAuthor;
	}

	/*
	 * Struktur localMap: (Schlï¿½ssel(Journal/ConferenceName)
	 * ,(Schlï¿½ssel(TermName),(Term(lokaler Term),Term(globaler Term)))
	 * ,(Schlï¿½ssel(TermName),(Term(lokaler Term),Term(globaler Term)))
	 * ,(Schlï¿½ssel(TermName),(Term(lokaler Term),Term(globaler Term))) ,...)
	 */

	void addAuthor(String str, String stream) {
		if (!authors.containsKey(str)) {
			authors.put(str, new Author(str));
			authors.get(str).addStream(stream);
		} else {
			if (authors.get(str).streams.containsKey(stream)) {
				authors.get(str).streams.get(stream).inc();
			} else {
				authors.get(str).addStream(stream);
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
		aliases.put(names[0], names);		
		
	}
	void checkAliases(){
		for(Iterator<Entry<String,String[]>>it = aliases.entrySet().iterator(); it.hasNext();){
			
		}
	}
	//schmeißt alle überflüssigen Terme, also die, mit vorkommen 1 aus localMap und globalMap
	void filterMap(Map<String, Map<String, LinkedTerm>> localMap, Map<String, Term> globalMap) {
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

	// Methode 1: Wenn der Term noch nie vorgekommen ist wird
	// diese Methode ausgefï¿½hrt
	void createAllNewEntry(String str, String stream) {
		Map<String, LinkedTerm> tmpMap = new HashMap<String, LinkedTerm>();
		Term glblTerm = new Term(str);

		LinkedTerm tmpLTerm = new LinkedTerm();
		tmpLTerm.setLocalTerm(new Term(str));
		tmpLTerm.setGlobalTerm(glblTerm);
		tmpMap.put(str, tmpLTerm);

		globalMap.put(str, glblTerm);
		localMap.put(stream, tmpMap);


		
	}

	// Methode 2: Wenn der Term global schon vorgekommen ist, aber der Stream
	// noch nicht
	void createNewLocalEntry(String str, String stream) {
		Map<String, LinkedTerm> tmpMap = new HashMap<String, LinkedTerm>();

		LinkedTerm tmpLTerm = new LinkedTerm();
		tmpLTerm.setLocalTerm(new Term(str));
		tmpLTerm.setGlobalTerm(globalMap.get(str));
		tmpMap.put(str, tmpLTerm);

		localMap.put(stream, tmpMap);


	}

	// Methode 3: Wenn der Term global schon vorgekommen ist und der Stream
	// bereits existiert, der Term aber noch nicht im Stream eingetragen
	void createNewTermEntry(String str, String stream) {
		Map<String, LinkedTerm> tmpMap = new HashMap<String, LinkedTerm>();

		LinkedTerm tmpLTerm = new LinkedTerm();
		tmpLTerm.setLocalTerm(new Term(str));
		tmpLTerm.setGlobalTerm(globalMap.get(str));
		tmpMap.put(str, tmpLTerm);

		localMap.get(stream).put(str, tmpLTerm);

	}

}

// Author enthï¿½lt 2 Listen: -In streams gibt es fï¿½r jedes Journal/Conference
// einen Eintrag und die Anzahl an Artikeln in diesem Stream, an denen der
// Author beteiligt war.
// -coAuthors entï¿½hlt alle Coauthoren des Authors, als rekursiv verschachtelte
// Struktur.
class Author {
	Map<String, Counter> streams = new HashMap<String, Counter>();
	ArrayList<Author> coAuthors = new ArrayList<Author>();
	ArrayList<Author> aliases = new ArrayList<Author>();
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
