package com.zielund.research.schemawizard;

import javax.swing.UIManager;

/**
 * This application is a GUI wizard that guides a user through the analysis
 * or profiling of a tabular data file.  The user simply supplies the pathname
 * of the file to profile as the command-line parameter, and the wizard interface
 * requests all additional information.
 * 
 * The application can handle many variations on tabular data files including files
 * delimited by many different individual characters or of fixed width fields.  It makes
 * an attempt to guess whether the file is delimited or fixed width by examining the first
 * 10 records for the presence of common delimiters.  This is usually successful for 
 * the most common ones: tab and pipe, but comma and space delimiters can be located mistakenly
 * 
 * For fixed-width files it attempts to determine the width of individual fields by looking at 
 * the first 10 records for patterns of white-space or switching between numeric and character
 * data.  This isn't usually correct, but can get the user pretty far along to figuring
 * out the right widths.
 * 
 * The second step of the wizard attempts to identify the data type of each column in the data
 * file.  It can recognize integers, floating point numbers, dates of a variety of formats
 * and assumes anything else is a string.
 * 
 * The third step of the wizard gets some basic statistics about each column
 * separately including minimum and maximum value, number of nulls or type errors,
 * and histogram of most frequent values.  It also gets the average of numeric fields
 * 
 * The last step of the wizard gives a few different output options.  The user can
 * generate an SQL Create Table statement, and an Oracle sqlldr control file
 * to help with importing to a database.  The user can process the entire file
 * and have well formatted output go to a "good" file while poorly formatted 
 * (wrong number of columns or type errors in any one column) goes to a bad file.
 * Also, the good records can be written directly to a specified database using 
 * JDBC, but only using the slow record-by-record insert method which you would not
 * want to try on anything over a few tens of thousands of records.
 * 
 * Problems can occurr working with files that have no line breaks, or that have delimited
 * fields in which the first column can be blank.
 * 
 * @author thomas.zielund@thomsonreuters.com
 *
 */
public class SchemaWiz
{
	boolean packFrame = false;

	/**
	 *  Construct the application (no parameters)
	 */
	public SchemaWiz()
	{
		SchemaWizFrame frame = new SchemaWizFrame();
		// Validate frames that have preset sizes
		// Pack frames that have useful preferred size info, e.g. from their layout
		if (packFrame)
			frame.pack();
		else
			frame.validate();
		frame.setVisible(true);
	}

	/**
	 * Construct the application (one argument)
	 * @param arg is the filename to profile
	 */
	public SchemaWiz(String arg)
	{
		SchemaWizFrame frame = new SchemaWizFrame();
		// Validate frames that have preset sizes
		// Pack frames that have useful preferred size info, e.g. from their layout
		if (packFrame)
			frame.pack();
		else
			frame.validate();
		frame.setVisible(true);
		frame.txtInfile.setText(arg);
		frame.state_check();
		frame.start_preview();
	}

	/**
	 *  Main method sets up the GUI environment and passes through the 
	 *  args as a single filename
	 * @param args
	 */
	public static void main(String[] args)
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e)
		{
		}
		try
		{
			String s = "";
			for (int i = 0; i < args.length; i++)
			{
				if (i != 0)
				{
					s += " ";
				}
				s += args[i];
			}
			if (s.equals(""))
			{
				new SchemaWiz();
			} else
			{
				new SchemaWiz(s);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			// new SchemaWiz();
		}
	}
}