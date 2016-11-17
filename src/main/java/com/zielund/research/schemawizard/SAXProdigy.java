package com.zielund.research.schemawizard;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import com.zielund.research.schemawizard.util.DotCounter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SAXProdigy extends DefaultHandler
{

	private SAXParser f_saxParser;
	private int f_phase = 0;
	private String f_inputName;
	private InputStream f_inputStream;
	private boolean f_needHeader = false;
	private boolean f_uppercase = true;
	private DotCounter f_dc = new DotCounter();
	private ArrayList<String> f_path = new ArrayList<String>();
	private ArrayList<TreeMap<String, Integer>> f_pathMap = new ArrayList<TreeMap<String, Integer>>();
	private StringBuffer f_charBuffer = new StringBuffer();
	// private TreeMap<String, Integer> f_elementCounts = new TreeMap();
	private TreeMap<String, MyTypeCheck> f_elementTypes = new TreeMap<String, MyTypeCheck>();
	private TreeMap<String, MyStatistic> f_elementStats = new TreeMap<String, MyStatistic>();

	public SAXProdigy(String inputName) throws ParserConfigurationException, SAXException
	{
		SAXParserFactory factory = SAXParserFactory.newInstance();
		f_saxParser = factory.newSAXParser();
		f_inputName = inputName;
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

	// private void addElementCount(String myPath)
	// {
	// if (f_elementCounts.containsKey(myPath))
	// {
	// f_elementCounts.put(myPath, new Integer(f_elementCounts.get(myPath).intValue() + 1));
	// } else
	// {
	// f_elementCounts.put(myPath, new Integer(1));
	// }
	// TreeMap<String,Integer> thisPathMap = f_pathMap.get(f_pathMap.size() - 1);
	// if (thisPathMap.containsKey(myPath))
	// {
	// thisPathMap.put(myPath, new Integer(thisPathMap.get(myPath).intValue() + 1));
	// } else
	// {
	// thisPathMap.put(myPath, new Integer(1));
	// }
	// }

	private void addElementValue(String myPath, String value)
	{
		if (f_elementTypes.containsKey(myPath))
		{
			MyTypeCheck mtc = f_elementTypes.get(myPath);
			mtc.setTypeAuto(value);
			mtc.incTotalCount();
		} else
		{
//			System.err.print("#");
			MyTypeCheck mtc = new MyTypeCheck(value);
			f_elementTypes.put(myPath, mtc);
		}
		TreeMap<String, Integer> thisPathMap = f_pathMap.get(f_pathMap.size() - 1);
		if (thisPathMap.containsKey(myPath))
		{
			thisPathMap.put(myPath, new Integer(thisPathMap.get(myPath).intValue() + 1));
		} else
		{
			thisPathMap.put(myPath, new Integer(1));
		}
	}

	private void addElementStat(String myPath, String value)
	{
		if (f_elementStats.containsKey(myPath))
		{
			MyStatistic ms = f_elementStats.get(myPath);
			try
			{
				ms.add(value);
				if (ms.getlongCountDistinct() > 1000000)
				{
					System.err.println("Disabling " + ms.getName() + " at distinct count of " + ms.distValues.size());
					ms.DisableDist();
				}
			} catch (OutOfMemoryError oome)
			{
				useLessMemory();
				ms.add(value);
			}
		} else
		{
			if (!f_elementTypes.containsKey(myPath))
			{
				f_elementTypes.put(myPath, new MyTypeCheck(MyTypeCheck.TN_STRING));
			}
			MyTypeCheck mtc = f_elementTypes.get(myPath);
			mtc.setGivenName(myPath);
			MyStatistic ms = new MyStatistic(f_elementTypes.get(myPath));
			f_elementStats.put(myPath, ms);
			ms.add(value);
		}
	}

	/*
	 * BEGIN DefaultHandler method implementations
	 */

	/**
	 * Characters are accumulated in a buffer. Note that the buffer is cleared any time an element is encountered. This means that strings with elements in-fixed are not fully
	 * represented
	 */
	public void characters(char[] arg0, int arg1, int arg2) throws SAXException
	{
		if (f_phase >= 0)
		{
			String s = new String(arg0, arg1, arg2);
			f_charBuffer.append(s);
//			f_charBuffer.append(arg0, arg1, arg2);
		}
	}

	/**
	 * When an element is encountered, its path is added to the counts and all of its attributes are added to counts, type estimates, and statistics
	 */
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
	{
		if (f_phase >= 0)
		{
			f_charBuffer = new StringBuffer();
			if (f_uppercase) {
				qName = qName.toUpperCase();
			}
			f_path.add(qName);
			String myPath = getPath();
			if (f_phase == 1)
			{
				for (int i = 0; i < attributes.getLength(); i++)
				{
					String attName = attributes.getQName(i);
					if (f_uppercase) {
						attName = attName.toUpperCase();
					}
					String attType = attributes.getType(i);
					String attValue = attributes.getValue(i);
					addElementStat(myPath + "\\@" + attName + "(" + attType + ")", attValue);
				}
			} else
			{
				addElementValue(myPath, "");
				for (int i = 0; i < attributes.getLength(); i++)
				{
					String attName = attributes.getQName(i);
					if (f_uppercase) {
						attName = attName.toUpperCase();
					}
					String attType = attributes.getType(i);
					String attValue = attributes.getValue(i);
					// addElementCount(myPath + "\\@" + attName + "(" + attType + ")");
					addElementValue(myPath + "\\@" + attName, attValue);
				}
			}
			f_pathMap.add(new TreeMap<String, Integer>());
		}
		f_dc.inc();
	}

	/**
	 * When an element ends, any accumulated characters are collected as a string and the value is added to counts, type estimates, and statistics
	 */
	public void endElement(String uri, String localName, String qName) throws SAXException
	{
		if (f_phase >= 0)
		{
			String myPath = getPath();
			String buffer = f_charBuffer.toString().trim();
			if (buffer.length() > 0)
			{
				if (f_phase == 1)
				{
					addElementStat(myPath + "\\@[text]", buffer);
				} else
				{
					// addElementCount(myPath + "\\@[text]");
					addElementValue(myPath + "\\@[text]", buffer);
				}
			}
			f_path.remove(f_path.size() - 1);
			updateCardinality(f_pathMap.get(f_pathMap.size() - 1));
			f_pathMap.remove(f_pathMap.size() - 1);
		}
		if (f_phase < 0 && f_dc.getCounter() > 100000)
		{
			throw new SAXException("Skip the rest");
		}
		// if (f_phase < 1 && f_dc.getCounter() > 5000000) {
		// throw new SAXException("Skip the rest");
		// }
	}

	private void updateCardinality(TreeMap<String, Integer> treeMap)
	{
		for (String myPath : treeMap.keySet())
		{
			if (f_elementTypes.containsKey(myPath))
			{
				f_elementTypes.get(myPath).setCardinality(treeMap.get(myPath).intValue());
			} // The else clause /shouldn't/ exist
		}

	}

	/**
	 * Displays the final report
	 */
	public void endDocument() throws SAXException
	{
		f_dc.finished();
		// finalReport();
	}

	@Override
	public void startDocument() throws SAXException
	{
		f_pathMap.add(new TreeMap<String, Integer>());
	}

	public void useLessMemory()
	{
		int mostDistinct = 0;
		MyStatistic topStat = null;
		for (MyStatistic ms : f_elementStats.values())
		{
			if (ms.isDistEnabled() && ms.distValues.size() > mostDistinct)
			{
				mostDistinct = ms.distValues.size();
				topStat = ms;
			}
		}
		System.err.println("Disabling distribution of " + topStat.getName() + " at distinct count of "
				+ topStat.getCountDistinct());
		topStat.DisableDist();
	}

	private ArrayList<ArrayList<String>> getFinalReportStructure()
	{
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		ArrayList<String> labels = new ArrayList<String>();
		labels.add("Path");
		labels.add("N");
		labels.add("Type");
		labels.add("Cardinality");
		if (f_elementStats.size() > 0)
		{
			labels.add("DistinctCount");
			labels.add("SpecialProperties");
			labels.add("Ordering");
			labels.add("Min");
			labels.add("Max");
			labels.add("Mean");
			labels.add("Hist#1");
			labels.add("Hist#2");
			labels.add("Hist#3");
			labels.add("Hist#4");
			labels.add("Hist#5");
			labels.add("Hist#6");
			labels.add("Hist#7");
			labels.add("Hist#8");
			labels.add("Hist#9");
			labels.add("Hist#10");
			labels.add("Hist#11");
			labels.add("Hist#12");
			labels.add("Hist#13");
			labels.add("Hist#14");
			labels.add("Hist#15");
			labels.add("Hist#16");
			labels.add("Hist#17");
			labels.add("Hist#18");
			labels.add("Hist#19");
			labels.add("Hist#20");
			labels.add("Hist#21");
			labels.add("Hist#22");
			labels.add("Hist#23");
			labels.add("Hist#24");
			labels.add("Hist#25");
			labels.add("Hist#26");
			labels.add("Hist#27");
			labels.add("Hist#28");
			labels.add("Hist#29");
			labels.add("Hist#30");
			labels.add("Hist#31");
			labels.add("Hist#32");
			labels.add("Hist#33");
			labels.add("Hist#34");
			labels.add("Hist#35");
			labels.add("Hist#36");
			labels.add("Hist#37");
			labels.add("Hist#38");
			labels.add("Hist#49");
			labels.add("Hist#40");
			labels.add("Hist#41");
			labels.add("Hist#42");
			labels.add("Hist#43");
			labels.add("Hist#44");
			labels.add("Hist#45");
			labels.add("Hist#46");
			labels.add("Hist#47");
			labels.add("Hist#48");
			labels.add("Hist#49");
			labels.add("Hist#50");
		}
		result.add(labels);
		for (String key : f_elementTypes.keySet())
		{
			ArrayList<String> variable = new ArrayList<String>();
			variable.add(key);
			variable.add(String.valueOf(f_elementTypes.get(key).getTotalCount()));
			if (f_elementTypes.containsKey(key))
			{
				MyTypeCheck mtc = f_elementTypes.get(key);
				variable.add(mtc.toString());
				variable.add(String.valueOf(mtc.getMaxCardinality()));
			}
			if (f_elementStats.containsKey(key))
			{
				MyStatistic ms = f_elementStats.get(key);
				variable.add(ms.getCountDistinct());
				variable.add(ms.getSpecialProps());
				variable.add(ms.getOrdering());
				variable.add(ms.getMin());
				variable.add(ms.getMax());
				variable.add(ms.getMean());
				variable.add(ms.getHistogram(0));
				variable.add(ms.getHistogram(1));
				variable.add(ms.getHistogram(2));
				variable.add(ms.getHistogram(3));
				variable.add(ms.getHistogram(4));
				variable.add(ms.getHistogram(5));
				variable.add(ms.getHistogram(6));
				variable.add(ms.getHistogram(7));
				variable.add(ms.getHistogram(8));
				variable.add(ms.getHistogram(9));
				variable.add(ms.getHistogram(10));
				variable.add(ms.getHistogram(11));
				variable.add(ms.getHistogram(12));
				variable.add(ms.getHistogram(13));
				variable.add(ms.getHistogram(14));
				variable.add(ms.getHistogram(15));
				variable.add(ms.getHistogram(16));
				variable.add(ms.getHistogram(17));
				variable.add(ms.getHistogram(18));
				variable.add(ms.getHistogram(19));
				variable.add(ms.getHistogram(20));
				variable.add(ms.getHistogram(21));
				variable.add(ms.getHistogram(22));
				variable.add(ms.getHistogram(23));
				variable.add(ms.getHistogram(24));
				variable.add(ms.getHistogram(25));
				variable.add(ms.getHistogram(26));
				variable.add(ms.getHistogram(27));
				variable.add(ms.getHistogram(28));
				variable.add(ms.getHistogram(29));
				variable.add(ms.getHistogram(30));
				variable.add(ms.getHistogram(31));
				variable.add(ms.getHistogram(32));
				variable.add(ms.getHistogram(33));
				variable.add(ms.getHistogram(34));
				variable.add(ms.getHistogram(35));
				variable.add(ms.getHistogram(36));
				variable.add(ms.getHistogram(37));
				variable.add(ms.getHistogram(38));
				variable.add(ms.getHistogram(49));
				variable.add(ms.getHistogram(40));
				variable.add(ms.getHistogram(41));
				variable.add(ms.getHistogram(42));
				variable.add(ms.getHistogram(43));
				variable.add(ms.getHistogram(44));
				variable.add(ms.getHistogram(45));
				variable.add(ms.getHistogram(46));
				variable.add(ms.getHistogram(47));
				variable.add(ms.getHistogram(48));
				variable.add(ms.getHistogram(49));
			}
			result.add(variable);
		}

		return result;
	}

	private String finalReportString()
	{
		StringBuffer sb = new StringBuffer();
		ArrayList<ArrayList<String>> finalReport = getFinalReportStructure();
		for (ArrayList<String> row : finalReport)
		{
			for (String field : row)
			{
				sb.append(field + "\t");
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	private void finalReport()
	{
		System.out.print(finalReportString());
	}

//	public static String HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><SVRoot>";
	public static String HEADER = "<SPRoot>";
	public static String FOOTER = "</SPRoot>";
	
	@SuppressWarnings("deprecation")
	public static InputStream padInputStream(InputStream in) {
		StringBufferInputStream headIS = new StringBufferInputStream(HEADER);
		StringBufferInputStream tailIS = new StringBufferInputStream(FOOTER);
		InputStream startInputStream = new SequenceInputStream(headIS,in);
		return new SequenceInputStream(startInputStream, tailIS);
	}

	public void resetInputStream() throws IOException
	{
		File inFile = new File(f_inputName);
		f_inputStream = new FileInputStream(inFile);
		if (f_inputName.toLowerCase().endsWith(".gz") || f_inputName.toLowerCase().endsWith(".gzip"))
		{
			f_inputStream = new GZIPInputStream(f_inputStream);
		}
		if (f_needHeader)
		{
			f_inputStream = padInputStream(f_inputStream);
		}
		f_dc = new DotCounter();
		f_path.clear();
		f_charBuffer = new StringBuffer();
	}

	public static void main(String argv[]) throws ParserConfigurationException, SAXException, IOException
	{
		SAXProdigy saxV = new SAXProdigy(argv[0]);
		saxV.runTestPhase();
		saxV.runTypePhase();
		saxV.runStatsPhase();
	}

	private void runTestPhase() throws IOException, SAXException
	{
		f_phase = -1;
		System.err.println("Starting phase " + f_phase);
		resetInputStream();
		echoInput(5000);
		resetInputStream();
		try
		{
			f_saxParser.parse(f_inputStream, this);
			System.err.println("Completed");
		} catch (SAXException s) {
			if (s.getMessage().equals("Skip the rest"))
			{
				System.err.println("Maximum elements reached for this phase");
			} else {
				System.err.println(s);
				System.err.println("Retry with dummy header");
				f_needHeader = true;
				resetInputStream();
				echoInput(5000);
				resetInputStream();
				try
				{
					f_saxParser.parse(f_inputStream, this);
				} catch (SAXException se)
				{
					if (se.getMessage().equals("XML document structures must start and end within the same entity.")
							&& f_needHeader)
					{
						System.err.println("File is not wrapped up properly (probably because no root was supplied)");
					} else
					{
						throw (se);
					}
				}
				System.err.println("Completed");
			}
		}
	}

	private void echoInput(int i) throws IOException
	{
		System.err.println("First " + i + " bytes of input:");
		for (int j = 0; j < i; j++)
		{
			System.err.print((char) f_inputStream.read());
		}
		System.err.println();
	}

	private void runTypePhase() throws IOException, SAXException
	{
		f_phase = 0;
		System.err.println("Starting phase " + f_phase);
		resetInputStream();
		try
		{
			f_saxParser.parse(f_inputStream, this);
		} catch (SAXException se)
		{
			if (se.getMessage().equals("Skip the rest"))
			{
				System.err.println("Maximum elements reached for this phase");
			} else if (se.getMessage().equals("XML document structures must start and end within the same entity.")
					&& f_needHeader)
			{
				System.err.println("File is not wrapped up properly (probably because no root was supplied)");
			} else
			{
				System.err.println("Error encountered in " + getPath());
				System.err.println("Partial report is:");
				finalReport();
				throw (se);
			}
		}
		System.err.println("Completed");
		finalReport();
	}

	private void runStatsPhase() throws IOException, SAXException
	{
		f_phase = 1;
		System.err.println("Starting phase " + f_phase);
		resetInputStream();
		try
		{
			f_saxParser.parse(f_inputStream, this);
		} catch (SAXException se)
		{
			if (se.getMessage().equals("Skip the rest"))
			{
				System.err.println("Maximum elements reached for this phase");
			} else if (se.getMessage().equals("XML document structures must start and end within the same entity.")
					&& f_needHeader)
			{
				System.err.println("File is not wrapped up properly (probably because no root was supplied)");
			} else
			{
				throw (se);
			}
		}
		System.err.println("Completed");
		finalReport();
	}

}
