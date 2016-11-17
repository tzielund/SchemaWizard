package com.zielund.research.schemawizard;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MyTypeCheck {
	public static final int TC_UNSET = 0;
	public static final int TC_STRING = 1;
	public static final int TC_DOUBLE = 2;
	public static final int TC_INTEGER = 4;
	public static final int TC_DATE = 8;
	public static String TN_STRING = "string";
	public static String TN_DOUBLE = "double";
	public static String TN_INTEGER = "integer";
	public static String TN_DATE = "date";
	public static String TN_UNSET = "no type";
	public static String DP_INTEGER = "yyyyMMdd";
	public static String DP_HYPHEN = "yyyy-MM-dd";
	public static String DP_DOT = "yyyy.MM.dd";
	public static String DP_US = "MM/dd/yyyy";
	public static String DP_EUROPE = "dd/MM/yyyy";
	public static String DP_FULL = "MMM dd, yyyy";
	public static String DP_TIMESTAMP = "yyyy-MM-dd HH:mm:ss.SSS";
	public static String DP_TIMESTAMP2 = "yyyy-MM-dd HH:mm:ss";
	String fGivenName;
	String fTrueName;
	long fErrorCount;
	int fTypeCode = TC_UNSET;
	int fLength = 0;
	int fScale = 0;
	int fMaxCardinality = 0;
	long fTotalCount = 0;
	private Object localLastParse = "";
	private String localLastInput = "";
	// private static Object lastParse = "";
	// private static String lastInput = "";
	String fDatePattern = "";
	boolean fDatePatternAmbiguous = true;
	private static SimpleDateFormat sdf = new SimpleDateFormat();
	boolean fIsNotNull = false;
	boolean fIsNullSoFar = false;

	// Constructors
	public MyTypeCheck() {
	};

	public MyTypeCheck(String name, String pattern) {
		fGivenName = name;
		fTrueName = name;
		fTypeCode = TC_STRING;
		fDatePattern = pattern;
	}

	public MyTypeCheck(int typeCode) {
		this.setTypeCode(typeCode);
	};

	public MyTypeCheck(String value) {
		this.setTypeAuto(value);
		fTotalCount = 1;
	};

	// Static functions for quick check
	public static boolean checkBlank(String parseValue) {
		return parseValue.trim().equals("");
	}

	public static Double getDouble(String parseValue) {
		if (checkBlank(parseValue)) {
			return null;
		}
		try {
			Double d = new Double(parseValue.trim());
			return d;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public static boolean checkDouble(String parseValue) {
		if (checkBlank(parseValue)) {
			return true;
		}
		return getDouble(parseValue) != null;
	}

	public static Integer getInteger(String parseValue) {
		if (checkBlank(parseValue)) {
			parseValue = "";
			return null;
		}
		try {
			Integer d = new Integer(parseValue.trim());
			return d;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public static boolean checkInteger(String parseValue) {
		if (checkBlank(parseValue)) {
			return true;
		}
		return getInteger(parseValue) != null;
	}

	public static Date getDate(String parseValue, String pattern) {
		if (checkBlank(parseValue)) {
			return null;
		}
		sdf.setLenient(false);
		sdf.applyPattern(pattern);
		try {
			Date d = sdf.parse(parseValue);
			return d;
		} catch (ParseException p) {
			return null;
		}
	}

	public static boolean checkDate(String parseValue, String pattern) {
		if (checkBlank(parseValue)) {
			return true;
		}
		return getDate(parseValue, pattern) != null;
	}

	public static Date getIntDate(String parseValue) {
		parseValue = parseValue.trim();
		if (checkBlank(parseValue)) {
			return null;
		}
		if (parseValue.length() != 8) {
			return null;
		}
		String century = parseValue.substring(0, 2);
		if (!(century.equals("19") || century.equals("20"))) {
			return null;
		}
		return getDate(parseValue, DP_INTEGER);
	}

	public static boolean checkIntDate(String parseValue) {
		if (checkBlank(parseValue)) {
			return true;
		}
		return getIntDate(parseValue) != null;
	}

	/**
	 * This doesn't really check whether parseValue is a string (which it
	 * obviously is). It just checks whether parseValue is not a number
	 */
	public static boolean checkString(String parseValue) {
		if (checkBlank(parseValue)) {
			return true;
		}
		return !checkDouble(parseValue) && !checkDate(parseValue, DP_US)
				&& !checkDate(parseValue, DP_DOT)
				&& !checkDate(parseValue, DP_EUROPE)
				&& !checkDate(parseValue, DP_HYPHEN);
	}

	public static String legalizeKDESQLName(String badname) {
		// Another thing this function should do is look for duplicate names
		// between fields.
		String legal1st = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		String legal = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_0123456789";
		String repl = "_";
		String goodname = "";
		if (badname == null || badname.length() == 0) {
			goodname = "X";
			badname = " ";
		} else if (legal1st.indexOf(badname.charAt(0)) < 0) {
			goodname = "C";
			badname = " " + badname;
		} else {
			goodname = goodname + badname.charAt(0);
		}
		for (int i = 1; i < badname.length(); i++) {
			if (legal.indexOf(badname.charAt(i)) < 0) {
				goodname = goodname + repl;
			} else {
				goodname = goodname + badname.charAt(i);
			}
		}
		return goodname;
	}

	// Properties
	public String getGivenName() {
		return fGivenName;
	}

	public void setGivenName(String givenName) {
		fGivenName = givenName;
		setTrueName(givenName);
	}

	public String getTrueName() {
		return fTrueName;
	}

	public void setTrueName(String trueName) {
		fTrueName = legalizeKDESQLName(trueName);
	}

	public long getErrorCount() {
		return fErrorCount;
	}

	public long getTotalCount() {
		return fTotalCount;
	}

	public void setErrorCount(int errorCount) {
		fErrorCount = errorCount;
	}

	public void incErrorCount() {
		fErrorCount++;
	}

	public void incTotalCount() {
		fTotalCount++;
	}

	public int getTypeCode() {
		return fTypeCode;
	}

	public void setTypeCode(int typeCode) {
		if ((typeCode == TC_STRING) || (typeCode == TC_DOUBLE)
				|| (typeCode == TC_DATE) || (typeCode == TC_INTEGER)) {
			fTypeCode = typeCode;
		} else {
			fTypeCode = TC_UNSET;
		}
	}

	public static String codeToType(int code) {
		if (code == TC_STRING) {
			return TN_STRING;
		} else if (code == TC_DATE) {
			return TN_DATE;
		} else if (code == TC_INTEGER) {
			return TN_INTEGER;
		} else if (code == TC_DOUBLE) {
			return TN_DOUBLE;
		} else {
			return TN_UNSET;
		}
	}

	public String getType() {
		return codeToType(fTypeCode);
	};

	public String getKDEType() {
		if (fTypeCode == TC_UNSET) {
			return "string";
		} else {
			return codeToType(fTypeCode);
		}
	};

	public String getSQLType(String driver) {
		if (driver.toUpperCase().startsWith("ORACLE")) {
			if (fTypeCode == TC_STRING) {
				return "varchar2";
			} else if (fTypeCode == TC_DATE) {
				return "date";
			} else if (fTypeCode == TC_INTEGER) {
				return "integer";
			} else if (fTypeCode == TC_DOUBLE) {
				return "double precision";
			} else if (fTypeCode == TC_UNSET) {
				return "char";
			} else {
				return TN_UNSET;
			}
		} else {
			if (fTypeCode == TC_STRING) {
				return "varchar";
			} else if (fTypeCode == TC_DATE) {
				return "datetime";
			} else if (fTypeCode == TC_INTEGER) {
				return "integer";
			} else if (fTypeCode == TC_DOUBLE) {
				return "float";
			} else if (fTypeCode == TC_UNSET) {
				return "char";
			} else {
				return TN_UNSET;
			}
		}
	};

	public String getSQLSize() {
		if (fTypeCode == TC_STRING) {
			return "(" + fLength + ")";
		} else if (fTypeCode == TC_UNSET) {
			return "(1)";
		} else
			return "";
	}

	public void setType(String type) {
		type = type.toLowerCase();
		if (type.equals(TN_STRING)) {
			fTypeCode = TC_STRING;
		} else if (type.equals(TN_DOUBLE)) {
			fTypeCode = TC_DOUBLE;
		} else if (type.equals(TN_DATE)) {
			fTypeCode = TC_DATE;
		} else if (type.equals(TN_INTEGER)) {
			fTypeCode = TC_INTEGER;
		} else {
			fTypeCode = TC_STRING;
		}
	}

	public void setPattern(String pattern) {
		fDatePattern = pattern;
		fTypeCode = TC_DATE;
		fDatePatternAmbiguous = false;
	}

	public String getPattern() {
		switch (fTypeCode) {
		case TC_DATE:
			return fDatePattern;
		case TC_DOUBLE:
		case TC_INTEGER:
		case TC_STRING:
			return new Integer(fLength).toString();
		case TC_UNSET:
			return "";
		}
		return "";
	}

	public int getMaxSize() {
		return fLength;
	}

	// Query Properties
	public boolean isInteger() {
		return fTypeCode == TC_INTEGER;
	}

	public boolean isDouble() {
		return fTypeCode == TC_DOUBLE;
	}

	public boolean isString() {
		return fTypeCode == TC_STRING;
	}

	public boolean isDate() {
		return fTypeCode == TC_DATE;
	}

	public boolean isUnset() {
		return fTypeCode == TC_UNSET;
	}

	public boolean isNumeric() {
		return isInteger() || isDouble();
	}

	/**
	 * TestType checks whether the parseValue can be parsed as a member of the
	 * current type, and also puts the parsed value into a cached private object
	 * called localLastParse.
	 */
	public boolean testType(String parseValue, boolean resize) {
		int l = parseValue.trim().length();
		if (l > fLength && resize) {
			fLength = l;
		}
		// if (parseValue.equals(localLastInput)) {
		// return (localLastParse != null);
		// }
		localLastParse = null;
		localLastInput = parseValue;
		if (checkBlank(parseValue)) {
			fIsNullSoFar = true;
			return true;
		} else if (isString()) {
			localLastParse = parseValue;
			return true;
		} else if (isDouble()) {
			localLastParse = getDouble(parseValue);
			return (localLastParse != null);
		} else if (isInteger()) {
			localLastParse = getInteger(parseValue);
			return (localLastParse != null);
		} else if (isDate()) {
			if (fDatePattern.equals("")) {
				if (checkIntDate(parseValue)) {
					fDatePattern = DP_INTEGER;
					fDatePatternAmbiguous = true;
					localLastParse = getDate(parseValue, fDatePattern);
					return true;
				}
				if (checkDate(parseValue, DP_TIMESTAMP)) {
					fDatePattern = DP_TIMESTAMP;
					fDatePatternAmbiguous = false;
					localLastParse = getDate(parseValue, fDatePattern);
					return true;
				}
				if (checkDate(parseValue, DP_TIMESTAMP2)) {
					fDatePattern = DP_TIMESTAMP2;
					fDatePatternAmbiguous = false;
					localLastParse = getDate(parseValue, fDatePattern);
					return true;
				}
				if (checkDate(parseValue, DP_FULL)) {
					fDatePattern = DP_FULL;
					fDatePatternAmbiguous = false;
					localLastParse = getDate(parseValue, fDatePattern);
					return true;
				}
				if (checkDate(parseValue, DP_HYPHEN)) {
					fDatePattern = DP_HYPHEN;
					fDatePatternAmbiguous = false;
					localLastParse = getDate(parseValue, fDatePattern);
					return true;
				}
				if (checkDate(parseValue, DP_DOT)) {
					fDatePattern = DP_DOT;
					fDatePatternAmbiguous = false;
					localLastParse = getDate(parseValue, fDatePattern);
					return true;
				}
				if (checkDate(parseValue, DP_US)) {
					fDatePattern = DP_US;
					fDatePatternAmbiguous = checkDate(parseValue, DP_EUROPE);
					localLastParse = getDate(parseValue, fDatePattern);
					return true;
				}
				if (checkDate(parseValue, DP_EUROPE)) {
					fDatePattern = DP_EUROPE;
					fDatePatternAmbiguous = false; // because DP_US is
													// attempted first
					localLastParse = getDate(parseValue, fDatePattern);
					return true;
				}
				localLastInput = null;
				return false;
			} else if (fDatePatternAmbiguous) {
				if (fDatePattern.equals(DP_INTEGER)) {
					localLastParse = getDate(parseValue, fDatePattern);
					return (localLastParse != null);
				} else if (fDatePattern.equals(DP_US)) {
					if (checkDate(parseValue, DP_US)) {
						fDatePattern = DP_US;
						fDatePatternAmbiguous = checkDate(parseValue, DP_EUROPE);
						localLastParse = getDate(parseValue, fDatePattern);
						return (localLastParse != null);
					} else if (checkDate(parseValue, DP_EUROPE)) {
						fDatePattern = DP_EUROPE;
						fDatePatternAmbiguous = false;
						localLastParse = getDate(parseValue, fDatePattern);
						return (localLastParse != null);
					} else {
						localLastInput = null;
						return false;
					}
				}
			} else { /* Date, not ambiguous */
				localLastInput = parseValue;
				localLastParse = getDate(parseValue, fDatePattern);
				return (localLastParse != null);
			}
		} else if (isUnset() & checkBlank(parseValue)) {
			return true;
		}
		return false;
	}

	/**
	 * Parses the parseValue and returns the typed object. Relies on testType to
	 * do the parsing for it.
	 */
	public Object parse(String parseValue) {
		if (isString()) {
			return parseValue;
		} else if (isUnset() || checkBlank(parseValue)) {
			return null;
		} else {
			testType(parseValue, false);
			return localLastParse;
		}
		// else if (localLastParse == null)
		// {
		// testType(parseValue, false); // Which will fill a value (or null)
		// into localLastParse;
		// }
		// return localLastParse;
	}

	public boolean setTypeAuto(String parseValue) {
		if (!testType(parseValue, true)) {
			if (isDate() && fDatePatternAmbiguous && fDatePattern == DP_INTEGER) {
				setTypeCode(TC_INTEGER);
				setTypeAuto(parseValue);
			} else if (isDouble() || isDate()) {
				setTypeCode(TC_STRING);
			} else if (isInteger()) {
				setTypeCode(TC_DOUBLE);
				setTypeAuto(parseValue);
			} else if (isUnset()) {
				setTypeCode(TC_DATE);
				if (!testType(parseValue, true)) {
					setTypeCode(TC_INTEGER);
					setTypeAuto(parseValue);
				}
			}
			return true;
		} else {
			return false;
		}
	}

	public boolean setCheckAuto(String parseValue, boolean detectType) {
		boolean result;
		if (detectType) {
			result = setTypeAuto(parseValue);
		} else {
			result = !testType(parseValue, true);
		}
		if (result) {
			incErrorCount();
		}
		return result;
	}

	public void setCardinality(int num) {
		if (num > fMaxCardinality) {
			fMaxCardinality = num;
		}
	}

	public int getMaxCardinality() {
		return fMaxCardinality;
	}

	public String toString() {
		if (isString()) {
			return (getType() + "(" + getSQLSize() + ")");
		} else if (isDate()) {
			return (getType() + "(" + getPattern() + ")");
		} else {
			return (getType());
		}
	}
}
