package com.zielund.research.schemawizard;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellRenderer;

// interesting startup parameters
// /data/example.txt
// /data/example_fixed.txt
// d:/source.txt
// gunzip -c //abbey/DataSets/gus/npgus.delimited.txt.gz |

/**
 * A Swing application structure to organize and lead a user through profiling a tabular data file. This class is responsible for most of the GUI
 */
@SuppressWarnings("unchecked")
public class SchemaWizFrame extends JFrame
{
	// Fields added by JBuilder
	BorderLayout borderLayout1 = new BorderLayout();
	JTabbedPane jTabbedPane1 = new JTabbedPane();
	JPanel tabFileProps = new JPanel();
	JPanel tabColumnProps = new JPanel();
	JPanel tabColumnStats = new JPanel();
	JPanel tabFinish = new JPanel();
	JPanel jPanel6 = new JPanel();
	JScrollPane jScrollPane1 = new JScrollPane();
	BorderLayout borderLayout2 = new BorderLayout();
	JLabel jLabel1 = new JLabel();
	JTextField txtInfile = new JTextField();
	JButton cmdDoIt = new JButton();
	JRadioButton optFixed = new JRadioButton();
	JRadioButton optDelimited = new JRadioButton();
	JPanel pnlStatus = new JPanel();
	TitledBorder titledBorder1;
	JPanel pnlNavigation = new JPanel();
	JButton btnNext = new JButton();
	JButton btnPrev = new JButton();
	BorderLayout borderLayout3 = new BorderLayout();
	BorderLayout borderLayout4 = new BorderLayout();
	JPanel jPanel3 = new JPanel();
	JPanel jPanel4 = new JPanel();
	BorderLayout borderLayout5 = new BorderLayout();
	JRadioButton optDetectTypes = new JRadioButton();
	JRadioButton optDetectErrors = new JRadioButton();
	JButton btnReview = new JButton();
	// Fields I added
	javax.swing.Timer timerContinuing = new javax.swing.Timer(0, null);
	ButtonGroup bgDelim = new ButtonGroup();
	ButtonGroup bgDetect = new ButtonGroup();
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
	// GetDelimChar getDelimChar = new GetDelimChar();
	RecordSplitter recSplit = new RecordSplitter();
	MyMetaTableModel reviewMetaModel = new MyMetaTableModel();
	MyTableModel reviewModel = new MyTableModel();
	MyStatsTableModel statsModel = new MyStatsTableModel();
	JTable reviewTable = new JTable(reviewModel)
	{
		public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int vColIndex)
		{
			Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
			if (c instanceof JComponent)
			{
				JComponent jc = (JComponent) c;
				jc.setToolTipText((String) getValueAt(rowIndex, vColIndex));
			}
			return c;
		}
	};
	JTable reviewMetaTable = new JTable(reviewModel)
	{
		public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int vColIndex)
		{
			Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
			if (c instanceof JComponent)
			{
				JComponent jc = (JComponent) c;
				jc.setToolTipText((String) getValueAt(rowIndex, vColIndex));
			}
			return c;
		}
	};
	JScrollPane jScrollPane3 = new JScrollPane();
	Box reviewMetaBox;
	JPanel jPanel9 = new JPanel();
	Box reviewBox;
	BorderLayout borderLayout6 = new BorderLayout();
	JViewport reviewViewport = new JViewport();
	BorderLayout borderLayout7 = new BorderLayout();
	JScrollBar reviewScrollBar = new JScrollBar();
	GridBagLayout gridBagLayout1 = new GridBagLayout();
	JCheckBox optTitleRow = new JCheckBox();
	JScrollPane jScrollPane2 = new JScrollPane();
	JPanel jPanel11 = new JPanel();
	JTable jTable1 = new JTable()
	{
		public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int vColIndex)
		{
			Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
			if (c instanceof JComponent)
			{
				JComponent jc = (JComponent) c;
				jc.setToolTipText((String) getValueAt(rowIndex, vColIndex));
			}
			return c;
		}
	};
	BorderLayout borderLayout8 = new BorderLayout();
	BorderLayout borderLayout9 = new BorderLayout();
	JButton cmdBrowse = new JButton();
	JEditorPane txtFileContent = new JEditorPane();
	JCheckBox optFilterShortRecords = new JCheckBox();
	JCheckBox optFilterLongRecords = new JCheckBox();
	JCheckBox optFilterBadTypeRecords = new JCheckBox();
	JTextField txtKDEFile = new JTextField();
	JButton cmdBrowseKDESchema = new JButton();
	JCheckBox optKDEFile = new JCheckBox();
	JLabel jLabel3 = new JLabel();
	JCheckBox optSQLFile = new JCheckBox();
	JTextField txtSQLFile = new JTextField();
	JButton cmdBrowseSQLSchema = new JButton();
	JCheckBox optGoodFile = new JCheckBox();
	JTextField txtGoodFile = new JTextField();
	JButton cmdBrowseGoodFile = new JButton();
	JButton cmdBrowseBadFile = new JButton();
	JCheckBox optBadFile = new JCheckBox();
	JTextField txtBadFile = new JTextField();
	JButton cmdAct = new JButton();
	JLabel jLabel4 = new JLabel();
	JTextField txtTabStops = new JTextField();
	JPanel jPanel10 = new JPanel();
	JCheckBox optPause = new JCheckBox();
	Process p = null;
	BorderLayout borderLayout10 = new BorderLayout();
	JLabel lblStatus = new JLabel();
	int timerKind = 0;
	JCheckBox optStripQuotes = new JCheckBox();
	JCheckBox optQuoteStrings = new JCheckBox();
	// GetDelimChar getDelimChar2 = new GetDelimChar();
	RecordSplitter recSplit2 = new RecordSplitter();
	JButton cmdChooseDelim = new JButton();
	boolean handEditedQuote = false;
	JCheckBox optGoodDatabase = new JCheckBox();
	JButton cmdBrowseGoodFile1 = new JButton();
	JTextField txtJDBCString = new JTextField();
	JTextField txtJDBCUserid = new JTextField();
	JLabel jLabel5 = new JLabel();
	JLabel jLabel6 = new JLabel();
	GridBagLayout gridBagLayout3 = new GridBagLayout();
	JTextField txtTableName = new JTextField();
	JLabel jLabel7 = new JLabel();
	JLabel jLabel8 = new JLabel();
	JLabel jLabel2 = new JLabel();
	JPasswordField txtJDBCPassword = new JPasswordField();
	JLabel jLabel9 = new JLabel();
	JTextField txtJDBCDriver = new JTextField();
	JLabel jLabel10 = new JLabel();
	JCheckBox optSQLTableOverwrite = new JCheckBox();
	JCheckBox optSQLDatabase = new JCheckBox();
	JCheckBox optTrim = new JCheckBox();
	JPanel jPanel1 = new JPanel();
	JButton cmdExport = new JButton();
	JTextField txtDelimChar = new JTextField();
	JTextField txtDelimChar2 = new JTextField();
	ArrayList firstLines = new ArrayList();
	JButton cmdPeek = new JButton();
	JTextField txtDateReformat = new JTextField();
	JCheckBox optReformatDates = new JCheckBox();

	// End stuff I added

	// Construct the frame
	public SchemaWizFrame()
	{
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		try
		{
			jbInit();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	// Component initialization
	private void jbInit() throws Exception
	{
		titledBorder1 = new TitledBorder("");
		reviewMetaBox = Box.createVerticalBox();
		reviewBox = Box.createVerticalBox();
		this.getContentPane().setLayout(borderLayout1);
		this.setSize(new Dimension(575, 431));
		this.setTitle("Tommy Z's Schema Wizard");
		tabFileProps.setLayout(borderLayout2);
		jLabel1.setText("Data File:");
		txtInfile.setMinimumSize(new Dimension(4, 12));
		txtInfile.setPreferredSize(new Dimension(150, 21));
		txtInfile.setToolTipText("Name the data file to explore, or click ... for file chooser");
		txtInfile.addActionListener(new java.awt.event.ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				txtInfile_actionPerformed(e);
			}
		});
		cmdDoIt.setToolTipText("Start or re-start scanning file for columnnar structure.  Will make " + "best guesses on other options on this page.");
		cmdDoIt.setSelected(true);
		cmdDoIt.setText("Do It");
		cmdDoIt.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				cmdDoIt_actionPerformed(e);
			}
		});
		optFixed.setToolTipText("NOT YET IMPLEMENTED: Select to specify a fixed width file, then pick " + "column widths.  Best guess made when file opened.");
		optFixed.setText("Fixed Width");
		optFixed.addItemListener(new java.awt.event.ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				optFixed_itemStateChanged(e);
			}
		});
		optDelimited.setToolTipText("Select to specify a delimited file, then pick delimiter character. " + " Best guess made when file opened.");
		optDelimited.setSelected(true);
		optDelimited.setText("Delimited");
		optDelimited.addItemListener(new java.awt.event.ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				optDelimited_itemStateChanged(e);
			}
		});
		optDelimited.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				optDelimited_actionPerformed(e);
			}
		});
		tabFileProps.setBorder(BorderFactory.createLoweredBevelBorder());
		jPanel6.setBorder(BorderFactory.createRaisedBevelBorder());
		jPanel6.setLayout(gridBagLayout1);
		pnlNavigation.setLayout(borderLayout3);
		btnNext.setToolTipText("Click to move on to the next step");
		btnNext.setText("Next >");
		btnNext.addActionListener(new java.awt.event.ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				btnNext_actionPerformed(e);
			}
		});
		btnPrev.setEnabled(false);
		btnPrev.setToolTipText("Click to return to the previous step");
		btnPrev.setText("< Prev");
		btnPrev.addActionListener(new java.awt.event.ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				btnPrev_actionPerformed(e);
			}
		});
		jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener()
		{

			public void stateChanged(ChangeEvent e)
			{
				jTabbedPane1_stateChanged(e);
			}
		});
		tabColumnProps.setLayout(borderLayout4);
		jPanel3.setBorder(BorderFactory.createEtchedBorder());
		jPanel4.setLayout(borderLayout5);
		optDetectTypes.setToolTipText("Select to specify type-scanning should replace an incorrect type " + "with the next compatible type.");
		optDetectTypes.setSelected(true);
		optDetectTypes.setText("Detect Types");
		optDetectErrors.setToolTipText("Select to specify type-scanning should simply report every type error " + "that occurs.");
		optDetectErrors.setText("Detect Errors");
		btnReview.setToolTipText("Starts type-scanning over from the beginning of the file.");
		btnReview.setText("Re Start");
		btnReview.addActionListener(new java.awt.event.ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				btnReview_actionPerformed(e);
			}
		});
		reviewTable.setBackground(Color.lightGray);
		reviewTable.setToolTipText("Example data from each column goes here");
		reviewTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tabColumnProps.addFocusListener(new java.awt.event.FocusAdapter()
		{

			public void focusGained(FocusEvent e)
			{
				tabColumnProps_focusGained(e);
			}
		});
		reviewMetaTable.setToolTipText("Determined column properties go here");
		reviewMetaTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		reviewMetaTable.setModel(reviewMetaModel);
		jScrollPane3.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		jPanel9.setLayout(borderLayout6);
		reviewMetaBox.setBackground(Color.lightGray);
		reviewMetaBox.setForeground(Color.pink);
		reviewViewport.setLayout(borderLayout7);
		reviewScrollBar.addAdjustmentListener(new java.awt.event.AdjustmentListener()
		{

			public void adjustmentValueChanged(AdjustmentEvent e)
			{
				reviewScrollBar_adjustmentValueChanged(e);
			}
		});
		reviewViewport.setViewPosition(new Point(0, 32));
		reviewViewport.setViewSize(new Dimension(760, 100));
		optTitleRow.setToolTipText("If checked, assume first record in file is column titles.  Best guess " + "made when file opened.");
		optTitleRow.setText("First row contains field titles");
		jPanel11.setLayout(borderLayout8);
		tabColumnStats.setLayout(borderLayout9);
		jTable1.setToolTipText("This table displays statistc for each column.  Not all types can " + "produce all statistics.");
		jTable1.setModel(statsModel);
		cmdBrowse.setToolTipText("Click to choose a file");
		cmdBrowse.setText("...");
		cmdBrowse.addActionListener(new java.awt.event.ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				cmdBrowse_actionPerformed(e);
			}
		});
		tabFinish.setLayout(gridBagLayout3);
		jTabbedPane1.setTabPlacement(JTabbedPane.BOTTOM);
		jTabbedPane1.setEnabled(false);
		jTabbedPane1.setOpaque(true);
		txtFileContent.setToolTipText("View of the first few lines of the file");
		txtFileContent.setEditable(false);
		txtFileContent.setContentType("text/html");
		txtFileContent.addMouseListener(new java.awt.event.MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				txtFileContent_mouseClicked(e);
			}
		});
		txtFileContent.addMouseMotionListener(new java.awt.event.MouseMotionAdapter()
		{
			public void mouseMoved(MouseEvent e)
			{
				txtFileContent_mouseMoved(e);
			}

			public void mouseDragged(MouseEvent e)
			{
				txtFileContent_mouseDragged(e);
			}
		});
		txtFileContent.setMinimumSize(new Dimension(6, 23));
		optFilterShortRecords.setSelected(true);
		optFilterShortRecords.setText("Filter Short Records");
		optFilterLongRecords.setSelected(true);
		optFilterLongRecords.setText("Filter Long Records");
		optFilterBadTypeRecords.setSelected(true);
		optFilterBadTypeRecords.setText("Filter Type-Error Records");
		txtKDEFile.setPreferredSize(new Dimension(150, 21));
		cmdBrowseKDESchema.setText("...");
		optKDEFile.setSelected(true);
		optKDEFile.setText("Generate SQLLoader control file");
		jLabel3.setFont(new java.awt.Font("Dialog", 1, 12));
		jLabel3.setText("Output Metadata:");
		optSQLFile.setSelected(true);
		optSQLFile.setText("Generate SQL Schema file");
		txtSQLFile.setPreferredSize(new Dimension(150, 21));
		cmdBrowseSQLSchema.setText("...");
		optGoodFile.setSelected(true);
		optGoodFile.setText("Copy non-filtered (\"good\") records to file:");
		txtGoodFile.setPreferredSize(new Dimension(150, 21));
		cmdBrowseGoodFile.setText("...");
		cmdBrowseBadFile.setText("...");
		optBadFile.setSelected(true);
		optBadFile.setText("Copy filtered (\"bad\") records to file:");
		txtBadFile.setPreferredSize(new Dimension(150, 21));
		cmdAct.setText("Make It So");
		cmdAct.addActionListener(new java.awt.event.ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				cmdAct_actionPerformed(e);
			}
		});
		jLabel4.setText("Name the table:");
		txtTabStops.setEnabled(false);
		txtTabStops.setToolTipText("Comma delimited list of char columns.  W: at the start means use " + "field widths instead.  ! at the end means end of record");
		txtTabStops.addActionListener(new java.awt.event.ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				txtTabStops_actionPerformed(e);
			}
		});
		optPause.setText("Pause");
		optPause.addActionListener(new java.awt.event.ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				optPause_actionPerformed(e);
			}
		});
		pnlStatus.setLayout(borderLayout10);
		lblStatus.setBorder(BorderFactory.createEtchedBorder());
		lblStatus.setMaximumSize(new Dimension(4000, 4));
		lblStatus.setMinimumSize(new Dimension(40, 4));
		lblStatus.setPreferredSize(new Dimension(4000, 4));
		optStripQuotes.setText("Ignore first&last quotes");
		optStripQuotes.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				optStripQuotes_actionPerformed(e);
			}
		});
		optQuoteStrings.setText("Quote Strings in Output Files");
		cmdChooseDelim.setText("Better...");
		cmdChooseDelim.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				cmdChooseDelim_actionPerformed(e);
			}
		});
		optGoodDatabase.setText("Copy non-filtered records to database:");
		cmdBrowseGoodFile1.setText("...");
		cmdBrowseGoodFile1.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				cmdBrowseGoodFile1_actionPerformed(e);
			}
		});
		txtJDBCString.setText("jdbc:microsoft:sqlserver:localhost;");
		txtJDBCUserid.setText("");
		txtJDBCPassword.setText("");
		txtJDBCDriver.setText("com.microsoft.jdbc.sqlserver.SQLServerDriver");
		readConfiguration();
		// txtJDBCDriver.setText("oracle.jdbc.driver.OracleDriver");
		// txtJDBCPassword.setText("user");
		// txtJDBCUserid.setText("pass");
		// txtJDBCString.setText("jdbc:oracle:thin:@server:1521:service_name");
		txtJDBCString.setPreferredSize(new Dimension(150, 21));
		txtJDBCUserid.setMinimumSize(new Dimension(40, 21));
		txtJDBCUserid.setPreferredSize(new Dimension(40, 21));
		jLabel5.setText("JDBC String");
		jLabel6.setText("User");
		txtTableName.setMinimumSize(new Dimension(40, 21));
		txtTableName.setPreferredSize(new Dimension(120, 21));
		jLabel7.setText("Out Delim:");
		jLabel8.setFont(new java.awt.Font("Dialog", 1, 12));
		jLabel8.setText("Output Data");
		jLabel2.setText("Password");
		jLabel9.setText("JDBC Drvr");
		jLabel10.setText("jLabel10");
		optSQLTableOverwrite.setText("Drop table if necessary");
		optSQLDatabase.setText("Create table in database (below)");
		optTrim.setText("Ignore lead/trail Blanks");
		optTrim.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				optTrim_actionPerformed(e);
			}
		});
		cmdExport.setToolTipText("");
		cmdExport.setText("Export...");
		cmdExport.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				cmdExport_actionPerformed(e);
			}
		});
		txtDelimChar.setToolTipText("");
		txtDelimChar2.setMinimumSize(new Dimension(45, 21));
		txtDelimChar2.setPreferredSize(new Dimension(45, 21));
		cmdPeek.setText("try");
		cmdPeek.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				cmdPeek_actionPerformed(e);
			}
		});
		txtDateReformat.setText("yyyy-MM-dd");
		optReformatDates.setText("Reformat Dates");

		this.getContentPane().add(jTabbedPane1, BorderLayout.CENTER);
		jTabbedPane1.add(tabFileProps, "Specify File Properties");
		tabFileProps.add(jPanel6, BorderLayout.NORTH);
		jPanel6.add(jLabel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		jPanel6.add(cmdBrowse, new GridBagConstraints(2, 0, 1, 2, 0.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
		jPanel6.add(optFixed, new GridBagConstraints(3, 1, 1, 2, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		jPanel6.add(optDelimited, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		jPanel6.add(txtInfile, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 133, 14));
		jPanel6.add(cmdDoIt, new GridBagConstraints(4, 3, 1, 4, 0.0, 0.0, GridBagConstraints.SOUTHEAST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		jPanel6.add(optTitleRow, new GridBagConstraints(0, 2, 2, 2, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		jPanel6.add(optFilterShortRecords, new GridBagConstraints(0, 4, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		jPanel6.add(txtTabStops, new GridBagConstraints(4, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 117, 0));
		jPanel6.add(optFilterLongRecords, new GridBagConstraints(2, 4, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		jPanel6.add(optStripQuotes, new GridBagConstraints(0, 5, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		jPanel6.add(optTrim, new GridBagConstraints(2, 5, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		jPanel6.add(txtDelimChar, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		jPanel6.add(cmdPeek, new GridBagConstraints(5, 0, 1, 3, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));
		tabFileProps.add(jScrollPane1, BorderLayout.CENTER);
		jScrollPane1.getViewport().add(txtFileContent, null);
		jScrollPane1.getViewport().add(txtFileContent, null);
		jTabbedPane1.add(tabColumnProps, "Specify Column Properties");
		tabColumnProps.add(jPanel3, BorderLayout.NORTH);
		jPanel3.add(optDetectTypes, null);
		jPanel3.add(optDetectErrors, null);
		jPanel3.add(btnReview, null);
		jPanel3.add(optFilterBadTypeRecords, null);
		tabColumnProps.add(jPanel4, BorderLayout.CENTER);
		jPanel4.add(jScrollPane3, BorderLayout.CENTER);
		jScrollPane3.getViewport().add(jPanel9, null);
		jPanel9.add(reviewMetaBox, BorderLayout.NORTH);
		reviewMetaBox.add(reviewMetaTable, null);
		jPanel9.add(reviewBox, BorderLayout.CENTER);
		reviewBox.add(reviewViewport, null);
		reviewViewport.add(reviewTable, BorderLayout.EAST);
		jPanel4.add(reviewScrollBar, BorderLayout.EAST);
		jTabbedPane1.add(tabColumnStats, "Review Column Statistics");
		tabColumnStats.add(jScrollPane2, BorderLayout.CENTER);
		tabColumnStats.add(jPanel1, BorderLayout.NORTH);
		jPanel1.add(cmdExport, null);
		jScrollPane2.getViewport().add(jPanel11, null);
		jPanel11.add(jTable1, BorderLayout.CENTER);
		jTabbedPane1.add(tabFinish, "Finish");
		tabFinish.add(optSQLFile, new GridBagConstraints(0, 3, 2, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		tabFinish.add(optKDEFile, new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		tabFinish.add(jLabel3, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		tabFinish.add(cmdBrowseSQLSchema, new GridBagConstraints(5, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		tabFinish.add(cmdBrowseKDESchema, new GridBagConstraints(5, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		tabFinish.add(txtKDEFile, new GridBagConstraints(2, 2, 3, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(8, 0, 0, 0), 0, 0));
		this.getContentPane().add(pnlNavigation, BorderLayout.SOUTH);
		pnlNavigation.add(pnlStatus, BorderLayout.CENTER);
		pnlStatus.add(lblStatus, BorderLayout.WEST);
		pnlNavigation.add(jPanel10, BorderLayout.EAST);
		jPanel10.add(btnPrev, null);
		jPanel10.add(btnNext, null);
		pnlNavigation.add(optPause, BorderLayout.WEST);
		// Initializer code I added
		bgDelim.add(optDelimited);
		bgDelim.add(optFixed);
		bgDetect.add(optDetectTypes);
		bgDetect.add(optDetectErrors);
		tabFinish.add(jLabel4, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		tabFinish.add(txtTableName, new GridBagConstraints(2, 1, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		tabFinish.add(txtSQLFile, new GridBagConstraints(2, 3, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		tabFinish.add(optGoodDatabase, new GridBagConstraints(0, 9, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		tabFinish.add(cmdBrowseGoodFile1, new GridBagConstraints(5, 9, 1, 3, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));
		tabFinish.add(jLabel6, new GridBagConstraints(2, 11, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		tabFinish.add(txtJDBCUserid, new GridBagConstraints(3, 11, 2, 1, 0.0, 0.0, GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		tabFinish.add(optGoodFile, new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		tabFinish.add(jLabel8, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		tabFinish.add(txtGoodFile, new GridBagConstraints(2, 8, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		tabFinish.add(cmdBrowseGoodFile, new GridBagConstraints(5, 8, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		tabFinish.add(optBadFile, new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		tabFinish.add(txtBadFile, new GridBagConstraints(2, 7, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		tabFinish.add(jLabel7, new GridBagConstraints(2, 6, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		tabFinish.add(cmdBrowseBadFile, new GridBagConstraints(5, 7, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		tabFinish.add(jLabel2, new GridBagConstraints(2, 12, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		tabFinish.add(cmdAct, new GridBagConstraints(0, 13, 6, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 19), 0, 0));
		tabFinish.add(txtJDBCPassword, new GridBagConstraints(3, 12, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		tabFinish.add(jLabel5, new GridBagConstraints(2, 10, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		tabFinish.add(txtJDBCString, new GridBagConstraints(3, 10, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		tabFinish.add(jLabel9, new GridBagConstraints(2, 9, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		tabFinish.add(txtJDBCDriver, new GridBagConstraints(3, 9, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		tabFinish.add(optSQLTableOverwrite, new GridBagConstraints(3, 4, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		tabFinish.add(jLabel10, new GridBagConstraints(3, 4, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		tabFinish.add(optSQLDatabase, new GridBagConstraints(0, 4, 3, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		tabFinish.add(txtDelimChar2, new GridBagConstraints(3, 6, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		tabFinish.add(cmdChooseDelim, new GridBagConstraints(4, 6, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		tabFinish.add(optQuoteStrings, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		tabFinish.add(txtDateReformat, new GridBagConstraints(4, 5, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		tabFinish.add(optReformatDates, new GridBagConstraints(2, 5, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		reviewTable.setColumnModel(reviewMetaTable.getColumnModel());
		timerContinuing.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				timerEvent();
			}
		});
		timerContinuing.setRepeats(false);
		timerContinuing.setDelay(100);
		timerContinuing.setInitialDelay(100);
		readConfiguration();
		// End initializer code I added
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
					txtJDBCDriver.setText(config.getProperty("jdbcDriver"));
				if (config.containsKey("jdbcURL"))
					txtJDBCString.setText(config.getProperty("jdbcURL"));
				if (config.containsKey("jdbcUser"))
					txtJDBCUserid.setText(config.getProperty("jdbcUser"));
				if (config.containsKey("jdbcPassword"))
					txtJDBCPassword.setText(config.getProperty("jdbcPassword"));
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
		String source = txtInfile.getText();
		String message = "Doesn't work yet";
		char[] msg = message.toCharArray();
		BufferedReader br = new BufferedReader(new CharArrayReader(msg));
//		if (source.toUpperCase().endsWith(".GZ"))
//		{
//			source = "gunzip -c " + source + " |";
//		}
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
		// lblStatus.setText (""); lblStatus.paint(lblStatus.getGraphics());
		// lblStatus.getGraphics().fillRect(lblStatus.LEFT,lblStatus.TOP,lblStatus.RIGHT-lblStatus.LEFT,lblStatus.BOTTOM-lblStatus.TOP);
		Color c = lblStatus.getForeground();
		lblStatus.setForeground(lblStatus.getBackground());
		lblStatus.setText(lblStatus.getText());
		lblStatus.paint(lblStatus.getGraphics());
		lblStatus.setForeground(c);
		lblStatus.getGraphics().fillRect(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
		lblStatus.setText(message);
		lblStatus.paint(lblStatus.getGraphics());
		Toolkit.getDefaultToolkit().sync();
	}

	/* // ******************************* // */
	/* // ******* PREVIEW SECTION ******* // */
	/* // ******************************* // */

	// public boolean isFixWidthError(String s) {
	// return s.length()!=numChars;
	// }
	//

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
			if (nf > numFields && (optFilterLongRecords.isSelected() || jTabbedPane1.getSelectedComponent() == tabFileProps))
			{
				b = true;
				numRecsGTNumFields++;
			} else if (nf < numFields && (optFilterShortRecords.isSelected() || jTabbedPane1.getSelectedComponent() == tabFileProps))
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
		this.setTitle("Schema Wizard: Preview " + txtInfile.getText());
		String s;
		s = "Preview: " + recNum + " records, " + numFields + " fields";
		if (numRecsLTNumFields > 0)
		{
			s += " (" + numRecsLTNumFields + " Short";
			if (optFilterShortRecords.isSelected())
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
			if (optFilterLongRecords.isSelected())
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
			txtFileContent.setText(data1.toString());
			if (recSplit.autoConfigure(firstLines))
			{
				showStuff();
			}
			short_preview();
		} catch (IOException ioe)
		{
			setStatus("Error opening " + txtInfile.getText());
		}
	}

	/**
	 * Updates the GUI with what has been learned in the early stage of preview analysis. That is, what has been guessed from the first 10 records in the file
	 */
	void showStuff()
	{
		if (recSplit.isDelimited())
		{
			txtDelimChar.setText(recSplit.getDelimCharName());
			optDelimited.setSelected(true);
		} else
		{
			txtTabStops.setText(recSplit.getTabStopString());
			optFixed.setSelected(true);
		}
		optQuoteStrings.setSelected(recSplit.isQuoteEnclosed());
		optTrim.setSelected(recSplit.isTrimBlanks());
		optTitleRow.setSelected(recSplit.isLabeled());
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
			txtFileContent.setText(data1.toString());
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
			timerContinuing.start();
		} catch (IOException e)
		{
			setStatus("Error opening " + txtInfile.getText());
		}
	}

	/**
	 * Compute one chunk of file preview analysis (about 10000 records). This method is called repeatedly until the file is complete
	 */
	private void continuingPreview()
	{
		try
		{
			if (jTabbedPane1.getSelectedComponent() == tabFileProps)
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
					timerContinuing.start();
				} else
				{
					preview_status();
					setStatus(lblStatus.getText() + "--Concluded");
				}
			} else
			{
				preview_status();
				setStatus(lblStatus.getText() + "--Stopped");
			}
		} catch (IOException e)
		{
			setStatus("Error reading " + txtInfile.getText());
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
		this.setTitle("Schema Wizard: Review" + txtInfile.getText());
		setStatus("Review: " + recNum + " records, " + numFields + " fields");
		if (numFieldTypeErrors > 0)
		{
			setStatus(lblStatus.getText() + " (" + numFieldTypeErrors + " type errors)");
		}
	}

	public boolean isTypeError(String[] dt, boolean resize)
	{
		boolean b = false;
		if (optFilterBadTypeRecords.isSelected())
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
			if (optTitleRow.isSelected())
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
			reviewMetaTable.getColumnModel().getColumn(0).setPreferredWidth(120);
			for (int i = 0; i < numFields; i++)
			{
				reviewModel.addValue("");
				int width = ((MyTypeCheck) reviewMetaModel.getField(i)).getMaxSize();
				if (width < 5)
				{
					width = 5;
				}
				reviewMetaTable.getColumnModel().getColumn(i + 1).setPreferredWidth(width * 10);
			}
			reviewMetaModel.fireTableDataChanged();
			reviewModel.fireTableDataChanged();
			reviewMetaModel.resetErrorStats();
			numFieldTypeErrors = 0;
			review_status();
			timerKind = 2;
			timerContinuing.start();
		} catch (IOException e)
		{
			setStatus("Error opening " + txtInfile.getText());
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
			if (jTabbedPane1.getSelectedComponent() == tabColumnProps)
			{
				if (stInFile.nextToken() == stInFile.TT_EOF)
				{
					review_status();
					setStatus(lblStatus.getText() + "--Concluded");
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
								if (reviewMetaModel.setCheckAuto(dt[i], i, optDetectTypes.getModel().isSelected()))
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
						timerContinuing.start();
					} else
					{
						review_status();
						setStatus(lblStatus.getText() + "--Concluded");
					}
				}
			} else
			{
				review_status();
				setStatus(lblStatus.getText() + "--Ended");
			}
		} catch (IOException e)
		{
			setStatus("Error reading " + txtInfile.getText());
		}
	}

	/* // ******************************* // */
	/* // ********* STATS SECTION ******* // */
	/* // ******************************* // */

	private void stats_status()
	{
		this.setTitle("Schema Wizard: Statistics " + txtInfile.getText());
		setStatus("Statistics: " + recNum + " records, " + numFields + " fields");
	}

	void short_Stats()
	{
		cmdExport.setEnabled(false);
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
			if (optTitleRow.isSelected())
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
			timerContinuing.start();
		} catch (IOException e)
		{
			setStatus("Error opening " + txtInfile.getText());
			reviewMetaModel.setLockDisplay(false);
		}
	}

	private void continuingStats()
	{
		try
		{
			long started = new Date().getTime() + 1000;
			if (jTabbedPane1.getSelectedComponent() == tabColumnStats)
			{
				if (stInFile.nextToken() == stInFile.TT_EOF)
				{
					statsModel.finish();
					cmdExport.setEnabled(true);
					stats_status();
					setStatus(lblStatus.getText() + "--Concluded");
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
						timerContinuing.start();
					} else
					{
						statsModel.finish();
						cmdExport.setEnabled(true);
						stats_status();
						setStatus(lblStatus.getText() + "--Concluded");
					}
				}
			} else
			{
				stats_status();
				setStatus(lblStatus.getText() + "--Concluded");
			}
		} catch (IOException e)
		{
			setStatus("Error reading " + txtInfile.getText());
			lblStatus.paint(lblStatus.getGraphics());
			statsModel.setLockDisplay(false);
		}
	}

	/* // ******************************* // */
	/* // ******* OTHER STUFF *********** // */
	/* // ******************************* // */

	void state_check()
	{
		if (jTabbedPane1.getSelectedComponent() == tabFinish)
		{
			btnNext.setEnabled(false);
		} else
		{
			btnNext.setEnabled(true);
		}
		if (jTabbedPane1.getSelectedComponent() == tabFileProps)
		{
			btnPrev.setEnabled(false);
			if (txtInfile.getText().equals(""))
			{
				btnNext.setEnabled(false);
			} else
			{
				btnNext.setEnabled(true);
			}
		} else
		{
			btnPrev.setEnabled(true);
		}
	}

	public void prepare_Finish()
	{
		if (recSplit.isDelimited())
		{
			recSplit2.setDelimChar(recSplit.getDelimChar());
		} else
		{
			recSplit2.setDelimChar('\t');
		}
		txtDelimChar2.setText(txtDelimChar.getText());
		optQuoteStrings.setSelected(optStripQuotes.isSelected());
		String fn = txtInfile.getText();
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
		txtTableName.setText(tn);
		txtKDEFile.setText(path + ".ctl");
		txtSQLFile.setText(path + ".sql");
		txtBadFile.setText(path + ".bad");
		txtGoodFile.setText(path + ".good");
	}

	public void finish_status()
	{
		this.setTitle("Schema Wizard: Finish writing " + txtGoodFile.getText());
		setStatus("Finish: " + recNum + " records, " + numFields + " fields");
		if (numFieldTypeErrors > 0)
		{
			setStatus(lblStatus.getText() + " (" + numBadRecs + " bad recs)");
		}
	}

	public void start_Finish()
	{
		long magnitude = 10;
		if (optKDEFile.isSelected())
		{
			setStatus("Writing Oracle SQL Loader control file, " + txtKDEFile.getText());
			reviewMetaModel.writeSQLLDRFile(txtKDEFile.getText(), txtTableName.getText(), recSplit);
			setStatus("Writing Oracle SQL Loader control file, " + txtKDEFile.getText() + "... Finished");
		}
		if (optSQLFile.isSelected() || optGoodDatabase.isSelected())
		{
			try
			{
				StringWriter strSQL = new StringWriter();
				setStatus("Writing SQL Schema file, " + txtSQLFile.getText());
				// reviewMetaModel.writeSQL();
				BufferedWriter bw1 = new BufferedWriter(new PrintWriter(strSQL));
				bw1.write("-- Created by KDE Schema Wizard\n");
				// bw1.write("drop table " + txtTableName.getText() + ";\n");
				bw1.write("create table " + txtTableName.getText() + " (\n");
				for (int i = 0; i < numFields; i++)
				{
					MyTypeCheck mtc = (MyTypeCheck) reviewMetaModel.getField(i);
					bw1.write("\t" + mtc.getTrueName() + " ");
					bw1.write(mtc.getSQLType(txtJDBCDriver.getText()));
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
				if (optSQLFile.isSelected())
				{
					bw1 = new BufferedWriter(new FileWriter(txtSQLFile.getText()));
					bw1.write(strSQL.toString());
					bw1.flush();
					bw1.close();
					System.err.println("SQL Written to file " + txtSQLFile.getText());
				}
				if (optSQLDatabase.isSelected())
				{
					try
					{
						Class.forName(txtJDBCDriver.getText());
						Connection myDatabaseConnection = DriverManager.getConnection(txtJDBCString.getText(), txtJDBCUserid.getText(), new String(txtJDBCPassword.getPassword()));
						Statement s = myDatabaseConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
						if (optSQLTableOverwrite.isSelected())
						{
							try
							{
								System.err.println("Dropping table in database");
								s.execute("drop table " + txtTableName.getText());
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
				setStatus("Writing SQL Schema file, " + txtKDEFile.getText() + "... Finished");
			} catch (IOException i)
			{
				i.printStackTrace();
			}
		}
		if (optGoodFile.isSelected() || optBadFile.isSelected() || optGoodDatabase.isSelected())
		{
			try
			{
				// ProgressMonitor pm = new ProgressMonitor (this,"Exporting Data", "100's", 1, 100);
				recSplit2.setQuoteEnclosed(optQuoteStrings.isSelected());
				ArrayList dateFieldList = new ArrayList();
				SimpleDateFormat sdr = new SimpleDateFormat(txtDateReformat.getText());
				if (optReformatDates.isSelected())
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
				if (optGoodFile.isSelected())
				{
					bwg = new BufferedWriter(new PrintWriter(new FileWriter(txtGoodFile.getText())));
				}
				if (optBadFile.isSelected())
				{
					bwb = new BufferedWriter(new PrintWriter(new FileWriter(txtBadFile.getText())));
				}
				if (optGoodDatabase.isSelected())
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
						strSQL += " from " + txtTableName.getText();
						Class.forName(txtJDBCDriver.getText());
						myDatabaseConnection = DriverManager.getConnection(txtJDBCString.getText(), txtJDBCUserid.getText(), new String(txtJDBCPassword.getPassword()));
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
						if (optBadFile.isSelected())
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
						if (optGoodFile.isSelected())
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
						if (optGoodDatabase.isSelected() && rs != null)
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
								if (optBadFile.isSelected())
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

				if (optBadFile.isSelected())
				{
					bwb.flush();
					bwb.close();
				}
				if (optGoodFile.isSelected())
				{
					bwg.flush();
					bwb.close();
				}
				if (optGoodDatabase.isSelected() && stmt != null)
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
				setStatus(lblStatus.getText() + " --Done");
			} catch (FileNotFoundException f)
			{
				setStatus("Error finding good/bad file(s)");
			} catch (IOException i)
			{
				setStatus("Error writing to good/bad file(s)");
			}
		}
	}

	public void set_ReviewTablePosition()
	{
		int newy = new Double(reviewScrollBar.getValue() * reviewViewport.getViewSize().getHeight() / 100).intValue();
		reviewViewport.setViewPosition(new Point(0, newy));
	}

	public void startTabAction()
	{
		if (jTabbedPane1.getSelectedComponent() == tabColumnProps)
		{
			short_Review();
		} else if (jTabbedPane1.getSelectedComponent() == tabColumnStats)
		{
			short_Stats();
		} else if (jTabbedPane1.getSelectedComponent() == tabFinish)
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

	// Overridden so we can exit on System Close
	protected void processWindowEvent(WindowEvent e)
	{
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING)
		{
			System.exit(0);
		}
	}

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

	void cmdDoIt_actionPerformed(ActionEvent e)
	{
		state_check();
		short_preview();
	}

	void btnNext_actionPerformed(ActionEvent e)
	{
		if (jTabbedPane1.getSelectedIndex() < jTabbedPane1.getTabCount() - 1)
		{
			jTabbedPane1.setSelectedIndex(jTabbedPane1.getSelectedIndex() + 1);
			startTabAction();
		} else
		{
			btnNext.setEnabled(false);
		}
	}

	void btnPrev_actionPerformed(ActionEvent e)
	{
		if (jTabbedPane1.getSelectedIndex() > 0)
		{
			jTabbedPane1.setSelectedIndex(jTabbedPane1.getSelectedIndex() - 1);
		} else
		{
			btnPrev.setEnabled(false);
		}
	}

	void jTabbedPane1_stateChanged(ChangeEvent e)
	{
		state_check();
	}

	void txtInfile_actionPerformed(ActionEvent e)
	{
		state_check();
	}

	void tabColumnProps_focusGained(FocusEvent e)
	{
		short_Review();
	}

	void btnReview_actionPerformed(ActionEvent e)
	{
		try
		{
			stInFile = stInFileReset();
		} catch (FileNotFoundException fnf)
		{
			fnf.printStackTrace();
		}
		continuingReview();
	}

	void reviewScrollBar_adjustmentValueChanged(AdjustmentEvent e)
	{
		set_ReviewTablePosition();
	}

	void cmdBrowse_actionPerformed(ActionEvent e)
	{
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File(txtInfile.getText()).getParentFile());
		int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			txtInfile.setText(chooser.getSelectedFile().getPath());
			// File possibleSchema = new File(chooser.getSelectedFile().getPath()+ ".schema");
			// if (possibleSchema.exists()) {
			// JOptionPane.showMessageDialog(this,"What the");
			// }
			start_preview();
		}
	}

	void cboDelimChar_actionPerformed(ActionEvent e)
	{
		recSplit.setDelimStr(txtDelimChar.getText());
	}

	void cmdAct_actionPerformed(ActionEvent e)
	{
		start_Finish();
	}

	void txtTabStops_actionPerformed(ActionEvent e)
	{
		try
		{
			recSplit.setTabStops(txtTabStops.getText());
		} catch (ParseException pe)
		{

		}
	}

	void optPause_actionPerformed(ActionEvent e)
	{

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
				JOptionPane.showMessageDialog(this, "No candidate delimiters remain after " + rnum + " records");
			} else
			{
				String delims = candidates;
				for (int i = delims.length() - 1; i >= 0; i--)
				{
					delims = delims.substring(0, i) + recSplit2.charToName(delims.substring(i, i + 1) + delims.substring(i + 1));
				}
				int result = JOptionPane.showConfirmDialog(this, "After " + rnum + " records the remaining delimiters are \n" + delims + "\n Continue with next " + (r * 10)
						+ " records?");
				if (result == JOptionPane.OK_OPTION)
				{
					findDelimiters(r * 10, candidates, rnum);
				} else if (result == JOptionPane.NO_OPTION)
				{
					result = JOptionPane.showConfirmDialog(this, "Use " + recSplit.charToName(candidates.substring(0, 1)) + " as new delimiter?");
					if (result == JOptionPane.OK_OPTION)
					{
						recSplit2.setDelimChar(candidates.charAt(0));
						txtDelimChar2.setText(recSplit2.getDelimCharName());
					}
				}
			}
		} catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
	}

	void optStripQuotes_actionPerformed(ActionEvent e)
	{
		recSplit.setQuoteEnclosed(optStripQuotes.isSelected());
		handEditedQuote = true;
	}

	void cmdBrowseGoodFile1_actionPerformed(ActionEvent e)
	{
		String o2k = new String("Microsoft SQLServer");
		String ot = new String("Oracle Thin");
		String o = new String("Other");
		Object[] dv =
		{ o2k, ot, o };
		Object result = JOptionPane.showInputDialog(this, "What JDBC Driver do you want to use?", "Choose Driver", JOptionPane.INFORMATION_MESSAGE, null, dv, o2k);
		if (result.equals(o2k))
		{
			String serverName = JOptionPane.showInputDialog(this, "Server name");
			String databaseName = JOptionPane.showInputDialog(this, "Database Name");
			txtJDBCDriver.setText("com.microsoft.jdbc.sqlserver.SQLServerDriver");
			txtJDBCString.setText("jdbc:microsoft:sqlserver://" + serverName + ";DatabaseName=" + databaseName);
		} else if (result.equals(ot))
		{
			String serverName = JOptionPane.showInputDialog(this, "Server name");
			String databaseName = JOptionPane.showInputDialog(this, "Service or SID Name");
			txtJDBCDriver.setText("oracle.jdbc.driver.OracleDriver");
			txtJDBCString.setText("jdbc:oracle:thin:@" + serverName + ":1521:" + databaseName);
		} else
		{
			JOptionPane.showMessageDialog(this, "Then configure the driver and url in the text boxes");
		}
		optGoodDatabase.setSelected(true);
		optSQLDatabase.setSelected(true);
	}

	void optTrim_actionPerformed(ActionEvent e)
	{
		recSplit.setTrimBlanks(optTrim.isSelected());
	}

	void cmdExport_actionPerformed(ActionEvent e)
	{
		JFileChooser fc = new JFileChooser();
		File f = new File(txtInfile.getText());
		File d = f.getParentFile();
		fc.setCurrentDirectory(d);
		fc.setDialogTitle("Export results to file:");
		int result = fc.showDialog(this, "Ok");
		if (result == JFileChooser.APPROVE_OPTION)
		{
			try
			{
				String txtSourceFile = fc.getSelectedFile().getPath();
				PrintWriter pw = new PrintWriter(new FileWriter(txtSourceFile));
				pw.println("Column Statistics for file: " + txtInfile.getText());
				for (int i = 0; i < statsModel.getColumnCount(); i++)
				{
					for (int j = 0; j < statsModel.getRowCount(); j++)
					{
						pw.print(statsModel.getValueAt(j, i) + "\t");
					}
					pw.println();
				}
				pw.close();
			} catch (IOException ioe)
			{
				setStatus("IOException exporting statistics to file " + txtInfile.getText());
			}
		}
	}

	void optDelimited_actionPerformed(ActionEvent e)
	{
	}

	void optFixed_itemStateChanged(ItemEvent e)
	{
		if (optFixed.isSelected())
		{
			txtDelimChar.setEnabled(false);
			txtTabStops.setEnabled(true);
		}
	}

	void optDelimited_itemStateChanged(ItemEvent e)
	{
		if (optDelimited.isSelected())
		{
			txtDelimChar.setEnabled(true);
			txtTabStops.setEnabled(false);
		}
	}

	void cmdPeek_actionPerformed(ActionEvent e)
	{
		if (optDelimited.isSelected())
		{
			recSplit.setDelimCharName(txtDelimChar.getText());
		} else
		{
			try
			{
				if (txtTabStops.getText().equals(""))
				{
					if (!recSplit.autoConfigureFixed(firstLines, false))
					{
						throw new ParseException("Failed to autoconfigure tab stops", 0);
					}
					txtTabStops.setText(recSplit.getTabStopString());
				}
				recSplit.setTabStops(txtTabStops.getText());
			} catch (ParseException pe)
			{
				System.err.println(pe);
			}
			txtTabStops.setText(recSplit.getTabStopString());
		}
		showStuff();
	}

	void txtFileContent_mouseMoved(MouseEvent e)
	{
	}

	void txtFileContent_mouseClicked(MouseEvent e)
	{
	}

	void txtFileContent_mouseDragged(MouseEvent e)
	{
		int start = txtFileContent.getSelectionStart();
		int end = txtFileContent.getSelectionEnd();
		if (start == end)
		{
			setStatus("Selection is " + start);
		} else
		{
			end--;
			setStatus("Selection is from " + start + " to " + end);
		}
	}

}
