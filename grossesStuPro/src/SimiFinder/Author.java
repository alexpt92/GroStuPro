package SimiFinder;

import java.util.ArrayList;
import java.util.Map;

class Author {

	ArrayList<StreamWithCounter> streamsAsAuthor;
	ArrayList<StreamWithCounter> streamsAsCoAuthor;
	ArrayList<Author> coAuthors;
	String name;

	Author(String str, Map<String, Stream> InputStreamMap) {
		this.streamsAsAuthor = new ArrayList<StreamWithCounter>();
		this.streamsAsCoAuthor = new ArrayList<StreamWithCounter>();
		this.coAuthors = new ArrayList<Author>();
		this.name = str;
	}

	void addStream(Stream stream, boolean isCoAuthor) {
		boolean found = false;
		if (isCoAuthor) {
			for (StreamWithCounter s : streamsAsCoAuthor) {
				if (s.stream.name.equals(stream.name)) {
					s.counter.inc();
					found = true;
					break;
				}

			}
			if (!found) {
				streamsAsCoAuthor.add(new StreamWithCounter(stream));
			}
		} else {
			for (StreamWithCounter s : streamsAsAuthor) {
				if (s.stream.name.equals(stream.name)) {
					s.counter.inc();
					found = true;
					break;
				}
			}
			if (!found) {
				streamsAsAuthor.add(new StreamWithCounter(stream));
			}
		}

	}

}


