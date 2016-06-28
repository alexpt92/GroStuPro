package SimiFinder;

import java.io.FileInputStream;
import java.io.IOException;
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
	private String fileLocation, stopWordsLocation, method;
	private long time;
	private boolean filterStopWords, noValidation;

	public SaxParser(String[] args) {

		try {
			System.out.println("Type 'help' for run info");
			if (args[0].equals("help")) {
				System.out
						.println("options: ./simiFinder.jar [dblpFileLocaiton] [OutputFile] [runoptions]"
								+ "\n\n"
								+ "Runoptions:"
								+ "\n"
								+ "'-sw=[StopWordsFileLocation]': enable filtering of StopWords taken from StopWordsFile"
								+ "\n"
								+ "'-asCA': (asCoAuthor) consider appearances of each author as a coauthor"
								+ "\n"
								+ "'-noValiation': no DTD is needed, since the xml is considered valid(increases runtime)"
								+ "\n");
				System.exit(0);
			}
			method = "";
			String dblpXmlFileName = args[0];
			fileLocation = args[1];
			for (String s : args){

				if (s.startsWith("-sw")){
					filterStopWords = true; 
					stopWordsLocation = s.split("-sw=")[1];
				}
				else if (s.startsWith("-asCA")){
					method += "asCA";
					
				}
				else if(s.startsWith("-noValidation")){
					noValidation = true;
				}
			}
			

			time = System.currentTimeMillis();
			SAXParserFactory factory = SAXParserFactory.newInstance();
			if (noValidation){factory.setValidating(true);}
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
		if (args.length < 1) {
			System.out.println("Type 'help' for run info");
			System.exit(0);
		}

		// System.out.println("Dateipfad der dblp eingeben: "); Scanner input =
		// new Scanner(System.in); String s = input.next(); input.close();

		// String s =
		// "C:\\Users\\Admin\\Desktop\\Uni\\GrStuPro\\dblp\\dblp.xml";
		new SaxParser(args);
	}

	class ConfigHandler extends DefaultHandler {
		private Map<String, Term> globalMap = new HashMap<String, Term>();
		private Map<String, Map<String, LinkedTerm>> localMap = new HashMap<String, Map<String, LinkedTerm>>();
		private Map<String, Author> authors = new HashMap<String, Author>();
		private Map<String, String[]> aliasMap = new HashMap<String, String[]>();
		private Map<String, String> coAuthorMap = new HashMap<String, String>();
		private Map<String, Stream> streamMap = new HashMap<String, Stream>();

		StopWords stop;
		
		ArrayList<String> stops = new ArrayList<String>();

		private boolean insideInterestingField = false,
				insideAuthorField = false, getIt = false,
				insideWwwAuthorField = false, getWww = false, journal = false;
		private String Value = "", streamName = "", mainAuthor = "",
				oldStreamName = "";

		private MapManager maps = new MapManager(globalMap, localMap, authors,
				aliasMap, coAuthorMap, streamMap, stop);
		private int authorCount = 0;

		public void setDocumentLocator(Locator locator) {
		}

		public void startElement(String namespaceURI, String localName,
				String rawName, Attributes atts) throws SAXException {

			if (atts.getValue("key") != null) {

				String str = atts.getValue("key");
				if (str.startsWith("journals/") || str.startsWith("conf/")) {
					getIt = true;
					String[] tmp = str.split("/");
					streamName = tmp[1];
					if (!oldStreamName.equals(streamName)) {
						if (str.startsWith("journals/")) {
							journal = true;
						} else if (str.startsWith("conf/")) {
							journal = false;
						}
						streamMap.put(streamName, new Stream(streamName,
								journal));
						oldStreamName = streamName;
					}
				}
				if (str.startsWith("homepages/")) {
					getWww = true;
				}
			}

			if (rawName.equals("title") && getIt) {
				insideInterestingField = true;

			}
			if (rawName.equals("author") && getIt) {
				insideAuthorField = true;
			}

			if (rawName.equals("author") && getWww) {
				insideWwwAuthorField = true;
			}

		}

		public void endElement(String namespaceURI, String localName,
				String rawName) throws SAXException {
			if (rawName.equals("www") && getWww == true) {
			}

			if (rawName.equals("author")) {
				authorCount++;
			}

			if ((rawName.equals("article") || rawName.equals("inproceedings")
					|| rawName.equals("proceedings") || rawName.equals("book")
					|| rawName.equals("incollection")
					|| rawName.equals("phdthesis") || rawName
						.equals("masterthesis")) && getIt) {
				getIt = false;
				authorCount = 0;
				mainAuthor = "";
			}
		}

		@Override
		public void startDocument() {
			System.out.println("Document starts.");
			if (filterStopWords){
				stop = new StopWords(stopWordsLocation);
			}
		}

		@Override
		public void endDocument() {

			/*
			 * for (String key : authors.keySet()) // AutorMapTest {
			 * System.out.print("Key: " + key + " - ");
			 * System.out.print("Value: " + authors.get(key) + "\n"); }
			 */

			// maps.filterMap(localMap, globalMap);
			System.out.println("Document ends.");
			maps.authorSimilarity(fileLocation, method);
			System.out.println("Laufzeit" + " "
					+ (System.currentTimeMillis() - time) / 1000 +"s");

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
					maps.addTerm(s, streamName, filterStopWords);
				}
			}

			if (insideAuthorField) {
				String Value = new String(ch, start, length);

				if (authorCount == 0) {
					maps.addAuthor(Value, streamName, false, "");
					mainAuthor = Value;
				} else if (authorCount > 0 && !mainAuthor.equals("")) {
					maps.addAuthor(Value, streamName, true, mainAuthor);
				}
			}

			if (insideWwwAuthorField) {

			}

			insideInterestingField = false;
			insideAuthorField = false;
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