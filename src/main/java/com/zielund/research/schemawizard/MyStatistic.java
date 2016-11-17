package com.zielund.research.schemawizard;

import java.util.*;
import java.text.*;

@SuppressWarnings("unchecked")
public class MyStatistic {
	private static final int MAX_DISTRIBUTION_SIZE = 1000000;

	MyTypeCheck fMtc;
	SimpleDateFormat sdf;
	int fCount;
	int fNullCount;
	int fErrorCount;
	Object fMin;
	Object fMax;
	Object lastValue;
	boolean fDescending = true;
	boolean fAscending = true;
	boolean fClustered = true;
	Hashtable<Object,Long> distValues = new Hashtable<Object,Long>();
	String finishedDistValues = "";
	String finishedOrdering = "";
	String finishedSpecialProps = "";
	boolean finished = false;
	ArrayList topN = new ArrayList(50);
	ArrayList topNVal = new ArrayList(50);
	boolean distEnabled = true;
	double fSumValue = 0;
	double fSumSquareValue = 0;

	public MyStatistic(MyTypeCheck mtc) {
		fMtc = mtc;
		if (fMtc.getTypeCode() == MyTypeCheck.TC_DATE) {
			sdf = new SimpleDateFormat(fMtc.getPattern());
		}
	}

	public void setHistogramSize(int size) {
		while (size > topN.size()) {
			topN.remove(size);
		}
		while (size > topNVal.size()) {
			topN.remove(size);
		}
	}

	public String getName() {
		return fMtc.getTrueName();
	}

	public boolean isDistEnabled() {
		return distEnabled;
	}

	public void DisableDist() {
		distEnabled = false;
		distValues = null;
	}

	public String getType() {
		return fMtc.getType();
	}

	public String getNullCount() {
		if (fNullCount == 0) {
			return "";
		} else {
			return new Integer(fNullCount).toString();
		}
	}

	public String getErrorCount() {
		if (fErrorCount == 0) {
			return "";
		} else {
			return new Integer(fErrorCount).toString();
		}
	}

	public String getCount() {
		return new Integer(fCount).toString();
	}

	public String getMin() {
		if (fMin == null) {
			return "";
		}
		if (fMtc.isDate()) {
			if (sdf == null) {
				sdf = new SimpleDateFormat(fMtc.getPattern());
			}
			return sdf.format(fMin);
		}
		return fMin.toString();
	}

	public String getMax() {
		if (fMax == null) {
			return "";
		}
		if (fMtc.isDate()) {
			if (sdf == null) {
				sdf = new SimpleDateFormat(fMtc.getPattern());
			}
			return sdf.format(fMax);
		}
		return fMax.toString();
	}

	public long getlongCountDistinct() {
		if (distValues == null) {
			return 0;
		}
		return distValues.size();
	}

	public String getCountDistinct() {
		if (finished) {
			return finishedDistValues;
		}
		if (distEnabled) {
			return new Integer(distValues.size()).toString();
		} else {
			return "";
		}
	}

	public String getMean() {
		if (!fMtc.isNumeric()) {
			return "";
		} else {
			return new Double(fSumValue / fCount).toString();
		}
	}

	public String getStdDev() {
		if (!fMtc.isNumeric()) {
			return "";
		} else {
			return new Double(fSumSquareValue
					- ((fSumValue * fSumValue) / fCount) / (fCount - 1))
					.toString();
		}
	}

	public String getOrdering() {
		if (finished) {
			return finishedOrdering;
		}
		if (fAscending && fDescending) {
			if (fCount == 0) {
				if (fNullCount == 0) {
					return "No Data Yet";
				} else {
					return "Null";
				}
			} else {
				if (fNullCount == 0) {
					return "One Value";
				} else {
					return "Unary";
				}
			}
		} else if (fAscending) {
			return "ASC";
		} else if (fDescending) {
			return "DESC";
		} else if (!distEnabled) {
			return "Unord or Clust";
		} else if (fClustered) {
			return "Clustered";
		} else {
			return "Unordered";
		}
	}

	public Boolean isUnique() {
		if (!distEnabled) {
			return null;
		} else {
			return new Boolean(fNullCount < 2
					&& distValues.size() + fNullCount == fCount);
		}
	}

	public boolean isNotNull() {
		return fNullCount == 0;
	}

	public String getSpecialProps() {
		if (finished) {
			return finishedSpecialProps;
		}
		String s = "";
		if (!distEnabled) {
			s += "?uniqe ";
		} else {
			if (isUnique().booleanValue()) {
				s += "Unique ";
			}
		}
		if (isNotNull()) {
			s += "Not Null";
		}
		return s;
	}

	public String getHistogram(int i) {
		if (!distEnabled || i < 0 || i >= topN.size()) {
			return "";
		} else {
			String s = "";
			if (fMtc.isDate()) {
				if (sdf == null) {
					sdf = new SimpleDateFormat(fMtc.getPattern());
				}
				s = sdf.format((Date) topN.get(i));
			} else {
				s = topN.get(i).toString();
			}
			s += " (" + topNVal.get(i).toString() + ")";
			return s;
		}
	}

