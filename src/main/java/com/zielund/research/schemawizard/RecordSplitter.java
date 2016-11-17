package com.zielund.research.schemawizard;

import java.text.ParseException;
import java.util.*;

@SuppressWarnings("unchecked")
public class RecordSplitter
{
	public static final String[] DC_NAMES =
	{ "TAB", "PIPE", "COMMA", "SPACE" };
	public static final String[] DC_CHARS =
	{ "\t", "|", ",", " " };

	// private static final int IC_UNSET = 0;
	// private static final int IC_TAB = 1;
	// private static final int IC_PIPE = 2;
	// private static final int IC_COMMA = 3;
	// private static final int IC_SPACE = 4;

	/**
	 * Returns a printable name for common delimiter characters
	 */
	public static String charToName(String c)
	{
		for (int i = 0; i < DC_CHARS.length; i++)
		{
			if (c.equals(DC_CHARS[i]))
			{
				return DC_NAMES[i];
			}
		} // else
		return (c);
	}

	/**
	 * Returns the character described by the common delimiter char name
	 */
	public static String nameToChar(String s)
	{
		s = s.toUpperCase();
		for (int i = 0; i < DC_CHARS.length; i++)
		{
			if (s.equals(DC_NAMES[i]))
			{
				return DC_CHARS[i];
			}
		} // else
		return (s.substring(0, 1));
	}

	public static String[] parseDelimitedString(String s, char delim, boolean quoted, boolean trim)
	{
		RecordSplitter rs = new RecordSplitter();
		rs.setDelimChar(delim);
		rs.setQuoteEnclosed(quoted);
		rs.setTrimBlanks(trim);
		return (rs.parseDelimitedString(s, false));
	}

	/* **********************************************
	 * Generic Methods & Properties (and constructor) **********************************************
	 */

	public RecordSplitter()
	{
	}

	private boolean isDelimited = true;

	public boolean isDelimited()
	{
		return isDelimited;
	}

	public void setToDelimited()
	{
		isDelimited = true;
	}

	public boolean isFixed()
	{
		return !isDelimited;
	}

	public void setToFixed()
	{
		isDelimited = false;
	}

	private boolean trimBlanks = false;

	public boolean isTrimBlanks()
	{
		return trimBlanks;
	}

	public void setTrimBlanks(boolean value)
	{
		trimBlanks = value;
	}

	private boolean isLabeled = false;

	public boolean isLabeled()
	{
		return isLabeled;
	}

	public void setLabeled(boolean value)
	{
		isLabeled = value;
	}

	public int countTokens(String s)
	{
		if (isDelimited)
		{
			return countDelimitedTokens(s);
		} else
		{
			return countFixedTokens(s);
		}
	}

	public String[] parseString(String s, boolean ignoreTrimming)
	{
		if (isDelimited)
		{
			return parseDelimitedString(s, ignoreTrimming);
		} else
		{
			return parseFixedString(s, ignoreTrimming);
		}
	}

	public String[] parseString(String s)
	{
		return parseString(s, false);
	}

	public String joinArray(String[] sa)
	{
		if (isDelimited)
		{
			return joinDelimitedArray(sa);
		} else
		{
			return joinFixedArray(sa);
		}
	}

	/**
	 * Attempts to automatically configure the splitter based on the first few lines of input
	 */
	public boolean autoConfigure(ArrayList al)
	{
		for (int i = 0; i < 3; i++)
		{
			if (autoConfigureDelimited(al, DC_CHARS[i]))
			{
				return true;
			}
		}
		if (autoConfigureFixed(al, true))
		{
			return true;
		}
		return (autoConfigureDelimited(al, " "));
	}

	public void autoConfigureFirstRecordLabel(ArrayList al)
	{
		boolean firstRecordAllString = true;
		boolean secondRecordAllString = true;
		if (al.size() < 2)
		{
			return;
		}
		String[] first = parseString((String) al.get(0));
		String[] second = parseString((String) al.get(1));
		for (int i = 0; i < first.length; i++)
		{
			firstRecordAllString &= MyTypeCheck.checkString(first[i]);
		}
		for (int i = 0; i < second.length; i++)
		{
			secondRecordAllString &= MyTypeCheck.checkString(second[i]);
		}
		isLabeled = firstRecordAllString & !secondRecordAllString;
	}

	/* ******************************
	 * Delimited Methods & Properties ******************************
	 */

