package SimiFinder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

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
	private Map<String, Stream> streamMap;

	private Map<String, Author> authorMap;

	private Map<String, String[]> aliasMap;

	private StopWords stop;

	MapManager(Map<String, Term> inputGlobal,
			Map<String, Map<String, LinkedTerm>> inputLocal,
			Map<String, Author> inputAuthor, Map<String, String[]> inputAlias,
			Map<String, String> inputCoAuthor,
			Map<String, Stream> inputStreamMap, StopWords inputStop) {
		this.globalMap = inputGlobal;
		this.localMap = inputLocal;
		this.authorMap = inputAuthor;
		this.aliasMap = inputAlias;
		this.streamMap = inputStreamMap;
		this.stop = inputStop;
	}

	void authorSimilarity(String fileLoc, String method) {
		try {
			File file = new File(fileLoc);

			PrintStream ps = new PrintStream(file);
			ps.println("<out>");
			for (String sName : this.streamMap.keySet()) {
				if (this.streamMap.get(sName).entryCount.getVal() > 500) {
					this.streamMap.get(sName).findCommonStreams(method);
					// findet alle gemeinsamen
					ps.println("<stream key=\"" + this.streamMap.get(sName).type + sName + "\">");
					for (StreamWithCounter s : this.streamMap.get(sName).commonStreams) {
						ps.println("<entry name=\"" +s.stream.type + s.stream.name + "\" ratio=\""
								+ s.counter.getVal() + "\" />");
					}
					ps.println("</stream>");
				}

			}
			ps.println("</out>");
			ps.close();
			System.out.println("Datei " + fileLoc + " erstellt");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	void addAuthor(String str, String stream, boolean isCoAuthor,
			String mainAuthorName) {
		// fuegt ein und bearbeitet die author Elemente in authorMap und
		// streamMap
		if (authorMap.containsKey(str)) {
			authorMap.get(str).addStream(streamMap.get(stream), isCoAuthor);

			if (isCoAuthor) {
				authorMap.get(mainAuthorName).coAuthors.add(authorMap.get(str));
			} else {

				streamMap.get(stream).addAuthor(authorMap.get(str));

			}

		} else {
			authorMap.put(str, new Author(str, streamMap));
			authorMap.get(str).addStream(streamMap.get(stream), isCoAuthor);
			if (isCoAuthor) {
				authorMap.get(mainAuthorName).coAuthors.add(authorMap.get(str));
			}

			else {
				this.streamMap.get(stream).addAuthor(authorMap.get(str));
			}
		}
	}

	void addTerm(String str, String stream, boolean filterStops) {
		// fuellt local und globalMap
		boolean isStop = false;
		if (filterStops) {
			isStop = stop.isStopWord(str);
		}
		if (!isStop) {
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
	private double dVal;

	Counter() {
		this.val = 1;
		this.dVal = 1.0;
	}

	public void inc() {
		this.val++;
		
	}
	public void incD(){
		this.dVal += 1.0;
	}

	public int getVal() {
		return val;
	}

	public double getDVal() {
		return dVal;
	}
	public void setDVal(double d) {
		this.dVal = d;
	}
	
	public void addVal(int i) {
		this.val += i;
	}

	public void addDVal(double d) {
		this.dVal += d;
	}
}
