package tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
	private int documentCount = 0;

	public SaxParser(String dblpXmlFileName) {
		try {

			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			DefaultHandler handler = new ConfigHandler();
			saxParser.getXMLReader().setFeature(
					"http://xml.org/sax/features/validation", true);
			if (dblpXmlFileName.endsWith(".gz"))
				saxParser.parse(new GZIPInputStream(new FileInputStream(
						dblpXmlFileName)), handler);
			else
				saxParser.parse(new File(dblpXmlFileName), handler);
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
			System.out
					.println("Usage: java BooleanRetrieval1 [dblpXmlFileName]");
			System.exit(0);
		}
		new SaxParser(args[0]);
	}

	class ConfigHandler extends DefaultHandler {
		private String key = null;
		private String Value = "";
		private boolean insideInterestingField = false;
		private int level = 0;

		public void setDocumentLocator(Locator locator) {
		}

		public void startElement(String namespaceURI, String localName,
				String rawName, Attributes atts) throws SAXException {

			level++;
			if (level == 2) {
				if (atts.getLength() > 0) {
					String s = atts.getValue("key");
					setKey(null);
					if (s != null && !s.startsWith("homepages/")) {
						setKey(s);
						documentCount++;
						if (documentCount % 100000 == 0)
							System.err.println(documentCount + " documents");
					}
				}
				return;
			}
			if (level == 3) {
				Value = "";
				insideInterestingField = rawName.equals("title");
			}
		}

		public void endElement(String namespaceURI, String localName,
				String rawName) throws SAXException {
			level--;
			if (level == 2 && insideInterestingField && Value.length() > 0) {
			}
		}

		@Override
		public void startDocument() {
			System.out.println("Document starts.");
		}

		@Override
		public void endDocument() {
			System.out.println("Document ends.");
		}

		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			if (insideInterestingField)
				Value += new String(ch, start, length);
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

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

	}

}