	public boolean autoConfigureDelimited(ArrayList al, String pdc)
	{
		Iterator iter = al.iterator();
		boolean hasQuotes = false;
		boolean hasMultiSpace = false;
		while (iter.hasNext())
		{
			String s = (String) iter.next();
			if (s.indexOf(pdc) == -1)
			{
				return false;
			}
			hasQuotes |= s.indexOf('"') >= 0 || s.indexOf('\'') >= 0;
			hasMultiSpace |= s.indexOf("  ") > 0;
		}
		setDelimStr(pdc);
		setQuoteEnclosed(hasQuotes);
		setTrimBlanks(hasMultiSpace && !pdc.equals(" "));
		autoConfigureFirstRecordLabel(al);
		return true;
	}

	private boolean quoteEnclosed = false;

	public boolean isQuoteEnclosed()
	{
		return quoteEnclosed;
	}

	public void setQuoteEnclosed(boolean value)
	{
		quoteEnclosed = value;
	}

	private Character delimChar;

	public char getDelimChar()
	{
		if (delimChar == null)
		{
			return '\0';
		} else
		{
			return delimChar.charValue();
		}
	}

	public void setDelimChar(char dc)
	{
		delimChar = new Character(dc);
		setToDelimited();
	}

	public String getDelimStr()
	{
		if (delimChar == null)
		{
			return "";
		} else
		{
			return delimChar.toString();
		}
	}

	public void setDelimStr(String s)
	{
		if (s.length() == 0)
		{
			delimChar = null;
			setToDelimited();
		} else
		{
			setDelimChar(s.charAt(0));
		}
	}

	public void setDelimCharName(String dcn)
	{
		setDelimStr(nameToChar(dcn));
	}

	public String getDelimCharName()
	{
		return charToName(getDelimStr());
	}

	private boolean isQuote(char c)
	{
		return (c == '\'' || c == '"');
	}

	private int countDelimitedTokens(String s)
	{
		if (s == null)
		{
			return 1;
		} else if (!quoteEnclosed)
		{
			int nt = 1;
			char cdc = getDelimChar();
			for (int i = 0; i < s.length(); i++)
			{
				if (s.charAt(i) == cdc)
				{
					nt++;
				}
			}
			return nt;
		} else
		{
			int nt = 1;
			char cdc = getDelimChar();
			int start = 0;
			boolean thisQuoted = isQuote(s.charAt(0));
			if (thisQuoted)
			{
				start++;
			}
			for (int i = start; i < s.length(); i++)
			{
				if (thisQuoted)
				{
					if (s.charAt(i) == cdc && isQuote(s.charAt(i - 1)))
					{
						nt++;
						if (i + 1 < s.length() && isQuote(s.charAt(i + 1)))
						{
							i++;
							thisQuoted = true;
						} else
						{
							thisQuoted = false;
						}
					}
				} else
				{
					if (s.charAt(i) == cdc)
					{
						nt++;
						if (i + 1 < s.length() && isQuote(s.charAt(i + 1)))
						{
							i++;
							thisQuoted = true;
						} else
						{
							thisQuoted = false;
						}
					}
				}
			}
			return nt;
		}
	}

	private String[] parseDelimitedString(String s, boolean ignoreTrimming)
	{
		int cdt = countDelimitedTokens(s);
		String[] ds = new String[cdt];
		if (s == null)
		{
			return ds;
		}
		int current = 0;
		int next;
		String dc = String.valueOf(getDelimChar());
		boolean thisQuoted = quoteEnclosed && isQuote(s.charAt(0));
		for (int i = 0; i < cdt - 1; i++)
		{
			next = s.indexOf(dc, current);
//			while (quoteEnclosed && thisQuoted & !isQuote(s.charAt(next - 1)))
//			{
//				next = s.indexOf(dc, next + 1);
//			}
			if (next == current || next == 0)
			{
				ds[i] = "";
			} else
			{
				if (quoteEnclosed && thisQuoted && !ignoreTrimming)
				{
					ds[i] = s.substring(current + 1, next - 1);
				} else
				{
					ds[i] = s.substring(current, next);
				}
				if (trimBlanks && !ignoreTrimming)
				{
					ds[i] = ds[i].trim();
				}
			}
			current = next + 1;
			thisQuoted = (s.length() > current && isQuote(s.charAt(current)));
		}
		if (quoteEnclosed && thisQuoted && !ignoreTrimming)
		{
			ds[cdt - 1] = s.substring(current + 1, s.length() - 1);
		} else
		{
			ds[cdt - 1] = s.substring(current);
		}
		if (trimBlanks && !ignoreTrimming)
		{
			ds[cdt - 1] = ds[cdt - 1].trim();
		}
		return ds;
	}

