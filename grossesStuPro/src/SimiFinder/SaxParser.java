package SimiFinder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class SaxParser {

	private long time;
	private boolean checkStopWords = false;

	public SaxParser(String dblpXmlFileName) {
		try {
			time = System.currentTimeMillis();
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();

			ConfigHandler handler = new ConfigHandler();
			if (dblpXmlFileName.endsWith(".gz"))
				saxParser.parse(new GZIPInputStream(new FileInputStream(
						dblpXmlFileName)), handler);
			else
				saxParser.parse(new FileInputStream(dblpXmlFileName), handler);
		} catch (IOException e) {
			System.out.println("Error reading URI: " + e.getMessage());
		} catch (SAXException e) {
			System.out.println("Error in parsing: " + e.getMessage());
		} catch (ParserConfigurationException e) {
			System.out.println("Error in XML parser configuration: "
					+ e.getMessage());
		}
	}

	public static void main(String[] args) {
		System.setProperty("entityExpansionLimit", "2500000");
		/*
		 * if (args.length < 1) { System.out
		 * .println("Usage: java BooleanRetrieval1 [dblpXmlFileName]");
		 * System.exit(0); }
		 */
		/*
		System.out.println("Dateipfad der dblp eingeben: ");
		Scanner input = new Scanner(System.in);
		String s = input.next();
		input.close(); 
		*/
		String s = "C:\\Users\\Admin\\Desktop\\Uni\\GrStuPro\\dblp.xml";
		new SaxParser(s);
	}

	class ConfigHandler extends DefaultHandler {
		// Warum muss initialisiert werden`und nicht einfach ohne instanz:
		// "new StopWords()"
		StopWords stop = new StopWords();
		ArrayList<String> stops = new ArrayList<String>();
		private Map<String, Counter> m = new HashMap<String, Counter>();
		private boolean insideInterestingField = false, getIt = false;
		private String Value = "";

		public void setDocumentLocator(Locator locator) {
		}

		public void startElement(String namespaceURI, String localName,
				String rawName, Attributes atts) throws SAXException {

			if (atts.getValue("key") != null) {

				String str = atts.getValue("key");
				if (str.startsWith("journals/") || str.startsWith("conf/")) {
					getIt = true;

				}
			}

			if (rawName.equals("title") && getIt) {
				insideInterestingField = true;

			}

		}

		public void endElement(String namespaceURI, String localName,
				String rawName) throws SAXException {
		}

		@Override
		public void startDocument() {
			System.out.println("Document starts.");
		}

		@Override
		public void endDocument() {
			System.out.println("Document ends.");
			if (m != null) {
				//PrintWordList.printCountedList(m);
			} else {
				System.out.println("Map is empty");
			 
			}
			// Datei mit den gefundenen Stopwörten aus StopWordsList.txt wird
			// ausgegeben
			if (checkStopWords) {
				try {
					PrintStream ps = new PrintStream(
							new File("actualStops.txt"));
					for (String s : stops) {
						ps.println(s);
					}
					ps.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				System.out.println("actualStops printed.");
			}
			System.out.println("Laufzeit" + " " + (System.currentTimeMillis() - time)/1000);
		}

		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			if (insideInterestingField) {
				Value = new String(ch, start, length);
				String[] tokens = Value.split(" ");
				for (String s : tokens) {
					s = s.replaceAll("[^a-zA-Z]", "");
					s = s.toLowerCase();
					if (!StopWords.isStopWord(s)) {
						if (m.get(s) != null) {
							m.get(s).inc();
						} else {

							Counter counter = new Counter();
							m.put(s, counter);

						}
						
					}
					// benutzen, um die Stopwortliste mit den tatsächlichen
					// Stopwörtern zu vergleichen
					/*if (checkStopWords && StopWords.isStopWord(s)) {
						if (!stops.contains(s)) {
							stops.add(s);
						}
					}
					*/
				}
				getIt = false;
				insideInterestingField = false;
			}
		}

		private void Message(String mode, SAXParseException exception) {
			System.out.println(mode + " Line: " + exception.getLineNumber()
					+ " URI: " + exception.getSystemId() + "\n" + " Message: "
					+ exception.getMessage());
		}

		public void warning(SAXParseException exception) throws SAXException {

			Message("**Parsing Warning**\n", exception);
			throw new SAXException("Warning encountered");
		}

		public void error(SAXParseException exception) throws SAXException {

			Message("**Parsing Error**\n", exception);
			throw new SAXException("Error encountered");
		}

		public void fatalError(SAXParseException exception) throws SAXException {

			Message("**Parsing Fatal Error**\n", exception);
			throw new SAXException("Fatal Error encountered");
		}

	}
}