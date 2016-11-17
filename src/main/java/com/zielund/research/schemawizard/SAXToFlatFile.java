package com.zielund.research.schemawizard;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import com.zielund.research.schemawizard.util.DotCounter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SAXToFlatFile extends DefaultHandler
	{
		class STFField {
			final String f_label;
			final String f_path;
			String f_value;
			boolean f_isRequired;
			
			public STFField (String path, String label, boolean isRequired) {
			  f_label = label;
			  f_path = path;
			  f_isRequired = isRequired;
			}
			
			public void setValue (String value) {f_value = value;}
			public void clear() {f_value = "";}
			public String getLabel () {return f_label;}
			public String getPath() {return f_path;}
			public String getValue() {return f_value;}
			public boolean isRequired() {return f_isRequired;}
		}
		

		private SAXParser f_saxParser; 
		private String f_inputName;
		private String f_outputName;
		private InputStream f_inputStream;
		private PrintStream f_printStream;
		private boolean f_needHeader = false;
		private DotCounter f_dc = new DotCounter();
		private ArrayList<String> f_path = new ArrayList<String>();
		private StringBuffer f_charBuffer = new StringBuffer();
		private final String f_splitPath;
		private boolean f_uppercase = false;
		private ArrayList<STFField> f_fields = new ArrayList<STFField>();
		private HashMap<String,Integer> f_fieldIndex = new HashMap<String,Integer>();
		
		public SAXToFlatFile(String[] options) throws ParserConfigurationException, SAXException, FileNotFoundException, IOException
		{
			SAXParserFactory factory = SAXParserFactory.newInstance();
			f_saxParser = factory.newSAXParser();
//			f_inputName = "C:\\data\\wluk\\billing.events.08-04-2009.txt";
//			f_inputName = "C:\\data\\wluk\\wluk_3months.xml.gzip";
			//f_splitPath = "\\SVRoot\\file\\Event";

//			f_fields.add(new STFField("\\SVRoot\\event\\header\\@sessionId", "Session_ID", true));
//			f_fields.add(new STFField("\\SVRoot\\event\\display\\displayDocument\\displayPart\\@id", "Document_ID", true));
//			f_fields.add(new STFField("\\SVRoot\\event\\display\\displayDocument\\displayPart\\@docName", "Document_Name", false));
//			f_fields.add(new STFField("\\SVRoot\\event\\display\\displayDocument\\displayPart\\@contentDbName", "Content_DB", false));
//			f_fields.add(new STFField("\\SVRoot\\event\\header\\@timestamp", "Timestamp", false));
//			f_fields.add(new STFField("\\SVRoot\\event\\header\\@sequenceNumber", "Sequence_Number", false));
//			f_fields.add(new STFField("\\SVRoot\\event\\header\\@userId", "User_ID", false));

//			f_inputName = "C:\\data\\jan_6_2010\\international\\filelist.prism.txt.wrapped.xml";
////			f_inputName = "C:\\data\\jan_6_2010\\international\\filelist.prism.txt.example";
//			f_outputName = f_inputName + ".dat";
//			f_splitPath = "\\SVROOT\\FILE\\EVENT";
//			f_outputName = f_inputName + ".search.dat";
//			f_uppercase = true;
//			f_fields.add(new STFField("\\SVROOT\\FILE\\@NAME", "filename", false));
//			f_fields.add(new STFField("\\SVROOT\\FILE\\EVENT\\HEADER\\@TIMESTAMP", "timestamp", true));
//			f_fields.add(new STFField("\\SVROOT\\FILE\\EVENT\\HEADER\\@SESSIOINID", "sessionid", false));
//			f_fields.add(new STFField("\\SVROOT\\FILE\\EVENT\\HEADER\\@USERID", "userid", false));
//			f_fields.add(new STFField("\\SVROOT\\FILE\\EVENT\\HEADER\\@EVENTTYPE", "eventtype", true));
//			f_fields.add(new STFField("\\SVROOT\\FILE\\EVENT\\SEARCH\\@QUERYTEXT", "query", false));

//			f_fields.add(new STFField("\\SVRoot\\event\\header\\@sessionId", "Session_ID", true));
//			f_fields.add(new STFField("\\SVRoot\\event\\header\\@timestamp", "Timestamp", false));
//			f_fields.add(new STFField("\\SVRoot\\event\\header\\@sequenceNumber", "Sequence_Number", false));
//			f_fields.add(new STFField("\\SVRoot\\event\\header\\@userId", "User_ID", false));
//			f_fields.add(new STFField("\\SVRoot\\event\\header\\@eventType", "Event_Type", true));

//			f_outputName = f_inputName + ".docevents.dat";
//			f_fields.add(new STFField("\\SVRoot\\event\\header\\@sessionId", "Session_ID", true));
//			f_fields.add(new STFField("\\SVRoot\\event\\header\\@timestamp", "Timestamp", false));
//			f_fields.add(new STFField("\\SVRoot\\event\\header\\@sequenceNumber", "Sequence_Number", false));
//			f_fields.add(new STFField("\\SVRoot\\event\\header\\@userId", "User_ID", false));
//			f_fields.add(new STFField("\\SVRoot\\event\\header\\@eventType", "Event_Type", true));
//			f_fields.add(new STFField("\\SVRoot\\event\\display\\displayDocument\\displayPart\\@id", "Document_ID", false));
//			f_fields.add(new STFField("\\SVRoot\\event\\print\\printDocument\\printPart\\@id", "Print_Document_ID", false));
			
//			f_outputName = f_inputName + ".prints.dat";
//			f_fields.add(new STFField("\\SVRoot\\event\\header\\@sessionId", "Session_ID", true));
//			f_fields.add(new STFField("\\SVRoot\\event\\print\\printDocument\\printPart\\@id", "Document_ID", true));
//			f_fields.add(new STFField("\\SVRoot\\event\\print\\printDocument\\printPart\\@docName", "Document_Name", false));
//			f_fields.add(new STFField("\\SVRoot\\event\\print\\printDocument\\printPart\\@contentDbName", "Content_DB", false));
//			f_fields.add(new STFField("\\SVRoot\\event\\header\\@timestamp", "Timestamp", false));
//			f_fields.add(new STFField("\\SVRoot\\event\\header\\@sequenceNumber", "Sequence_Number", false));
//			f_fields.add(new STFField("\\SVRoot\\event\\header\\@userId", "User_ID", false));

			f_inputName = "C:\\data\\Jan_6_2010\\wluk_jan6\\wluk_20100106.xml";
			f_outputName = f_inputName + ".search.dat";
			f_splitPath = "\\ROOT\\file\\event";
			f_fields.add(new STFField("\\ROOT\\file\\event\\header\\@sessionId", "Session_ID", true));
			f_fields.add(new STFField("\\ROOT\\file\\event\\header\\@userId", "Cust_ID", true));
			f_fields.add(new STFField("\\ROOT\\file\\event\\header\\@timestamp", "search_time", true));
			f_fields.add(new STFField("\\ROOT\\file\\event\\search\\@queryText", "query_text", true));
			
			buildFieldIndex();
		}
		
		private void buildFieldIndex() {
			for (int i = 0; i < f_fields.size(); i++) {
				f_fieldIndex.put(f_fields.get(i).getPath(), new Integer(i));
			}
		}
		
		public String getPath()
		{
			StringBuffer sb = new StringBuffer("\\");
			String delim = "";
			for (String element : f_path)
			{
				sb.append(delim + element);
				delim = "\\";
			}
			return sb.toString();
		}


		/*
		 * BEGIN DefaultHandler method implementations
		 */
		
		/**
		 * Characters are accumulated in a buffer.  Note that the buffer is cleared
		 * any time an element is encountered.  This means that strings with elements
		 * in-fixed are not fully represented
		 */
		public void characters(char[] arg0, int arg1, int arg2) throws SAXException
		{
			f_charBuffer.append(arg0, arg1, arg2);
		}

		public void clearRecord() {
			for (STFField field : f_fields) {
//				field.clear();
			}
		}
		
		/**
		 * When an element is encountered, its path is added to the counts and
		 * all of its attributes are added to counts, type estimates, and statistics
		 */
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException
		{
			f_charBuffer = new StringBuffer();
			if (f_uppercase) {
				qName = qName.toUpperCase();
			}
			f_path.add(qName);
			String myPath = getPath();
			if (myPath.equals(f_splitPath)) {
				clearRecord();
			}
			for (int i = 0; i < attributes.getLength(); i++)
			{
				String attName = attributes.getQName(i);
				if (f_uppercase) {
					attName = attName.toUpperCase();
				}
				String attPath = myPath + "\\@" + attName;
				if (f_fieldIndex.containsKey(attPath)) {
					STFField field = f_fields.get(f_fieldIndex.get(attPath).intValue());
					field.setValue(attributes.getValue(i));
				}
			}
			f_dc.inc();
		}

		/**
		 * When an element ends, any accumulated characters are collected as a string
		 * and the value is added to counts, type estimates, and statistics
		 */
		public void endElement(String uri, String localName, String qName)
				throws SAXException
		{
			String myPath = getPath();
			if (f_charBuffer.length() > 0) {
				String myPathText = myPath + "\\@[TEXT]";
				if (f_fieldIndex.containsKey(myPathText)) {
					STFField field = f_fields.get(f_fieldIndex.get(myPathText).intValue());
					field.setValue(f_charBuffer.toString());
				}
			}
			if (myPath.equals(f_splitPath)) {
				printRecord();
			}
			f_path.remove(f_path.size() - 1);
		}

		public void printLabels() {
			for (STFField field : f_fields) {
				System.out.print(field.getLabel() + "\t");
				f_printStream.print(field.getLabel() + "\t");
			}
			System.out.println();
			f_printStream.println();
		}
		
		public void printRecord() {
			StringBuffer output = new StringBuffer();
			boolean allRequiredPresent = true;
			for (STFField field : f_fields) {
				String value = field.getValue();
				if (value == null) {value = "";}
				value = value.trim();
				if (field.isRequired() && (field == null || value.equals(""))) {
					allRequiredPresent = false;
				}
				String fieldval = value.replaceAll("\t", "\\t");
				output.append(fieldval + "\t");
			}
			if (allRequiredPresent) {
//				System.out.println(output);
				f_printStream.println(output);
			}
		}
		
		public void endDocument() throws SAXException
		{
			f_dc.finished();
		}
		

		public void resetInputStream() throws IOException {
			File inFile = new File(f_inputName);
			f_inputStream = new FileInputStream(inFile);
			if (f_inputName.toLowerCase().endsWith(".gz") || f_inputName.toLowerCase().endsWith(".gzip")) {
				f_inputStream = new GZIPInputStream(f_inputStream);
			}
			if (f_needHeader) {
				f_inputStream = SAXProdigy.padInputStream(f_inputStream);
			}
			f_dc = new DotCounter();
			f_path.clear();
			f_charBuffer = new StringBuffer();
			File outFile = new File(f_outputName);
			f_printStream = new PrintStream(new FileOutputStream(outFile));
		}
		
		public static void main(String argv[]) throws ParserConfigurationException, SAXException, IOException
		{
			SAXToFlatFile sax2ff = new SAXToFlatFile(argv);
			sax2ff.process();
		}

		private void process() throws IOException, SAXException
		{
			System.err.println ("Input from " + f_inputName);
			System.err.println("Output to " + f_outputName);
			System.err.println("Starting processing");
			resetInputStream();
			printLabels();
			f_saxParser.parse(f_inputStream, this);
		}


	}