	public static final String INITIAL_DELIMITER_CANDIDATES = "\t,| ;._-!@#$%^&*~`?:\\+=/";

	public String findGoodDelimiter(String[] sa, String candidates)
	{
		for (int i = 0; i < sa.length; i++)
		{
			for (int j = candidates.length() - 1; j >= 0; j--)
			{
				if (sa[i].indexOf(candidates.charAt(j)) > -1)
				{
					candidates = candidates.substring(0, j) + candidates.substring(j + 1);
				}
			}
		}
		return candidates;
	}

	public String joinDelimitedArray(String[] ds)
	{
		String s = "";
		String delim = "";
		char dc = getDelimChar();
		for (int i = 0; i < ds.length; i++)
		{
			if (quoteEnclosed && ds[i].indexOf(dc) > -1)
			{
				s += delim + "\"" + ds[i] + "\"";
			} else
			{
				s += delim + ds[i];
			}
			delim = "" + dc;
		}
		return s;
	}

	/* ********************************
	 * Fixed-Width Methods & Properties ********************************
	 */

	private int[] tabStops;

	public int[] getTabStops()
	{
		return tabStops;
	}

	public int[] getTabWidths()
	{
		int[] w = new int[tabStops.length];
		int soFar = 0;
		for (int i = 0; i < w.length; i++)
		{
			w[i] = tabStops[i] - soFar;
			soFar = tabStops[i];
		}
		return w;
	}

	private boolean isDigit(char c)
	{
		return Character.isDigit(c);
	}

	private boolean isLetter(char c)
	{
		return Character.isLetter(c);
	}

	private static final int TST_ZERO = 0;
	private static final int TST_DBL_ZERO = 1;
	private static final int TST_LETTER = 2;
	private static final int TST_SPACE = 3;
	private static final int TST_DBL_SPACE_START = 4;
	private static final int TST_DBL_SPACE_END = 5;

	private int nextTabStop(ArrayList lines, int start)
	{
		int tabPossible = -1;
		Iterator iter = lines.iterator();
		while (iter.hasNext())
		{
			String line = (String) iter.next();
			int[] tabs =
			{ -1, -1, -1, -1, -1, -1 };
			for (int i = 0; i < TST_DBL_SPACE_END; i++)
			{
				tabs[i] = nextTabStop(i, line, start);
				if (i == TST_ZERO && tabs[i] == -1)
				{
					i++;
				} // skip next test
				else if (i == TST_SPACE && tabs[i] == -1)
				{
					i++;
					i++;
				} // skip next two tests
				else if (i == TST_DBL_SPACE_START && tabs[i] == -1)
				{
					i++;
				} // skip next test
			}
			// tabs[TST_ZERO] = nextZero(line,start);
			// if (nextZero >= 0)
		}
		return tabPossible;
	}

	private int nextTabStop(int type, String s, int start)
	{
		if (start >= s.length())
		{
			return -1;
		}
		int i = start;
		switch (type)
		{
		case TST_ZERO:
			if (isDigit(s.charAt(i)))
			{
				while (i < s.length() && isDigit(s.charAt(i)))
				{
					i++;
				}
			} else
			{
				while (i < s.length() && !isDigit(s.charAt(i)))
				{
					i++;
				}
			}
			return i;
		case TST_DBL_ZERO:
			while ((i = nextTabStop(TST_ZERO, s, i)) > -1 && s.length() <= i + 1 && s.charAt(i + 1) != ' ')
			{
				// do nothing
			}
			if (s.charAt(i + 1) == ' ')
			{
				return i;
			} else
			{
				return -1;
			}
			// case TST_LETTER:
			// break;
			// case TST_SPACE:
			// break;
			// case TST_DBL_SPACE_START:
			// break;
			// case TST_DBL_SPACE_END:
			// break;
		default:
			return -1;
		}
	}

	private int nextZero(String s, int start)
	{
		return 0;
	}

	private int nextLetter(String s, int start)
	{
		return 0;
	}

	private int nextTabStop(String s, int start)
	{
		int i = s.indexOf("  ", start);
		if (i == -1)
		{
			return i;
		}
		while (i < s.length() && s.charAt(i) == ' ')
		{
			i++;
		}
		return i;
	}