	int compareTo(Object V1, Object V2) {
		if (V1 == null) {
			return 0;
		}
		if (fMtc.isDouble()) {
			return ((Double) V1).compareTo((Double) V2);
		} else if (fMtc.isInteger()) {
			return ((Integer) V1).compareTo((Integer) V2);
		} else if (fMtc.isDate()) {
			return ((Date) V1).compareTo((Date) V2);
		} else if (fMtc.isString()) {
			return ((String) V1).compareTo((String) V2);
		} else {
			return 0;
		}
	}

	public void add(String value) {
		if (value.equals("")) {
			fNullCount++;
			return;
		} else {
			Object Value = fMtc.parse(value);
			if (Value == null) {
				fErrorCount++;
				return;
			}
			fCount++;
			if (fMtc.isDouble()) {
				fSumValue += ((Double) Value).doubleValue();
				fSumSquareValue += ((Double) Value).doubleValue()
						* ((Double) Value).doubleValue();
			} else if (fMtc.isInteger()) {
				fSumValue += ((Integer) Value).doubleValue();
				fSumSquareValue += ((Integer) Value).doubleValue()
						* ((Integer) Value).doubleValue();
			}
			if (fMin == null || fMax == null || fCount == 0) {
				fMin = Value;
				fMax = Value;
			}

			if (compareTo(Value, fMin) < 0) {
				fMin = Value;
			}
			if (compareTo(Value, fMax) > 0) {
				fMax = Value;
			}
			if ((fAscending || fDescending) && lastValue != null
					&& !lastValue.equals(Value)) {
				if (fAscending && compareTo(lastValue, Value) > 0) {
					fAscending = false;
				}
				if (fDescending && compareTo(lastValue, Value) < 0) {
					fDescending = false;
				}
			}
			if (distEnabled) {
				if (distValues.containsKey(Value)) {
					if (fClustered && !Value.equals(lastValue)) {
						fClustered = false;
					}
					long x = (distValues.get(Value)).longValue() + 1;
					Long X = new Long(x);
					int i = topN.size();
					while (i > 0
							&& (X.compareTo((Long) topNVal.get(i - 1)) > 0)) {
						i--;
					}
					if (i < topN.size()) {
						int j;
						if (topN.contains(Value)) {
							j = topN.indexOf(Value);
							if (j >= i) {
								j++;
							}
						} else {
							j = topN.size();
						}
						topN.add(i, Value);
						topNVal.add(i, X);
						try {
							topN.remove(j);
							topNVal.remove(j);
						} catch (IndexOutOfBoundsException ioobe) {
							System.err.println(Value.toString() + " "
									+ ioobe.toString());
						}
					}
					distValues.put(Value, X);
				} else {
					Long X = new Long(1);
					if (topN.size() < 50) {
						topN.add(Value);
						topNVal.add(X);
					}
					distValues.put(Value, X);
//					if (distValues.size() > MAX_DISTRIBUTION_SIZE) {
//						DisableDist();
//					}
				}
			}
			lastValue = Value;
		}
	}

	public void finish() {
		finishedDistValues = getCountDistinct();
		finishedSpecialProps = getSpecialProps();
		finishedOrdering = getOrdering();
		finished = true;
		if (distEnabled) {
			distValues.clear();
		}
	}

	public void reset() {
		fCount = 0;
		fNullCount = 0;
		fErrorCount = 0;
		fMin = null;
		fMax = null;
		lastValue = null;
		fDescending = true;
		fAscending = true;
		fClustered = true;
		distValues.clear();
		topN = new ArrayList(50);
		topNVal = new ArrayList(50);
		distEnabled = true;
		fSumValue = 0;
		fSumSquareValue = 0;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("[");
		sb.append("Range=" + getMin() + "-" + getMax() + "; ");
		sb.append("Average=" + getMean() + "; ");
		sb.append("Distinct Count=" + getCountDistinct() + "; ");
		// sb.append("Histogram" + "; ")
		sb.append("Other=" + getSpecialProps());
		sb.append("]");
		return sb.toString();
	}

	public String toStringLong() {
		StringBuffer sb = new StringBuffer(getName() + ":[");
		sb.append("Type=" + fMtc.getType() + "; ");
		sb.append("N=" + getCount() + "; ");
		sb.append("Null Count=" + getNullCount() + "; ");
		sb.append("Range=" + getMin() + "-" + getMax() + "; ");
		sb.append("Average=" + getMean() + "; ");
		sb.append("StdDev=" + getStdDev() + "; ");
		sb.append("Distinct Count=" + getCountDistinct() + "; ");
		sb.append("Other=" + getSpecialProps() + "; ");
		sb.append("Histogram:{");
		for (int i = 0; i < topN.size(); i++) {
			sb.append(i);
			sb.append(":");
			sb.append(getHistogram(i));
			sb.append("|");
		}
		sb.append("}");
		sb.append("]");
		return sb.toString();
	}

	public String toStringGiant() {
		StringBuffer sb = new StringBuffer();
		for (Iterator iterator = distValues.keySet().iterator(); iterator.hasNext();) {
			Object key = iterator.next();
			sb.append(getName() + "\t");
			sb.append(key.toString() + "\t");
			sb.append(distValues.get(key).toString() + "\n");
		}
		return sb.toString();
	}
}
