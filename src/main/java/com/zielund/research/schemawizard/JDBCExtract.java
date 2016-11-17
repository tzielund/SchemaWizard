package com.zielund.research.schemawizard;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Properties;
import java.util.zip.GZIPOutputStream;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import com.zielund.research.schemawizard.util.DotCounter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

/**
 * This application/class can be used to extract any one table from a JDBC data
 * source to a tab-delimited text file.  It's a command-line utility which makes
 * it nicely scriptable.
 * 
 * Syntax: see the parseOptions method
 * 
 * ... JDBCExtract
 * 
 * Always include the JDBC drivers
 * 
 * @author u0045494
 *
 */
/**
 * @author u0045494
 * 
 */
public class JDBCExtract {
	public static final String AES_ALGORITHM = "AES/ECB/PKCS5Padding";
	private static final String AESKEY = "AESKEY";
	private static final String AES = "AES";
	private static final String AESGEN = "AESGEN";
	Connection f_conn;
	String f_query;
	PrintStream f_output = System.out;
	PrintStream f_logFile = System.err;
	byte[] f_aeskey = null;
	Cipher f_cipher = null;

	static Properties defaults = new Properties();

	/**
	 * Create an extractor based on parameter options obtained from command line
	 * 
	 * @param options
	 *            holds all the command-line specified options (see
	 *            parseOptions)
	 * @throws IOException
	 *             for file output problems
	 * @throws ClassNotFoundException
	 *             for missing JDBC Driver problems
	 * @throws SQLException
	 *             for problems with connection or query
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 */
	public JDBCExtract(Properties options) throws IOException,
			ClassNotFoundException, SQLException, NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeyException {
		Class.forName(options.getProperty(DRVR_PROPERTY));
		String url = options.getProperty(CONN_PROPERTY);
		String username = options.getProperty(USER_PROPERTY);
		String password = options.getProperty(PASS_PROPERTY);
		f_conn = DriverManager.getConnection(url, username, password);
		f_query = options.getProperty(QUERY_PROPERTY);
		f_output = System.out;
		if (options.containsKey(OUTFILE_PROPERTY)) {
			String filename = options.getProperty(OUTFILE_PROPERTY);
			if (filename.endsWith("gz")) {
				f_output = new PrintStream(new GZIPOutputStream(
						new FileOutputStream(new File(filename))));
				System.err.println("Compressed output going to " + filename);
			} else {
				f_output = new PrintStream(new FileOutputStream(new File(
						filename)));
				System.err.println("Output going to " + filename);
			}

		}
		if (options.containsKey(AESGEN)) {
			f_cipher = Cipher.getInstance(AES_ALGORITHM);
			SecureRandom r = new SecureRandom();
			f_aeskey = new byte[16];
			r.nextBytes(f_aeskey);
			SecretKeySpec key = new SecretKeySpec(f_aeskey, AES);
			f_cipher.init(Cipher.ENCRYPT_MODE, key);
		} else if (options.containsKey(AESKEY)) {
			String keyString = options.getProperty(AESKEY);
			f_aeskey = HexStringToByteArr(keyString);
			f_cipher = Cipher.getInstance(AES_ALGORITHM);
			SecretKeySpec key = new SecretKeySpec(f_aeskey, AES);
			f_cipher.init(Cipher.ENCRYPT_MODE, key);
		}
	}