	private int nextWeakTabStop(String s, int start)
	{
		int i = s.indexOf(" ", start);
		if (i == -1)
		{
			return i;
		}
		while (i < s.length() && s.charAt(i) == ' ')
		{
			i++;
		}
		return i;
	}

	private int nextNumericTabStop(String s, int start)
	{
		if (start >= s.length())
		{
			return -1;
		}
		int i = start;
		if (isDigit(s.charAt(i)))
		{
			while (i < s.length() && isDigit(s.charAt(i)))
			{
				i++;
			}
		} else
		{
			while (i < s.length() && !isDigit(s.charAt(i)))
			{
				i++;
			}
		}
		return i;
	}

	/**
	 * Attempts to detect fixed-width file looking only at the first few lines. Fixed width requires <bl> <li>All strings in the supplied list to be the same length</li> <li>
	 * Identify tab-stops in at least 2 places</li> <li>Tab-stops in the same place in at least 3 different records</li> </bl>
	 * 
	 * @param al
	 *            is a list of strings with each string being a record from the source file
	 */
	public boolean autoConfigureFixed(ArrayList al, boolean sizeMatters)
	{
		Iterator iter = al.iterator();
		if (!iter.hasNext())
		{
			return false;
		}
		String first = (String) iter.next();
		int firstLen = first.length();
		Hashtable tabs = new Hashtable();
		Hashtable weakTabs = new Hashtable();
		Hashtable numTabs = new Hashtable();
		boolean hasQuotes = first.indexOf('"') >= 0 || first.indexOf('\'') >= 0;
		int ts = 0;
		while ((ts = nextTabStop(first, ts)) >= 0)
		{
			Integer TS = new Integer(ts);
			Integer V = new Integer(1);
			tabs.put(TS, V);
		}
		ts = 0;
		while ((ts = nextWeakTabStop(first, ts)) >= 0)
		{
			Integer TS = new Integer(ts);
			Integer V = new Integer(1);
			weakTabs.put(TS, V);
		}
		ts = 0;
		while ((ts = nextNumericTabStop(first, ts)) >= 0)
		{
			Integer TS = new Integer(ts);
			Integer V = new Integer(1);
			numTabs.put(TS, V);
		}
		while (iter.hasNext())
		{
			String s = (String) iter.next();
			if (sizeMatters && s.length() != firstLen)
			{
				return false;
			}
			hasQuotes |= first.indexOf('"') >= 0 || first.indexOf('\'') >= 0;
			ts = 0;
			while ((ts = nextTabStop(s, ts)) >= 0)
			{
				Integer TS = new Integer(ts);
				Integer V = new Integer(1);
				if (tabs.containsKey(TS))
				{
					V = new Integer(((Integer) tabs.get(TS)).intValue() + 1);
				}
				tabs.put(TS, V);
			}
			ts = 0;
			while ((ts = nextWeakTabStop(s, ts)) >= 0)
			{
				Integer TS = new Integer(ts);
				Integer V = new Integer(1);
				if (weakTabs.containsKey(TS))
				{
					V = new Integer(((Integer) weakTabs.get(TS)).intValue() + 1);
				}
				weakTabs.put(TS, V);
			}
			ts = 0;
			while ((ts = nextNumericTabStop(s, ts)) >= 0)
			{
				Integer TS = new Integer(ts);
				Integer V = new Integer(1);
				if (numTabs.containsKey(TS))
				{
					V = new Integer(((Integer) numTabs.get(TS)).intValue() + 1);
				}
				numTabs.put(TS, V);
			}
		}
		// Now, to prove that something is a tab stop, it must have a tab-stop on 3 of 10
		Enumeration enumr = tabs.keys();
		while (enumr.hasMoreElements())
		{
			Integer TS = (Integer) enumr.nextElement();
			Integer V = (Integer) tabs.get(TS);
			if (V.intValue() < 3)
			{
				tabs.remove(TS);
			}
		}
		// or weak tabs on 8 of 10
		enumr = weakTabs.keys();
		while (enumr.hasMoreElements())
		{
			Integer TS = (Integer) enumr.nextElement();
			Integer V = (Integer) weakTabs.get(TS);
			if (V.intValue() < 8)
			{
				weakTabs.remove(TS);
			}
		}
		// or numeric tabs on 9 of 10
		enumr = numTabs.keys();
		while (enumr.hasMoreElements())
		{
			Integer TS = (Integer) enumr.nextElement();
			Integer V = (Integer) numTabs.get(TS);
			if (V.intValue() < 9)
			{
				numTabs.remove(TS);
			}
		}
		// now combine them
		enumr = weakTabs.keys();
		while (enumr.hasMoreElements())
		{
			Integer TS = (Integer) enumr.nextElement();
			Integer V = (Integer) weakTabs.get(TS);
			if (!tabs.containsKey(TS))
			{
				tabs.put(TS, V);
			}
		}
		enumr = numTabs.keys();
		while (enumr.hasMoreElements())
		{
			Integer TS = (Integer) enumr.nextElement();
			Integer V = (Integer) numTabs.get(TS);
			if (!tabs.containsKey(TS))
			{
				tabs.put(TS, V);
			}
		}
		if (tabs.size() < 1)
		{
			return false;
		}
		// Now sort them and such
		int tabArr[] = new int[tabs.size()];
		enumr = tabs.keys();
		int i = 0;
		while (enumr.hasMoreElements())
		{
			Integer TS = (Integer) enumr.nextElement();
			Integer V = (Integer) tabs.get(TS);
			tabArr[i] = TS.intValue();
			i++;
		}
		Arrays.sort(tabArr);
		// Now, double check that none of them are completely blank
		// boolean[] allBlank = new boolean[tabArr.length]; for (int k = 0; k < allBlank.length; k++) {allBlank[k] = true;}
		// iter = al.iterator();
		// while (iter.hasNext()) {
		// String s = (String)iter.next();
		// int lastStop = 0;
		// for (int k = 0; k < tabArr.length; k++) {
		// String sx = s.substring(lastStop,tabArr[k]);
		// allBlank[k] &= MyTypeCheck.checkBlank(sx);
		// lastStop = tabArr[k];
		// }
		// }
		// int numBlanks = 0;
		// for (int k=1; k<allBlank.length; k++) {
		// if (allBlank[k]) {numBlanks++;}
		// }
		// if (numBlanks>0) {
		// int[] tabs2 = new int[tabArr.length-numBlanks];
		// int j=0;
		// for (int k=0; k<allBlank.length-1; k++) {
		// if (!allBlank[k+1]) {
		// tabs2[j] = tabArr[k];
		// j++;
		// }
		// }
		// tabArr = new int[tabs2.length];
		// tabArr = tabs2;
		// }
		try
		{
			setTabStops(tabArr, true);
			setTrimBlanks(true);
			setQuoteEnclosed(hasQuotes);
			autoConfigureFirstRecordLabel(al);
			return true;
		} catch (ParseException pe)
		{
			pe.printStackTrace();
			return false;
		}
	}

