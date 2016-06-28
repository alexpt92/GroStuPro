package SimiFinder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

class Stream {
	String name;
	String type;
	ArrayList<Author> authors;
	ArrayList<StreamWithCounter> commonStreams;
	Counter entryCount;

	Stream(String str, boolean journal) {
		this.name = str;
		this.authors = new ArrayList<Author>();
		this.commonStreams = new ArrayList<StreamWithCounter>();
		this.entryCount = new Counter();
		if (journal) {
			this.type = "journals/";
		} else {
			this.type = "conf/";
		}
	}

	void addAuthor(Author author) {
		// addAuthor fuegt author nur hinzu, falls er noch nicht existiert. Das
		// alte Element muss nicht ueberschrieben werden, da es sich nur um
		// einen Pointer handelt.
		boolean found = false;
		if (!authors.isEmpty()) {
			for (Author a : authors) {
				if (a.name.equals(author.name)) {
					found = true;

					break;
				}
			}
		}
		if (!found) {
			this.authors.add(author);
			this.entryCount.inc();
		}
	}

	void findCommonStreams(String method) {
		// baut commonStreams
		boolean found = false;
		ArrayList<StreamWithCounter> tmpStreams = new ArrayList<StreamWithCounter>();
		for (Author a : this.authors) {
			for (StreamWithCounter glblStream : a.streamsAsAuthor) {
				StreamWithCounter globalStream = new StreamWithCounter(
						glblStream.stream);// Damit kein Pointer uebergeben
											// wird, sondern nur mit den Werten
											// gerechnet wird

				// iteriert ueber alle Streams, in denen der Author als
				// Hauptautor geschrieben hat und tut das fuer jeden Hauptautor
				// der im aktuellen Stream geschrieben hat.
				for (StreamWithCounter localStream : tmpStreams) {
					// localStream ist der Stream, der bereits vorgekommen ist.
					if (globalStream.stream.name
							.equals(localStream.stream.name)
							&& !globalStream.stream.name.equals(this.name)) {
						// wenn der Stream schon vorgekommen ist, wird sein
						// counter, um den bereits vorhandenen Counter erhoeht
						localStream.counter.addVal(globalStream.counter
								.getVal());
						found = true;
						break;
					}
				}
				if (!found && !globalStream.stream.name.equals(this.name)) {
					tmpStreams.add(globalStream);
				}
			}
			if (method.contains("asCA")) {
				for (StreamWithCounter glblStream : a.streamsAsCoAuthor) {
					StreamWithCounter globalStream = new StreamWithCounter(
							glblStream.stream);
					for (StreamWithCounter localStream : tmpStreams) {

						if (globalStream.stream.name
								.equals(localStream.stream.name)
								&& !globalStream.stream.name.equals(this.name)) {

							localStream.counter.addDVal((globalStream.counter
									.getDVal() / 2));
							found = true;
							break;
						}
					}
					if (!found && !globalStream.stream.name.equals(this.name)) {
						tmpStreams.add(globalStream);
					}
				}
			}
		}
		if (method.contains("compCA")){
			
		}
		if (method.contains("asCA")){
			//consider appearances of each author as a coauthor
			for (StreamWithCounter s : tmpStreams){
				s.counter.addVal((int)s.counter.getDVal());
			}
		}
		for (StreamWithCounter s : tmpStreams){
			s.counter.setDVal((double)s.counter.getVal()/(double)s.stream.authors.size());
		}
		
		
		try {
			Collections.sort(tmpStreams, new Comparator<StreamWithCounter>() {

				@Override
				public int compare(StreamWithCounter str1,
						StreamWithCounter str2) {

					return str2.counter.getVal() - str1.counter
							.getVal();
				}
			});
			int i = 0;
			for (StreamWithCounter counted : tmpStreams) {
				if (i < 10) {
					commonStreams.add(counted);
					i++;
				}
			}
			tmpStreams.clear();
		} catch (Exception e) {
			System.out.println("Fuer " + name + " ist tmpStreams leer");
		}
	}
}

class StreamWithCounter {
	Counter counter;
	Stream stream;

	StreamWithCounter(Stream s) {
		this.counter = new Counter();
		this.stream = s;
	}
}