	/**
	 * Main method used to extract the query result to delimited text. Output
	 * goes either to standard out or to the output file specified by command
	 * line parameter
	 * 
	 * @throws SQLException
	 *             when there's database issues
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 */
	public DotCounter extract(Properties options) throws SQLException,
			IllegalBlockSizeException, BadPaddingException {
		DotCounter dc = new DotCounter();
		try {
			Statement statement = f_conn.createStatement();
			System.err.println("Executing: " + f_query);
			ResultSet rs = statement.executeQuery(f_query);
			String delim = "";
			logStart(options);
			StringBuffer header = new StringBuffer();
			for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
				header.append(delim + rs.getMetaData().getColumnName(i));
				delim = "\t";
			}
			if (!options.containsKey(HEADLESS_PROPERTY)) {
				outputStringBuffer(header);
			}
			logHeader(header.toString());
			while (rs.next()) {
				StringBuffer line = new StringBuffer();
				delim = "";
				for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
					line.append(delim + rs.getString(i));
					delim = "\t";
				}
				dc.inc();
				outputStringBuffer(line);
			}
			dc.finished();
			logFinished(dc);
			f_output.close();
			rs.close();
			statement.close();
			return (dc);
		} catch (SQLException e) {
			logError(dc, e);
			throw (e);
		}
	}

	public static String byteToHex(byte b) {
		return Integer.toString((b & 0xff) + 0x100, 16).substring(1);
	}

	public static byte HexToByte(String hex) {
		if (hex.length() != 2) {
			throw new NumberFormatException(
					"Expecting a two-character hex byte");
		}
		return (byte) ((Character.digit(hex.charAt(0), 16) << 4) + Character
				.digit(hex.charAt(1), 16));
	}

	public static String byteArrToHex(byte[] x) {
		StringBuffer y = new StringBuffer(x.length * 2);
		for (int i = 0; i < x.length; i++) {
			y.append(byteToHex(x[i]));
		}
		return y.toString();
	}

	public static byte[] HexStringToByteArr(String hexstr) {
		byte[] result = new byte[hexstr.length() / 2];
		for (int i = 0; i < hexstr.length(); i += 2) {
			result[i / 2] = HexToByte(hexstr.substring(i, i + 2));
		}
		return result;
	}

	private void outputStringBuffer(StringBuffer line)
			throws IllegalBlockSizeException, BadPaddingException {
		if (f_aeskey != null) {
			byte[] x = f_cipher.doFinal(line.toString().getBytes());
			String y = byteArrToHex(x);
			f_output.println(y);
		} else {
			f_output.println(line.toString());
		}
	}

	/**
	 * Setter for output print stream (default is System.out)
	 * 
	 * @param ps
	 *            is a replacement output print stream
	 */
	public void setOutputPrintStream(PrintStream ps) {
		f_output = ps;
	}

	public static final String CONFIG_FILE = "JDBCExtract.cfg";
	public static final String DRVR_PROPERTY = "JDBC_DRIVER";
	public static final String CONN_PROPERTY = "JDBC_URL";
	public static final String USER_PROPERTY = "JDBC_USER";
	public static final String PASS_PROPERTY = "JDBC_PASSWORD";
	public static final String SRVR_PROPERTY = "NAMED_SERVER";
	public static final String DB_PROPERTY = "NAMED_DATABASE";
	public static final String QUERY_PROPERTY = "QUERY";
	public static final String TABLE_PROPERTY = "TABLE";
	public static final String OUTFILE_PROPERTY = "OUTFILE";
	public static final String HEADLESS_PROPERTY = "HEADLESS";

	public static final String DEFAULT_DRIVER = "oracle.jdbc.driver.OracleDriver";
	public static final String DEFAULT_CONNECTION = "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=binford.int.westgroup.com)(PORT=1521)))(CONNECT_DATA=(SID=ors165)))";
	public static final String DEFAULT_USER = "fermi_staging";
	public static final String DEFAULT_PASSWORD = "fermi_staging";

	/**
	 * Load default values from configuration file
	 * 
	 * @throws IOException
	 *             when there's problems locating or reading it
	 */
	public static void loadDefaults() throws IOException {
		File configFile = new File(CONFIG_FILE);
		if (configFile.exists()) {
			defaults.loadFromXML(new FileInputStream(configFile));
			if (!defaults.containsKey(DRVR_PROPERTY)) {
				defaults.setProperty(DRVR_PROPERTY, DEFAULT_DRIVER);
			}
			if (!defaults.containsKey(CONN_PROPERTY)) {
				defaults.setProperty(CONN_PROPERTY, DEFAULT_CONNECTION);
			}
			if (!defaults.containsKey(USER_PROPERTY)) {
				defaults.setProperty(USER_PROPERTY, DEFAULT_USER);
			}
			if (!defaults.containsKey(PASS_PROPERTY)) {
				defaults.setProperty(PASS_PROPERTY, DEFAULT_PASSWORD);
			}

		} else {
			defaults.setProperty(DRVR_PROPERTY, DEFAULT_DRIVER);
			defaults.setProperty(CONN_PROPERTY, DEFAULT_CONNECTION);
			defaults.setProperty(USER_PROPERTY, DEFAULT_USER);
			defaults.setProperty(PASS_PROPERTY, DEFAULT_PASSWORD);
			defaults.storeToXML(new FileOutputStream(configFile),
					"Default configuration of JDBCExtract");
		}
	}

	/**
	 * Parses the command line args into an options structure.
	 * 
	 * Syntax: java <java options> com.thomson.research.schemawiz.JDBCExtract
	 * [options] Options include: [ option: s server +ARG :: Server by Name ] [
	 * option: n named_db +ARG :: Named Database ] [ option: d driver +ARG ::
	 * JDBC Driver ] [ option: u user +ARG :: Username for database login ] [
	 * option: q query +ARG :: Query text to run and extract in full ] [ option:
	 * o outfile +ARG :: Output file (full or relative path) ] [ option: h help ::
	 * Display syntax help and exit ] [ option: c connection +ARG :: JDBC
	 * Connection URL ] [ option: t table +ARG :: Name of table to extract in
	 * full ] [ option: p password +ARG :: Password for database login ] [
	 * Options: [ short {-s=[ option: s server +ARG :: Server by Name ], -n=[
	 * option: n named_db +ARG :: Named Database ], -d=[ option: d driver +ARG ::
	 * JDBC Driver ], -u=[ option: u user +ARG :: Username for database login ],
	 * -q=[ option: q query +ARG :: Query text to run and extract in full ],
	 * -o=[ option: o outfile +ARG :: Output file (full or relative path) ],
	 * -h=[ option: h help :: Display syntax help and exit ], -c=[ option: c
	 * connection +ARG :: JDBC Connection URL ], -t=[ option: t table +ARG ::
	 * Name of table to extract in full ], -p=[ option: p password +ARG ::
	 * Password for database login ]} ] [ long {--table=[ option: t table +ARG ::
	 * Name of table to extract in full ], --connection=[ option: c connection
	 * +ARG :: JDBC Connection URL ], --help=[ option: h help :: Display syntax
	 * help and exit ], --query=[ option: q query +ARG :: Query text to run and
	 * extract in full ], --password=[ option: p password +ARG :: Password for
	 * database login ], --named_db=[ option: n named_db +ARG :: Named Database ],
	 * --server=[ option: s server +ARG :: Server by Name ], --outfile=[ option:
	 * o outfile +ARG :: Output file (full or relative path) ], --driver=[
	 * option: d driver +ARG :: JDBC Driver ], --user=[ option: u user +ARG ::
	 * Username for database login ]} ]
	 * 
	 * @param args
	 *            are straight from main
	 * @return a Properties representation of the arguments
	 * @throws ParseException
	 *             when the command line is a problem
	 * @throws IOException
	 *             when the file system is a problem
	 */
	public static Properties parseOptions(String[] args) throws ParseException,
			IOException {
		Properties p = new Properties();
		Options options = new Options();
		CommandLineParser parser = new PosixParser();
		CommandLine cmd;
		options.addOption("n", "named_db", true, "Named Database");
		options.addOption("s", "server", true, "Server by Name");
		options.addOption("d", "driver", true, "JDBC Driver");
		options.addOption("c", "connection", true, "JDBC Connection URL");
		options.addOption("u", "user", true, "Username for database login");
		options.addOption("p", "password", true, "Password for database login");
		options.addOption("t", "table", true,
				"Name of table to extract in full");
		options.addOption("q", "query", true,
				"Query text to run and extract in full");
		options.addOption("o", "outfile", true,
				"Output file (full or relative path)");
		options.addOption("?", "help", false, "Display syntax help and exit");
		options.addOption("h", "header", false, "Header output to data file?");
		options.addOption("i", "interactive", false,
				"If true will read query from standard in");
		options.addOption("k", "keygen", false,
				"Request to have an AES key generated");
		options.addOption("K", "Key", true,
				"AES Key (comma-delimited decimal string of bytes)");
		cmd = parser.parse(options, args);

		p.setProperty(DRVR_PROPERTY, defaults.getProperty(DRVR_PROPERTY));
		p.setProperty(CONN_PROPERTY, defaults.getProperty(CONN_PROPERTY));
		p.setProperty(USER_PROPERTY, defaults.getProperty(USER_PROPERTY));
		p.setProperty(PASS_PROPERTY, defaults.getProperty(PASS_PROPERTY));

		if (cmd.hasOption("o")) {
			p.setProperty(OUTFILE_PROPERTY, cmd.getOptionValue("o"));
		}

		if (cmd.hasOption("?")) {
			syntax(options);
			System.exit(0);
		}
		if (cmd.hasOption("h")) {
			p.setProperty(HEADLESS_PROPERTY, String.valueOf(true));
		}
		if (cmd.hasOption("d")) {
			p.setProperty(DRVR_PROPERTY, cmd.getOptionValue("d"));
		}
		if (cmd.hasOption("c")) {
			p.setProperty(CONN_PROPERTY, cmd.getOptionValue("c"));
		}
		if (cmd.hasOption("u")) {
			p.setProperty(USER_PROPERTY, cmd.getOptionValue("u"));
		}
		if (cmd.hasOption("p")) {
			p.setProperty(PASS_PROPERTY, cmd.getOptionValue("p"));
		}

		if (cmd.hasOption("k")) {
			p.put(AESGEN, AESGEN);
		}
		if (cmd.hasOption("K")) {
			String key = cmd.getOptionValue("K");
			byte[] keyTest = HexStringToByteArr(key);
			p.put(AESKEY, key);
		}

		if (cmd.hasOption("n")) {
			String name = cmd.getOptionValue("n").toUpperCase();
			boolean needToWrite = false;
			if (defaults.containsKey(DB_PROPERTY + "_" + DRVR_PROPERTY + "_"
					+ name)) {
				p.setProperty(DRVR_PROPERTY, defaults.getProperty(DB_PROPERTY
						+ "_" + DRVR_PROPERTY + "_" + name));
			} else {
				needToWrite = true;
				defaults.setProperty(DB_PROPERTY + "_" + DRVR_PROPERTY + "_"
						+ name, p.getProperty(DRVR_PROPERTY));
			}
			if (defaults.containsKey(DB_PROPERTY + "_" + CONN_PROPERTY + "_"
					+ name)) {
				p.setProperty(CONN_PROPERTY, defaults.getProperty(DB_PROPERTY
						+ "_" + CONN_PROPERTY + "_" + name));
			} else {
				needToWrite = true;
				defaults.setProperty(DB_PROPERTY + "_" + CONN_PROPERTY + "_"
						+ name, p.getProperty(CONN_PROPERTY));
			}
			if (defaults.containsKey(DB_PROPERTY + "_" + USER_PROPERTY + "_"
					+ name)) {
				p.setProperty(USER_PROPERTY, defaults.getProperty(DB_PROPERTY
						+ "_" + USER_PROPERTY + "_" + name));
			} else {
				needToWrite = true;
				defaults.setProperty(DB_PROPERTY + "_" + USER_PROPERTY + "_"
						+ name, p.getProperty(USER_PROPERTY));
			}
			if (defaults.containsKey(DB_PROPERTY + "_" + PASS_PROPERTY + "_"
					+ name)) {
				p.setProperty(PASS_PROPERTY, defaults.getProperty(DB_PROPERTY
						+ "_" + PASS_PROPERTY + "_" + name));
			} else {
				needToWrite = true;
				defaults.setProperty(DB_PROPERTY + "_" + PASS_PROPERTY + "_"
						+ name, p.getProperty(PASS_PROPERTY));
			}
			if (needToWrite) {
				defaults.storeToXML(
						new FileOutputStream(new File(CONFIG_FILE)),
						"Updated configuration of JDBCExtract");
				System.err.println(defaults);
				System.err.println(namedDatabases());
				throw new RuntimeException("Did not recognize named database, "
						+ name + ".  Default values have been written to "
						+ CONFIG_FILE);
			}
		}

		if (cmd.hasOption("q")) {
			p.setProperty(QUERY_PROPERTY, cmd.getOptionValue("q"));
		} else if (cmd.hasOption("t")) {
			p.setProperty(QUERY_PROPERTY, "select * from "
					+ cmd.getOptionValue("t"));
			System.err.println("Inferred query = "
					+ p.getProperty(QUERY_PROPERTY));
		} else if (cmd.hasOption("i")) {
			System.out.println("Begin typing input query now:");
			StringBuffer queryBuffer = new StringBuffer();
			BufferedReader br = new BufferedReader(new InputStreamReader(
					System.in));
			String line;
			while ((line = br.readLine()) != null) {
				queryBuffer.append(line + " ");
			}
			p.setProperty(QUERY_PROPERTY, queryBuffer.toString());
			System.err.println("Read the following query: "
					+ queryBuffer.toString());
		} else {
			System.err.println("Syntax Error: Specify query or table");
			syntax(options);
			throw new RuntimeException();
		}
		return p;
	}

	private static String namedDatabases() {
		StringBuffer sb = new StringBuffer();
		for (Object okey : defaults.keySet()) {
			String key = (String) okey;
			if (key.startsWith(DB_PROPERTY + "_" + DRVR_PROPERTY)) {
				sb.append(key.substring(DB_PROPERTY.length()
						+ DRVR_PROPERTY.length() + 2)
						+ ", ");
			}
		}
		return sb.toString();
	}

	/**
	 * Prints the command-line syntax options
	 * 
	 * @param options
	 *            holds the available options
	 */
	private static void syntax(Options options) {
		System.err.println("Syntax:");
		System.err
				.println("java <java options> com.thomson.research.schemawiz.JDBCExtract [options]");
		System.err.println("Options include:");
		for (int i = 0; i < options.getOptions().size(); i++) {
			System.out.println(options.getOptions().toArray()[i]);
		}
		System.err.println(options.toString());
	}

	/**
	 * Main command-line executable. See parseOptions for syntax.
	 * 
	 * @param args
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws ParseException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 */
	public static void main(String[] args) throws ClassNotFoundException,
			SQLException, IOException, ParseException,
			NoSuchAlgorithmException, InvalidKeyException,
			NoSuchPaddingException, IllegalBlockSizeException,
			BadPaddingException {
		loadDefaults();
		Properties options = parseOptions(args);
		JDBCExtract extract = new JDBCExtract(options);
		DotCounter dc = extract.extract(options);
		extract.close();
	}

	private void logStart(Properties options) {
		if (options.containsKey(OUTFILE_PROPERTY)) {
			String filename = options.getProperty(OUTFILE_PROPERTY);
			filename = filename + ".log";
			try {
				f_logFile = new PrintStream(new FileOutputStream(new File(
						filename)));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				f_logFile = System.err;
			}
			String url = options.getProperty(CONN_PROPERTY);
			String username = options.getProperty(USER_PROPERTY);
			String password = options.getProperty(PASS_PROPERTY);
			f_logFile.println("--URL=" + url);
			f_logFile.println("--username=" + username);
			f_logFile.println("--password=" + password);
			if (f_aeskey != null) {
				f_logFile.print("--privateKey=" + byteArrToHex(f_aeskey));
				f_logFile.println();
			}
			f_logFile.println(f_query);
			f_logFile.println("Processing started at " + new Date().toString());
			f_logFile.println("----------");
		}
	}

	private void logHeader(String header) {
		f_logFile.println(header);
	}

	private void logFinished(DotCounter dc) {
		f_logFile.println("----------");
		f_logFile.println("Processing completed at" + new Date().toString());
		f_logFile.println("Total processing time was " + dc.getTotalTime()
				+ " milliseconds");
		f_logFile.println("A total of " + dc.getCounter() + " records written");
		f_logFile.close();
	}

	private void logError(DotCounter dc, Exception e) {
		f_logFile.println("----------");
		f_logFile.println("Exception Occurred!!!!!");
		e.printStackTrace(new PrintStream(f_logFile));
		f_logFile.println("Processing completed at" + new Date().toString());
		f_logFile.println("Total processing time was " + dc.getTotalTime()
				+ " milliseconds");
		f_logFile.println("A total of " + dc.getCounter() + " records written");
		f_logFile.close();
	}

	private void close() {
		f_output.close();
	}

}
