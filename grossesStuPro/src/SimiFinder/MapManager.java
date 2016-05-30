package SimiFinder;

import java.util.*;

public class MapManager {
	// globalTerms sind alle Terms zusammen in einer Map. Der Key ist dabei der
	// Term
	private Map<String, Term> globalMap/* = new HashMap<String, Term>() */;

	// localTerms sind alle Terms pro Stream(Journal oder Conference). Der Key
	// ist dabei der Journal/ConferenceName. Die zweite Map enth�lt den Term als
	// Schl�ssel.
	private Map<String, Map<String, LinkedTerm>> localMap/*
														 * = new HashMap<String,
														 * Map<String,
														 * LinkedTerm>>()
														 */;

	private Map<String, Author> authors;

	MapManager(Map<String, Term> inputGlobal,
			Map<String, Map<String, LinkedTerm>> inputLocal,
			Map<String, Author> inputAuthor) {
		this.globalMap = inputGlobal;
		this.localMap = inputLocal;
		this.authors = inputAuthor;
	}

	/*
	 * Struktur localMap: (Schl�ssel(Journal/ConferenceName)
	 * ,(Schl�ssel(TermName),(Term(lokaler Term),Term(globaler Term)))
	 * ,(Schl�ssel(TermName),(Term(lokaler Term),Term(globaler Term)))
	 * ,(Schl�ssel(TermName),(Term(lokaler Term),Term(globaler Term))) ,...)
	 */

	void addAuthor(String str, String stream) {
		if (!authors.containsKey(str)) {
			authors.put(str, new Author(str));
			authors.get(str).addStream(stream);
		} 
		else {
			if (authors.get(str).streams.containsKey(stream)) {
				authors.get(str).streams.get(stream).inc();
			} 
			else {
				authors.get(str).addStream(stream);
			}
		}

	}

	void addTerm(String str, String stream) {
		if (!StopWords.isStopWord(str)) {
			if (!globalMap.containsKey(str)) {
				// Methode 1
				createAllNewEntry(str, stream);
				System.out.println(stream);
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

	// die kommenden Methoden kann man sicher noch zusammenfassen, ich fand es
	// vorerst einfacher f�r den �berblick

	// Methode 1: Wenn der Term noch nie vorgekommen und kein StopWort ist wir
	// diese Methode ausgef�hrt
	void createAllNewEntry(String str, String stream) {
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

	// Methode 2: Wenn der Term global schon vorgekommen ist, aber der Stream
	// noch nicht
	void createNewLocalEntry(String str, String stream) {
		Map<String, LinkedTerm> tmpMap = new HashMap<String, LinkedTerm>();

		LinkedTerm tmpLTerm = new LinkedTerm();
		tmpLTerm.setLocalTerm(new Term(str));
		tmpLTerm.setGlobalTerm(globalMap.get(str));
		tmpMap.put(str, tmpLTerm);

		localMap.put(stream, tmpMap);

		tmpMap.clear();
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

		tmpMap.clear();

	}



}

// Author enth�lt 2 Listen: -In streams gibt es f�r jedes Journal/Conference
// einen Eintrag und die Anzahl an Artikeln in diesem Stream, an denen der
// Author beteiligt war.
// -coAuthors ent�hlt alle Coauthoren des Authors, als rekursiv verschachtelte
// Struktur.
class Author {
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
	int val;

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
