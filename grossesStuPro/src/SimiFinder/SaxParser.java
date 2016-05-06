package SimiFinder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
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

	public SaxParser(String dblpXmlFileName) {
		try {
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
		System.out.println("Dateipfad der dblp eingeben: ");
		Scanner input = new Scanner(System.in);
		String s = input.next();
		input.close();
		new SaxParser(s);
	}

	class ConfigHandler extends DefaultHandler {
		// private String key = null;
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
			Map<String, Integer> resultMap = new HashMap<String, Integer>();
			System.out.println("Document ends.");
			if (m != null) {
				try {
					for (Entry<String, Counter> entry : m.entrySet()) {
						if (entry.getValue().getVal() > 1) {
							resultMap.put(entry.getKey(), entry.getValue()
									.getVal());
						}
					}
					System.out.println("Start sorting");
					resultMap = OrderOutput.sortMapByValues(resultMap);
					System.out.println("Done sorting");
					PrintStream ps = new PrintStream(new File("wordCount.txt"));
					for (Entry<String, Integer> entry : resultMap.entrySet()) {

						ps.println(entry.getValue() + " "
								+ entry.getKey());

					}
					System.out.println("File printed.");
					ps.close();
					m.clear();
					resultMap.clear();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("Map is empty");

			}
		}

		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			if (insideInterestingField) {
				Value = new String(ch, start, length);
				String[] tokens = Value.split(" ");
				for (String s : tokens) {
					s = s.toLowerCase();
					if (m.get(s) != null) {
						m.get(s).inc();
					} else {

						Counter counter = new Counter();
						m.put(s, counter);

					}

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