	public void setTabStops(int[] ts, boolean hasRecLen) throws ParseException
	{
		int totalTab = 0;
		for (int i = 0; i < ts.length; i++)
		{
			if (totalTab > ts[i])
			{
				throw new ParseException("TabStops not ascending: #" + i + "=" + ts[i], i);
			}
			totalTab = ts[i];
		}
		tabStops = ts;
		recordLength = tabStops[tabStops.length - 1];
		setToFixed();
	}

	public String getTabStopString()
	{
		String s = "";
		String delimiter = "";
		for (int i = 0; i < tabStops.length; i++)
		{
			s += delimiter + tabStops[i];
			delimiter = ",";
		}
		if (recordLength > 0)
		{
			s += "!";
		}
		return s;
	}

	public void setTabWidths(int[] tw, boolean hasRecLen) throws ParseException
	{
		int[] ts = new int[tw.length];
		int totalTab = 0;
		for (int i = 0; i < tw.length; i++)
		{
			totalTab += tw[i];
			ts[i] = totalTab;
		}
		setTabStops(ts, hasRecLen);
	}

	public void setTabStops(String s) throws ParseException
	{
		boolean isWidth = false;
		boolean hasRecLen = false;
		if (s.toUpperCase().startsWith("W:"))
		{
			s = s.substring(2);
			isWidth = true;
		}
		if (s.endsWith("!"))
		{
			s = s.substring(0, s.length() - 1);
			hasRecLen = true;
		}
		String[] dt = parseDelimitedString(s, ',', false, false);
		ArrayList tsList = new ArrayList(dt.length);
		int lastStop = 0;
		for (int i = 0; i < dt.length; i++)
		{
			boolean isWidthOnce = false;
			int arrSize = 1;
			if (dt[i].startsWith("W"))
			{
				isWidthOnce = true & !isWidth;
				dt[i] = dt[i].substring(1);
			}
			if (dt[i].endsWith("]"))
			{
				if (!isWidth && !isWidthOnce)
				{
					throw new ParseException("Array syntax requires column width instead of tab-stop", i);
				}
				try
				{
					int startBracket = dt[i].indexOf('[');
					String arrSizeStr = dt[i].substring(startBracket + 1, dt[i].length() - 1);
					arrSize = new Integer(arrSizeStr).intValue();
					dt[i] = dt[i].substring(0, startBracket);
				} catch (Exception e)
				{
					throw new ParseException(e.toString(), i);
				}
				if (arrSize < 1)
				{
					throw new ParseException("Array syntax requires array size >= 1", i);
				}
			}
			try
			{
				Integer thisVal = new Integer(dt[i]);
				if (thisVal.intValue() < 1)
				{
					throw new ParseException("No negative numbers allowed", i);
				}
				if (isWidthOnce)
				{
					for (int j = 0; j < arrSize; j++)
					{
						tsList.add(new Integer(lastStop + thisVal.intValue()));
						lastStop += thisVal.intValue();
					}
				} else if (isWidth)
				{
					for (int j = 0; j < arrSize; j++)
					{
						tsList.add(thisVal);
					}
				} else
				{
					tsList.add(thisVal);
				}
			} catch (NumberFormatException nfe)
			{
				throw new ParseException(nfe.toString(), i);
			}
		}
		int[] ts = new int[tsList.size()];
		for (int i = 0; i < tsList.size(); i++)
		{
			Integer TS = (Integer) tsList.get(i);
			ts[i] = TS.intValue();
		}
		if (isWidth)
		{
			setTabWidths(ts, hasRecLen);
		} else
		{
			setTabStops(ts, hasRecLen);
		}
	}

