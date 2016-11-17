package com.zielund.research.schemawizard;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StreamTokenizer;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

/**
 * A command-line application to organize and lead a user through profiling a tabular data file. This class is responsible for most of the GUI
 */
public class SchemaWizardText
{
	private static final int tabFileProps = 1;
	private static final int tabColumnProps = 2;
	private static final int tabColumnStats = 3;
	private static final int tabFinish = 4;
	int tabStateStatus = tabFileProps;
	String txtInfile;
	boolean optDelimited = false;
	boolean optDetectTypes = true;
	// Fields I added
	BufferedReader brInFile;
	StreamTokenizer stInFile;
	int recNum = 0;
	int numFields = 0;
	int numChars = 0;
	int[] colWidths;
	int numRecsGTNumFields = 0;
	int numRecsLTNumFields = 0;
	int numFieldTypeErrors = 0;
	int numBadRecs = 0;
	RecordSplitter recSplit = new RecordSplitter();
	MyMetaTableModel reviewMetaModel = new MyMetaTableModel();
	MyTableModel reviewModel = new MyTableModel();
	MyStatsTableModel statsModel = new MyStatsTableModel();
	boolean optTitleRow = true;
	boolean optFilterShortRecords = true;
	boolean optFilterLongRecords = true;
	boolean optFilterBadTypeRecords = true;
	String txtKDEFile = "";
	boolean optKDEFile = true;
	boolean optSQLFile = true;
	String txtSQLFile = new String();
	boolean optGoodFile = true;
	String txtGoodFile = new String();
	boolean optBadFile = true;
	String txtBadFile = new String();
	String txtTabStops = new String();
	Process p = null;
	int timerKind = 0;
	boolean optStripQuotes = true;
	boolean optQuoteStrings = false;
	// GetDelimChar getDelimChar2 = new GetDelimChar();
	RecordSplitter recSplit2 = new RecordSplitter();
	boolean handEditedQuote = false;
	boolean optGoodDatabase = false;
	String txtJDBCString = new String();
	String txtJDBCUserid = new String();
	String txtTableName = new String();
	String txtJDBCPassword = new String();
	String txtJDBCDriver = new String();
	boolean optSQLTableOverwrite = false;
	boolean optSQLDatabase = false;
	boolean optTrim = true;
	String txtDelimChar = new String();
	String txtDelimChar2 = new String();
	ArrayList firstLines = new ArrayList();
	String txtDateReformat = new String();
	boolean optReformatDates = false;

	// Construct the frame
	public SchemaWizardText()
	{
		readConfiguration();
	}

	/**
	 * Reads some default parameter settings from SchemaWiz.properties which it assumes is in c:/bin/
	 */
	private void readConfiguration()
	{
		File configFile = new File("c:/bin/SchemaWiz.properties");
		if (configFile.exists())
		{
			try
			{
				FileInputStream fis = new FileInputStream(configFile);
				Properties config = new Properties();
				config.load(fis);
				if (config.containsKey("jdbcDriver"))
					txtJDBCDriver=(config.getProperty("jdbcDriver"));
				if (config.containsKey("jdbcURL"))
					txtJDBCString=(config.getProperty("jdbcURL"));
				if (config.containsKey("jdbcUser"))
					txtJDBCUserid=(config.getProperty("jdbcUser"));
				if (config.containsKey("jdbcPassword"))
					txtJDBCPassword=(config.getProperty("jdbcPassword"));
				fis.close();
			} catch (IOException ioe)
			{
				ioe.printStackTrace();
				setStatus("Error reading configuration: " + ioe.getMessage());
			}
		}
	}

