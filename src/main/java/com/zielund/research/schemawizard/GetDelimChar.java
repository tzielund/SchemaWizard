package com.zielund.research.schemawizard;

import java.awt.*;
import javax.swing.*;

/**
 * A custom JComboBox control providing a field delimiter picker. There are a few special named delimiters (such as TAB) as well as an opportunity to type in a delimiter character.
 * The class also implements the functionality of locating delimiter characters in a string.
 * 
 * @author u0045494
 * 
 */
@SuppressWarnings("unused")
public class GetDelimChar extends JComboBox
{
	BorderLayout borderLayout1 = new BorderLayout();
	private static final int IC_UNSET = 0;
	private static final int IC_TAB = 1;
	private static final int IC_PIPE = 2;
	private static final int IC_COMMA = 3;
	private static final int IC_SPACE = 4;

	private boolean quoteEnclosed = false;
	private boolean trimBlanks = false;

	/**
	 * No argument constructor to create and initialize object
	 */
	public GetDelimChar()
	{
		try
		{
			jbInit();
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	/**
	 * Initializes object by populating with the standard 'named' delimiters
	 * 
	 * @throws Exception
	 */
	private void jbInit() throws Exception
	{
		this.setLayout(borderLayout1);
		this.addItem("");
		this.addItem("TAB");
		this.addItem("PIPE");
		this.addItem("COMMA");
		this.addItem("SPACE");
	}

	/**
	 * Getter for boolean property related to whether the delimiter scan expects fields to be enclosed by leading and closing quotes
	 * 
	 * @return true when the object expects fields to be enclosed in quotes
	 */
	public boolean isQuoteEnclosed()
	{
		return quoteEnclosed;
	}

	/**
	 * Setter for boolean property related to whether the delimiter scan expects fields to be enclosed by leading and closing quotes
	 * 
	 * @param value
	 *            is true if the object should expect fields to be enclosed in quotes
	 */
	public void setQuoteEnclosed(boolean value)
	{
		quoteEnclosed = value;
	}

	/**
	 * Getter for boolean property related to whether the delimiter scan expects fields to require leading/trailing blanks to be trimmed
	 * 
	 * @return true when the object trims fields
	 */
	public boolean isTrimBlanks()
	{
		return trimBlanks;
	}

	/**
	 * Setter for boolean property related to whether the delimiter scan expects fields to require leading/trailing blanks to be trimmed
	 * 
	 * @param value
	 *            is true to indicate the object should trim blanks from fields
	 */
	public void setTrimBlanks(boolean value)
	{
		trimBlanks = value;
	}

	/**
	 * Method to translate a few common delimiter characters to printable name string
	 * 
	 * @param c
	 *            is the actual delimiter character
	 * @return a printable string (sometimes a name, sometimes the original character)
	 */
	public static String charToName(String c)
	{
		if (c.equals("\t"))
		{
			return "TAB";
		}
		if (c.equals("|"))
		{
			return "PIPE";
		}
		if (c.equals(","))
		{
			return "COMMA";
		}
		if (c.equals(" "))
		{
			return "SPACE";
		}
		return (c);
	}

	/**
	 * Method to translate named delimiters to the actual character
	 * 
	 * @param s
	 *            is a printable name of character (such as TAB) or actual character
	 * @return the corresponding single character
	 */
	public static String nameToChar(String s)
	{
		s = s.toUpperCase();
		if (s.equals("TAB"))
		{
			return "\t";
		}
		if (s.equals("PIPE"))
		{
			return "|";
		}
		if (s.equals("COMMA"))
		{
			return ",";
		}
		if (s.equals("SPACE"))
		{
			return " ";
		}
		return (s.substring(0, 1));
	}

	/**
	 * Getter for the actual DC (Delimiter Character) the object expects
	 * 
	 * @return the delimiter as a single character string
	 */
	public String getDC()
	{
		// String s = this.getSelectedItem().toString();
		String s = this.getEditor().getItem().toString();
		int i = this.getSelectedIndex();
		if (i == IC_TAB)
		{
			return "\t";
		}
		if (i == IC_PIPE)
		{
			return "|";
		}
		if (i == IC_COMMA)
		{
			return ",";
		}
		if (i == IC_SPACE)
		{
			return " ";
		}
		return s;
	}

	/**
	 * Setter for DC (Delimiter Character) property using either the given string or a name translation
	 * 
	 * @param s
	 *            provides the delimiter character either by name or as the first character of the string
	 */
	public void setDC(String s)
	{
		setSelectedItem(charToName(s));
		/*
		 * if (s.equals ("\t")) {s = "TAB";} if (s.equals ("|")) {s = "PIPE";} if (s.equals (",")) {s = "COMMA";} if (s.equals (" ")) {s = "SPACE";} setSelectedItem(s);
		 */
	}

	/**
	 * Method to scan a string for instances of DC and return the number of delimited text chunks
	 * 
	 * @param s
	 *            is a string to scan
	 * @return the number of delimited strings in s
	 */
	int countDelimitedTokens(String s)
	{
		String dc = getDC();
		if (dc.length() == 0 || s == null)
		{
			return 1;
		} else if (!quoteEnclosed)
		{
			int nt = 1;
			char cdc = dc.charAt(0);
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
			char cdc = dc.charAt(0);
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

	/**
	 * Checks whether c is a quote character
	 * 
	 * @param c
	 *            char to test
	 * @return true if c is a member of the class of quote characters
	 */
	private boolean isQuote(char c)
	{
		return (c == '\'' || c == '"');
	}

	/**
	 * Parses a given string into delimited chunks, while taking into consideration the quoting and trimming options
	 * 
	 * @param s
	 *            is the string to parse
	 * @return an array of individual string chunks
	 */
	public String[] parseDelimitedString(String s)
	{
		int cdt = countDelimitedTokens(s);
		String[] ds = new String[cdt];
		if (s == null)
		{
			return ds;
		}
		String dc = getDC();
		int current = 0;
		int next;
		boolean thisQuoted = isQuote(s.charAt(0));
		for (int i = 0; i < cdt - 1; i++)
		{
			next = s.indexOf(dc, current);
			while (quoteEnclosed && thisQuoted & !isQuote(s.charAt(next - 1)))
			{
				next = s.indexOf(dc, next + 1);
			}
			if (next == current)
			{
				ds[i] = "";
			} else
			{
				if (quoteEnclosed && thisQuoted)
				{
					ds[i] = s.substring(current + 1, next - 1);
				} else
				{
					ds[i] = s.substring(current, next);
				}
				if (trimBlanks)
				{
					ds[i] = ds[i].trim();
				}
			}
			current = next + 1;
			thisQuoted = (s.length() > current && isQuote(s.charAt(current)));
		}
		if (quoteEnclosed && thisQuoted)
		{
			ds[cdt - 1] = s.substring(current + 1, s.length() - 1);
		} else
		{
			ds[cdt - 1] = s.substring(current);
		}
		if (trimBlanks)
		{
			ds[cdt - 1] = ds[cdt - 1].trim();
		}
		return ds;
	}

	/**
	 * Joins an array of strings into a single string delimited by DC, while taking into account quoting rules
	 * 
	 * @param ds
	 *            is an array of strings
	 * @return a single DC-delimited string
	 */
	public String joinDelimitedArray(String[] ds)
	{
		String s = "";
		String delim = "";
		char dc = getDC().charAt(0);
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

	public static final String INITIAL_DELIMITER_CANDIDATES = "\t,| ;._-!@#$%^&*~`?:\\+=/";
	JPanel jPanel1 = new JPanel();
	JRadioButton optDelimited = new JRadioButton();
	JRadioButton optFixed = new JRadioButton();
	GridBagLayout gridBagLayout1 = new GridBagLayout();
	JComboBox jComboBox1 = new JComboBox();
	JTextField jTextField1 = new JTextField();

	/**
	 * Method to scan a string for good candidate delimiters
	 * 
	 * @param sa
	 *            is a string to scan
	 * @param candidates
	 *            is a set of candidate delimiters as a string of characters
	 * @return new candidates as a set of characters in string of character form
	 */
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

}