	private int recordLength = 0;

	public int getRecordLength()
	{
		return recordLength;
	}

	public void setRecordLength(int rl)
	{
		recordLength = rl;
	}

	private int countFixedTokens(String s)
	{
		int tokens = tabStops.length;
		if (recordLength > 0 && s.length() > recordLength)
		{
			return tokens + 1;
		} else
		{
			return tokens;
		}
	}

	private String[] parseFixedString(String s, boolean ignoreTrimming)
	{
		int cft = countFixedTokens(s);
		int tabSoFar = 0;
		String[] pft = new String[cft];
		for (int i = 0; i < tabStops.length; i++)
		{
			if (tabStops[i] >= s.length())
			{
				pft[i] = s.substring(tabSoFar);
				tabSoFar = s.length();
			} else
			{
				pft[i] = s.substring(tabSoFar, tabStops[i]);
			}
			if (isTrimBlanks() && !ignoreTrimming)
			{
				pft[i] = pft[i].trim();
			}
			if (!ignoreTrimming && isQuoteEnclosed() && isQuote(pft[i].charAt(0)) && isQuote(pft[i].charAt(pft[i].length() - 1)) && pft[i].length() > 1)
			{
				pft[i] = pft[i].substring(1, pft[i].length() - 2);
			}
			tabSoFar = tabStops[i];
		}
		if (cft > tabStops.length)
		{
			pft[cft - 1] = s.substring(tabSoFar);
			if (isTrimBlanks())
			{
				pft[cft - 1] = pft[cft - 1].trim();
			}
			if (isQuoteEnclosed() && isQuote(pft[cft - 1].charAt(0)) && isQuote(pft[cft - 1].charAt(pft[cft - 1].length() - 1)) && pft[cft - 1].length() > 1)
			{
				pft[cft - 1] = pft[cft - 1].substring(1, pft[cft - 1].length() - 2);
			}
		}
		return pft;
	}

	private String joinFixedArray(String[] sa)
	{
		String s = "";
		for (int i = 0; i < sa.length; i++)
		{
			if (i < tabStops.length)
			{
				if (sa[i].length() > tabStops[i])
				{
					s += sa[i].substring(0, tabStops[i] + 1);
				} else
				{
					char fill[] = new char[tabStops[i] - s.length() - sa[i].length()];
					Arrays.fill(fill, ' ');
					String f = new String(fill);
					s += sa[i] + f;
				}
			}
		}
		return s;
	}

}