	/**
	 * Starts the file scan over from the beginning
	 * 
	 * @return a new StreamTokenizer that will feed the file to internal consumers one line-token at a time.
	 * @throws FileNotFoundException
	 */
	public StreamTokenizer stInFileReset() throws FileNotFoundException
	{
		numFieldTypeErrors = 0;
		numRecsGTNumFields = 0;
		numRecsLTNumFields = 0;
		if (p != null)
		{
			p.destroy();
		}
		;
		String source = txtInfile;
		String message = "Doesn't work yet";
		char[] msg = message.toCharArray();
		BufferedReader br = new BufferedReader(new CharArrayReader(msg));
		if (source.endsWith("|"))
		{
			source = source.substring(0, source.length() - 1);
			Runtime rt = Runtime.getRuntime();
			try
			{
				p = rt.exec(source);
				br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			} catch (IOException ioe)
			{
				ioe.printStackTrace();
			}
			 } else if (source.toUpperCase().endsWith(".GZ")) {
			 try {
			 File srcFile = new File (source);
			 br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(srcFile))));
			 } catch (IOException ioe) {
			 ioe.printStackTrace();
			 }
		} else
		{
			try
			{
				File srcFile = new File(source);
				br = new BufferedReader(new FileReader(srcFile));
			} catch (FileNotFoundException fnf)
			{
				fnf.printStackTrace();
			}
		}
		StreamTokenizer stInFile = new StreamTokenizer(br);
		stInFile.resetSyntax();
		stInFile.wordChars('\u0000', '\u00FF');
		stInFile.whitespaceChars(10, 13);
		stInFile.eolIsSignificant(false);
		return stInFile;
	}

	/**
	 * Changes the status pane message at the bottom of the GUI
	 * 
	 * @param message
	 *            is what it's set to
	 */
	public void setStatus(String message)
	{
		System.err.println(message);
	}

	/* // ******************************* // */
	/* // ******* PREVIEW SECTION ******* // */
	/* // ******************************* // */

	/**
	 * Checks whether the number of parsed items in input matches the expected number of fields (tempered perhaps by optional trailing_nullcols type behavior)
	 * 
	 * @return true if record has an unacceptable number of fields
	 */
	public boolean isDelimError(String[] dt)
	{
		return isDelimError(dt.length);
	}

	/**
	 * Checks whether the number (representing number of fields on an input line) matches the expected number of fields (tempered perhaps by optional trailing_nullcols type
	 * behavior)
	 * 
	 * @return true if nf is an unacceptable number of fields
	 */
	public boolean isDelimError(int nf)
	{
		boolean b = false;
		if (nf != numFields)
		{
			if (nf > numFields && (optFilterLongRecords || tabStateStatus == tabFileProps))
			{
				b = true;
				numRecsGTNumFields++;
			} else if (nf < numFields && (optFilterShortRecords || tabStateStatus == tabFileProps))
			{
				b = true;
				numRecsLTNumFields++;
			}
		}
		return b;
	}

	/**
	 * Updates the preview (step 1) wizard view with the current status of preview file analysis (that is, how many records so far, how many fields, and how many records with
	 * delimiter errors)
	 */
	private void preview_status()
	{
		String s;
		s = "Preview: " + recNum + " records, " + numFields + " fields";
		if (numRecsLTNumFields > 0)
		{
			s += " (" + numRecsLTNumFields + " Short";
			if (optFilterShortRecords)
			{
				s += " X)";
			} else
			{
				s += ")";
			}
		}
		if (numRecsGTNumFields > 0)
		{
			s += " (" + numRecsGTNumFields + " Long";
			if (optFilterLongRecords)
			{
				s += " X)";
			} else
			{
				s += ")";
			}
		}
		setStatus(s);
	}

	/**
	 * Starts the preview (step 1) wizard view and background analysis
	 */
	public void start_preview()
	{
		try
		{
			stInFile = stInFileReset();
			firstLines = new ArrayList(10);
			CharArrayWriter data1 = new CharArrayWriter();
			BufferedWriter data = new BufferedWriter(data1);
			data.write("<html><head></head><body><pre>");
			while (stInFile.lineno() < 10 && (stInFile.nextToken() != stInFile.TT_EOF))
			{
				String s = stInFile.sval;
				data.write(s);
				data.newLine();
				firstLines.add(s);
			}
			data.write("</pre></body></html>");
			data.flush();
//			txtFileContent.setText(data1.toString());
			if (recSplit.autoConfigure(firstLines))
			{
				showStuff();
			}
			short_preview();
		} catch (IOException ioe)
		{
			setStatus("Error opening " + txtInfile);
		}
	}

	/**
	 * Updates the GUI with what has been learned in the early stage of preview analysis. That is, what has been guessed from the first 10 records in the file
	 */
	void showStuff()
	{
		if (recSplit.isDelimited())
		{
			txtDelimChar=(recSplit.getDelimCharName());
			optDelimited =(true);
		} else
		{
			txtTabStops=(recSplit.getTabStopString());
			optDelimited = false;
		}
		optQuoteStrings = (recSplit.isQuoteEnclosed());
		optTrim = (recSplit.isTrimBlanks());
		optTitleRow = recSplit.isLabeled();
		try
		{
			CharArrayWriter data1 = new CharArrayWriter();
			BufferedWriter data = new BufferedWriter(data1);
			data.write("<pre>");
			for (int line = 0; line < firstLines.size(); line++)
			{
				if (recSplit.isLabeled() && line == 0)
				{
					data.write("<i>");
				}
				String s = (String) firstLines.get(line);
				String[] stuff = recSplit.parseString(s, true);
				if (line == 0)
				{
					numFields = stuff.length;
				}
				int i;
				for (i = 0; i < stuff.length; i++)
				{
					if (i % 2 == 0)
					{
						data.write("<b>");
					}
					data.write(stuff[i]);
					if (i % 2 == 0)
					{
						data.write("</b>");
					}
					if (recSplit.isDelimited() && i < stuff.length - 1)
					{
						data.write("<i>" + recSplit.getDelimStr() + "</i>");
					}
				}
				if (recSplit.isLabeled() && line == 0)
				{
					data.write("</i>");
				}
				data.newLine();
			}
			data.write("</pre>");
			data.flush();
//			txtFileContent.setText(data1.toString());
		} catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
	}

	/**
	 * Get the first 10 records of the file and make some guesses about parameters of the file preview (step 1) wizard
	 */
	void short_preview()
	{
		try
		{
			stInFile = stInFileReset();
			while (stInFile.lineno() < 10000 && (stInFile.nextToken() != stInFile.TT_EOF))
			{
				String s = stInFile.sval;
				int nf = recSplit.countTokens(s);
				if (stInFile.lineno() == 1)
				{
					numFields = nf;
				}
				if (nf != numFields)
				{
					isDelimError(nf); // to maintain error count
				}
			}
			recNum = stInFile.lineno();
			preview_status();
			timerKind = 1;
//			timerContinuing.start();
		} catch (IOException e)
		{
			setStatus("Error opening " + txtInfile);
		}
	}

	/**
	 * Compute one chunk of file preview analysis (about 10000 records). This method is called repeatedly until the file is complete
	 */
	private void continuingPreview()
	{
		try
		{
			if (tabStateStatus == tabFileProps)
			{
				int rnum = 0;
				String dc = recSplit.getDelimStr();
				long started = new Date().getTime() + 1000;
				while (rnum < 10000 && new Date().getTime() < started && (stInFile.nextToken() != stInFile.TT_EOF))
				{
					String s = stInFile.sval;
					int nf = recSplit.countTokens(s);
					isDelimError(nf);
					rnum++;
					if (nf > numFields)
					{
						numRecsGTNumFields++;
						if (numRecsGTNumFields < 100)
						{
							// txtFileContent.setText(txtFileContent.getText() + "<pre>\n...Long record " + stInFile.lineno() + ":\n" + s + "\n</pre>");
						}
					} else if (nf < numFields)
					{
						numRecsLTNumFields++;
						if (numRecsLTNumFields < 100)
						{
							// txtFileContent.setText(txtFileContent.getText() + "<pre>\n...Short record " + stInFile.lineno() + ":\n" + s + "\n</pre>");
						}
					}
				}
				recNum = recNum + rnum;
				preview_status();
				stInFile.pushBack();
				if (stInFile.nextToken() != stInFile.TT_EOF)
				{
					timerKind = 1;
//					timerContinuing.start();
				} else
				{
					preview_status();
					setStatus("--Concluded");
				}
			} else
			{
				preview_status();
				setStatus("--Stopped");
			}
		} catch (IOException e)
		{
			setStatus("Error reading " + txtInfile);
		}
	}

	/* // ******************************* // */
	/* // ******** REVIEW SECTION ******* // */
	/* // ******************************* // */

	/**
	 * Updates the review (step 2) wizard view with the current status of review file analysis (that is, how many column type errors so far)
	 */
	private void review_status()
	{
		setStatus("Review: " + recNum + " records, " + numFields + " fields");
		if (numFieldTypeErrors > 0)
		{
			setStatus(" (" + numFieldTypeErrors + " type errors)");
		}
	}

	public boolean isTypeError(String[] dt, boolean resize)
	{
		boolean b = false;
		if (optFilterBadTypeRecords)
		{
			for (int i = 0; !b && i < dt.length; i++)
			{
				if (!reviewMetaModel.testType(dt[i], i, resize))
				{
					b = true;
					numFieldTypeErrors++;
				}
			}
		}
		return b;
	}

	void short_Review()
	{
		try
		{
			stInFile = stInFileReset();
			String dc = recSplit.getDelimStr();
			reviewMetaModel.clear();
			reviewModel.clear();
			for (int i = 0; i < numFields; i++)
			{
				MyTypeCheck mytype = new MyTypeCheck();
				mytype.setGivenName("Column " + i);
				reviewMetaModel.addField(mytype);
			}
			reviewModel.setColumnCount(numFields + 1);
			if (optTitleRow)
			{
				if (stInFile.nextToken() != stInFile.TT_EOF)
				{
					String s = stInFile.sval;
					String[] dt = recSplit.parseString(s);
					if (isDelimError(dt))
					{
						throw new Exception("Something wrong with numFields");
					}
					int i;
					for (i = 0; i < numFields; i++)
					{
						if (i < dt.length)
						{
							reviewMetaModel.setGivenName(dt[i], i);
						}
					}
				}
			}
			recNum = 0;
			while (recNum < 10 && (stInFile.nextToken() != stInFile.TT_EOF))
			{
				String s = stInFile.sval;
				String[] dt = recSplit.parseString(s);
				if (isDelimError(dt))
				{
					continue;
				}
				recNum++;
				int i;
				reviewModel.addValue("Record# " + stInFile.lineno());
				for (i = 0; i < numFields; i++)
				{
					if (i >= dt.length)
					{
						reviewModel.addValue("<null>");
					} else
					{
						String thisToken;
						thisToken = dt[i];
						reviewModel.addValue(thisToken);
						reviewMetaModel.setCheckAuto(thisToken, i, true);
					}
				}
			}
			reviewModel.addValue("...");
			for (int i = 0; i < numFields; i++)
			{
				reviewModel.addValue("");
				int width = ((MyTypeCheck) reviewMetaModel.getField(i)).getMaxSize();
				if (width < 5)
				{
					width = 5;
				}
			}
			reviewMetaModel.fireTableDataChanged();
			reviewModel.fireTableDataChanged();
			reviewMetaModel.resetErrorStats();
			numFieldTypeErrors = 0;
			review_status();
			timerKind = 2;
//			timerContinuing.start();
		} catch (IOException e)
		{
			setStatus("Error opening " + txtInfile);
		} catch (Exception e)
		{
			e.printStackTrace();
			setStatus(e.getMessage());
		}
	}

	private void continuingReview()
	{
		try
		{
			if (tabStateStatus == tabColumnProps)
			{
				if (stInFile.nextToken() == stInFile.TT_EOF)
				{
					review_status();
					setStatus("--Concluded");
				} else
				{
					long started = new Date().getTime() + 1000;
					stInFile.pushBack();
					int rnum = 0;
					String dc = recSplit.getDelimStr();
					while (rnum < 10000 && new Date().getTime() < started && (stInFile.nextToken() != stInFile.TT_EOF))
					{
						boolean errThisRecord = false;
						String s = stInFile.sval;
						String[] dt = recSplit.parseString(s);
						if (isDelimError(dt))
						{
							continue;
						}
						rnum++;
						int i;
						for (i = 0; i < numFields; i++)
						{
							if (i >= dt.length)
							{
							} else
							{
								if (reviewMetaModel.setCheckAuto(dt[i], i, optDetectTypes))
								{
									numFieldTypeErrors++;
									errThisRecord = true;
								}
							}
						}
						if (errThisRecord)
						{
							reviewModel.addValue("Record# " + stInFile.lineno());
							for (i = 0; i < numFields; i++)
							{
								if (i >= dt.length)
								{
									reviewModel.addValue("<null>");
								} else
								{
									reviewModel.addValue(dt[i]);
								}
							}
							reviewModel.fireTableDataChanged();
							reviewMetaModel.fireTableDataChanged();
						}
					}
					recNum = recNum + rnum;
					review_status();
					stInFile.pushBack();
					if (stInFile.nextToken() != stInFile.TT_EOF)
					{
						timerKind = 2;
//						timerContinuing.start();
					} else
					{
						review_status();
						setStatus("--Concluded");
					}
				}
			} else
			{
				review_status();
				setStatus("--Ended");
			}
		} catch (IOException e)
		{
			setStatus("Error reading " + txtInfile);
		}
	}

	/* // ******************************* // */
	/* // ********* STATS SECTION ******* // */
	/* // ******************************* // */

	private void stats_status()
	{
		setStatus("Statistics: " + recNum + " records, " + numFields + " fields");
	}

	void short_Stats()
	{
		try
		{
			int lookie;
			stInFile = stInFileReset();
			statsModel.clear();
			for (int i = 0; i < numFields; i++)
			{
				MyTypeCheck mtc = reviewMetaModel.getField(i);
				statsModel.addField(mtc);
				// int width = mtc.getMaxSize();
				// if (width < 5) {width = 5;}
				// statsModel.getco.getColumnModel().getColumn(i+1).setPreferredWidth(width*10);
			}
			if (optTitleRow)
			{
				stInFile.nextToken();
			}
			statsModel.setLockDisplay(true);
			int rnum = 0;
			while (rnum < 10 && (stInFile.nextToken() != stInFile.TT_EOF))
			{
				String s = stInFile.sval;
				String[] dt = recSplit.parseString(s);
				if (isDelimError(dt))
				{
					continue;
				}
				if (isTypeError(dt, false))
				{
					continue;
				}
				rnum++;
				for (int i = 0; i < numFields; i++)
				{
					if (i > dt.length)
					{
						statsModel.addValue("", i);
					} else
					{
						statsModel.addValue(dt[i], i);
					}
				}
			}
			statsModel.setLockDisplay(false);
			recNum = rnum;
			stats_status();
			timerKind = 3;
//			timerContinuing.start();
		} catch (IOException e)
		{
			setStatus("Error opening " + txtInfile);
			reviewMetaModel.setLockDisplay(false);
		}
	}

	private void continuingStats()
	{
		try
		{
			long started = new Date().getTime() + 1000;
			if (tabStateStatus == tabColumnStats)
			{
				if (stInFile.nextToken() == stInFile.TT_EOF)
				{
					statsModel.finish();
					stats_status();
					setStatus("--Concluded");
				} else
				{
					stInFile.pushBack();
					int rnum = 0;
					statsModel.setLockDisplay(true);
					while (rnum < 10000 && new Date().getTime() < started && (stInFile.nextToken() != stInFile.TT_EOF))
					{
						try
						{
							String s = stInFile.sval;
							String[] dt = recSplit.parseString(s);
							if (isDelimError(dt))
							{
								continue;
							}
							if (isTypeError(dt, false))
							{
								continue;
							}
							rnum++;
							int i;
							for (i = 0; i < numFields; i++)
							{
								if (i >= s.length())
								{
									statsModel.addValue("", i);
								} else
								{
									statsModel.addValue(dt[i], i);
								}
							}
						} catch (OutOfMemoryError oome)
						{
							System.err.println("Caught an OutOfMemoryError.  One record may be (partially) lost");
							statsModel.useLessMem();
						}
					}
					statsModel.setLockDisplay(false);
					recNum = recNum + rnum;
					stats_status();
					stInFile.pushBack();
					if (stInFile.nextToken() != stInFile.TT_EOF)
					{
						timerKind = 3;
						statsModel.checkMem();
//						timerContinuing.start();
					} else
					{
						statsModel.finish();
						stats_status();
						setStatus("--Concluded");
					}
				}
			} else
			{
				stats_status();
				setStatus("--Concluded");
			}
		} catch (IOException e)
		{
			setStatus("Error reading " + txtInfile);
			statsModel.setLockDisplay(false);
		}
	}

	/* // ******************************* // */
	/* // ******* OTHER STUFF *********** // */
	/* // ******************************* // */

	public void prepare_Finish()
	{
		if (recSplit.isDelimited())
		{
			recSplit2.setDelimChar(recSplit.getDelimChar());
		} else
		{
			recSplit2.setDelimChar('\t');
		}
		txtDelimChar2=(txtDelimChar);
		optQuoteStrings = (optStripQuotes);
		String fn = txtInfile;
		File fInFile = new File(fn);
		String tn = fInFile.getName();
		String path = fInFile.getAbsolutePath();
		while (tn.indexOf('.') > -1)
		{
			tn = tn.substring(0, tn.indexOf('.'));
		}
		while (tn.indexOf('-') > -1)
		{
			tn = tn.substring(0, tn.indexOf('-')) + '_' + tn.substring(tn.indexOf('-') + 1);
		}
		txtTableName=(tn);
		txtKDEFile = (path + ".ctl");
		txtSQLFile=(path + ".sql");
		txtBadFile=(path + ".bad");
		txtGoodFile=(path + ".good");
	}

	public void finish_status()
	{
		setStatus("Finish: " + recNum + " records, " + numFields + " fields");
		if (numFieldTypeErrors > 0)
		{
			setStatus(" (" + numBadRecs + " bad recs)");
		}
	}

	public void start_Finish()
	{
		long magnitude = 10;
		if (optKDEFile)
		{
			setStatus("Writing Oracle SQL Loader control file, " + txtKDEFile);
			reviewMetaModel.writeSQLLDRFile(txtKDEFile, txtTableName, recSplit);
			setStatus("Writing Oracle SQL Loader control file, " + txtKDEFile + "... Finished");
		}
		if (optSQLFile || optGoodDatabase)
		{
			try
			{
				StringWriter strSQL = new StringWriter();
				setStatus("Writing SQL Schema file, " + txtSQLFile);
				// reviewMetaModel.writeSQL();
				BufferedWriter bw1 = new BufferedWriter(new PrintWriter(strSQL));
				bw1.write("-- Created by KDE Schema Wizard\n");
				// bw1.write("drop table " + txtTableName.getText() + ";\n");
				bw1.write("create table " + txtTableName + " (\n");
				for (int i = 0; i < numFields; i++)
				{
					MyTypeCheck mtc = (MyTypeCheck) reviewMetaModel.getField(i);
					bw1.write("\t" + mtc.getTrueName() + " ");
					bw1.write(mtc.getSQLType(txtJDBCDriver));
					bw1.write(mtc.getSQLSize());
					if (i < numFields - 1)
					{
						bw1.write(",");
					}
					bw1.newLine();
				}
				bw1.write(")");
				bw1.flush();
				bw1.close();
				if (optSQLFile)
				{
					bw1 = new BufferedWriter(new FileWriter(txtSQLFile));
					bw1.write(strSQL.toString());
					bw1.flush();
					bw1.close();
					System.err.println("SQL Written to file " + txtSQLFile);
				}
				if (optSQLDatabase)
				{
					try
					{
						Class.forName(txtJDBCDriver);
						Connection myDatabaseConnection = DriverManager.getConnection(txtJDBCString, txtJDBCUserid, new String(txtJDBCPassword));
						Statement s = myDatabaseConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
						if (optSQLTableOverwrite)
						{
							try
							{
								System.err.println("Dropping table in database");
								s.execute("drop table " + txtTableName);
							} catch (SQLException se)
							{
								// ignore
								System.err.println(se.toString());
								System.err.println("--drop was probably not needed");
							}
						}
						System.err.println("Creating table in database");
						s.execute(strSQL.toString());
					} catch (SQLException se)
					{
						System.err.println(strSQL.toString());
						setStatus("SQL Exception: " + se.toString());
						se.printStackTrace();
						return;
					} catch (ClassNotFoundException cnf)
					{
						setStatus("Class not found exception: " + cnf.toString());
						cnf.printStackTrace();
						return;
					}
				}
				setStatus("Writing SQL Schema file, " + txtKDEFile + "... Finished");
			} catch (IOException i)
			{
				i.printStackTrace();
			}
		}
		if (optGoodFile || optBadFile || optGoodDatabase)
		{
			try
			{
				// ProgressMonitor pm = new ProgressMonitor (this,"Exporting Data", "100's", 1, 100);
				recSplit2.setQuoteEnclosed(optQuoteStrings);
				ArrayList dateFieldList = new ArrayList();
				SimpleDateFormat sdr = new SimpleDateFormat(txtDateReformat);
				if (optReformatDates)
				{
					MyTypeCheck mtc;
					for (int i = 0; i < numFields; i++)
					{
						mtc = reviewMetaModel.getField(i);
						if (mtc.isDate())
						{
							dateFieldList.add(new Integer(i));
						}
					}
				}
				boolean changed = dateFieldList.size() > 0
						|| (!(recSplit.getDelimStr().equals(recSplit2.getDelimStr()) && recSplit.isQuoteEnclosed() == recSplit2.isQuoteEnclosed()));
				stInFile = stInFileReset();
				BufferedWriter bwb = new BufferedWriter(new StringWriter(0));
				BufferedWriter bwg = new BufferedWriter(new StringWriter(0));
				Connection myDatabaseConnection = null;
				Statement stmt = null;
				ResultSet rs = null;
				if (optGoodFile)
				{
					bwg = new BufferedWriter(new PrintWriter(new FileWriter(txtGoodFile)));
				}
				if (optBadFile)
				{
					bwb = new BufferedWriter(new PrintWriter(new FileWriter(txtBadFile)));
				}
				if (optGoodDatabase)
				{
					String strSQL = "Select ";
					try
					{
						String delim = " ";
						for (int i = 0; i < numFields; i++)
						{
							MyTypeCheck mtc = (MyTypeCheck) reviewMetaModel.getField(i);
							strSQL += (delim + mtc.getTrueName());
							delim = ", ";
						}
						strSQL += " from " + txtTableName;
						Class.forName(txtJDBCDriver);
						myDatabaseConnection = DriverManager.getConnection(txtJDBCString, txtJDBCUserid, (txtJDBCPassword));
						stmt = myDatabaseConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
						rs = stmt.executeQuery(strSQL);
						rs.moveToInsertRow();
					} catch (ClassNotFoundException cnf)
					{
						setStatus(cnf.toString());
						cnf.printStackTrace();
						return;
					} catch (SQLException se)
					{
						setStatus(se.toString());
						System.err.println(strSQL);
						se.printStackTrace();
						return;
					}
				}
				numBadRecs = 0;
				recNum = 0;
				System.err.print("1");
				while ((stInFile.nextToken() != stInFile.TT_EOF))
				{
					String s = stInFile.sval;
					String[] dt = recSplit.parseString(s);
					if (isDelimError(dt) || isTypeError(dt, false))
					{
						if (optBadFile)
						{
							if (changed)
							{
								s = recSplit2.joinDelimitedArray(dt);
							}
							bwb.write(s);
							bwb.newLine();
							bwb.flush();
						}
						numBadRecs++;
					} else
					{
						if (optGoodFile)
						{
							if (dateFieldList.size() > 0)
							{
								Iterator iter = dateFieldList.iterator();
								while (iter.hasNext())
								{
									int f = ((Integer) iter.next()).intValue();
									String df = dt[f];
									Date dd = (Date) reviewMetaModel.getField(f).parse(df);
									if (dd == null)
									{
										// nothing; output is blank, as before
									} else
									{
										dt[f] = sdr.format(dd);
									}
								}
							}
							if (changed)
							{
								s = recSplit2.joinDelimitedArray(dt);
							}
							bwg.write(s);
							bwg.newLine();
						}
						if (optGoodDatabase && rs != null)
						{
							try
							{
								rs.moveToInsertRow(); // moves cursor to the insert row
								for (int i = 0; i < numFields; i++)
								{
									MyTypeCheck mtc = (MyTypeCheck) reviewMetaModel.getField(i);
									Object parsed = mtc.parse(dt[i]);
									if (parsed != null)
									{
										if (mtc.isString())
										{
											String p = dt[i];
											if (p.length() > mtc.getMaxSize())
											{
												p = p.substring(0, mtc.getMaxSize());
												System.err.println("Warning, String truncated Field " + i + " record " + recNum);
											}
											rs.updateString(i + 1, p);
										} else if (mtc.isInteger())
										{
											rs.updateInt(i + 1, ((Integer) parsed).intValue());
										} else if (mtc.isDouble())
										{
											rs.updateDouble(i + 1, ((Double) parsed).doubleValue());
										} else if (mtc.isDate())
										{
											rs.updateDate(i + 1, new java.sql.Date(((Date) parsed).getTime()));
										}
									} // else do nothing, which leaves rs[i] null
								}
								rs.insertRow();
							} catch (SQLException sqe)
							{
								if (optBadFile)
								{
									if (changed)
									{
										s = recSplit2.joinDelimitedArray(dt);
									}
									bwb.write(s);
									bwb.newLine();
								}
								numBadRecs++;
								System.err.println("SQL Error record " + recNum + ":" + sqe);
							}
						}
						recNum++;
					}
					if (recNum > magnitude)
					{
						System.err.print("0");
						magnitude *= 10;
					}
					if (recNum % 1000 == 0)
					{
						finish_status();
					}
				}

				if (optBadFile)
				{
					bwb.flush();
					bwb.close();
				}
				if (optGoodFile)
				{
					bwg.flush();
					bwb.close();
				}
				if (optGoodDatabase && stmt != null)
				{
					try
					{
						stmt.close();
					} catch (SQLException sqe)
					{
						setStatus("SQL Exception: " + sqe.toString());
						sqe.printStackTrace();
						return;
					}
				}
				finish_status();
				setStatus(" --Done");
			} catch (FileNotFoundException f)
			{
				setStatus("Error finding good/bad file(s)");
			} catch (IOException i)
			{
				setStatus("Error writing to good/bad file(s)");
			}
		}
	}

	public void startTabAction()
	{
		if (tabStateStatus == tabColumnProps)
		{
			short_Review();
		} else if (tabStateStatus == tabColumnStats)
		{
			short_Stats();
		} else if (tabStateStatus == tabFinish)
		{
			prepare_Finish();
		}
	}

	public void updateFixedWidth()
	{

	}

	// ***********************************************************************
	// Generated Event Code Below
	// ***********************************************************************

	public void timerEvent()
	{
		switch (timerKind)
		{
		case 1:
			continuingPreview();
			break;
		case 2:
			continuingReview();
			break;
		case 3:
			continuingStats();
			break;
		default:
		}
	}

	void cmdChooseDelim_actionPerformed(ActionEvent e)
	{
		String candidates = RecordSplitter.INITIAL_DELIMITER_CANDIDATES;
		try
		{
			stInFile = stInFileReset();
			long rnum = 0;
			findDelimiters(1000, candidates, rnum);
		} catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
	}

	void findDelimiters(long r, String candidates, long rnum)
	{
		try
		{
			while (!candidates.equals("") && rnum < r && (stInFile.nextToken() != stInFile.TT_EOF))
			{
				String s = stInFile.sval;
				String[] dt = recSplit.parseString(s);
				if (isDelimError(dt))
				{
					continue;
				}
				if (isTypeError(dt, true))
				{
					continue;
				}
				rnum++;
				candidates = recSplit.findGoodDelimiter(dt, candidates);
			}
			if (candidates.equals(""))
			{
				setStatus("No candidate delimiters remain after " + rnum + " records");
			} else
			{
				String delims = candidates;
				for (int i = delims.length() - 1; i >= 0; i--)
				{
					delims = delims.substring(0, i) + recSplit2.charToName(delims.substring(i, i + 1) + delims.substring(i + 1));
				}
			}
		} catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
	}

	void optStripQuotes_actionPerformed(ActionEvent e)
	{
		recSplit.setQuoteEnclosed(optStripQuotes);
		handEditedQuote = true;
	}


	void optTrim_actionPerformed(ActionEvent e)
	{
		recSplit.setTrimBlanks(optTrim);
	}


	void optDelimited_actionPerformed(ActionEvent e)
	{
	}

	void cmdPeek_actionPerformed(ActionEvent e)
	{
		if (optDelimited)
		{
			recSplit.setDelimCharName(txtDelimChar);
		} else
		{
			try
			{
				if (txtTabStops.equals(""))
				{
					if (!recSplit.autoConfigureFixed(firstLines, false))
					{
						throw new ParseException("Failed to autoconfigure tab stops", 0);
					}
					txtTabStops=(recSplit.getTabStopString());
				}
				recSplit.setTabStops(txtTabStops);
			} catch (ParseException pe)
			{
				System.err.println(pe);
			}
			txtTabStops=(recSplit.getTabStopString());
		}
		showStuff();
	}


}
