package com.zielund.research.schemawizard;

public class MyStatsTableModel extends MyMetaTableModel
{
	public static final int ROW_NAME = 0;
	public static final int ROW_NULLCOUNT = 1;
	public static final int ROW_TYPE = 2;
	public static final int ROW_ERRORS = 3;
	public static final int ROW_MIN = 4;
	public static final int ROW_MAX = 5;
	public static final int ROW_MEAN = 6;
	public static final int ROW_DISTINCTCOUNT = 7;
	public static final int ROW_NOTNULL = 8;
	public static final int ROW_SORTING = 9;
	public static final int ROW_HIST1 = 10;

	int m_numHistRows = 50;

	public MyStatsTableModel()
	{
	}

	public void addField(MyTypeCheck mtc)
	{
		typeList.add(new MyStatistic(mtc));
		fireTableStructureChanged();
	}

	public void addField(MyStatistic field)
	{
		typeList.add(field);
		fireTableStructureChanged();
	}

	public void addValue(String value, int col)
	{
		MyStatistic myType = (MyStatistic) typeList.get(col);
		myType.add(value);
		fireTableDataChanged();
	}

	public int getRowCount()
	{
		return 15;
	}

	public Object getValueAt(int row, int col)
	{
		MyStatistic myType;
		if (col == 0)
		{
			switch (row)
			{
			case ROW_NAME:
				return "Name";
			case ROW_NULLCOUNT:
				return "Num Nulls";
			case ROW_TYPE:
				return "Type";
			case ROW_ERRORS:
				return "Error Count";
			case ROW_MIN:
				return "Min Value";
			case ROW_MAX:
				return "Max Value";
			case ROW_MEAN:
				return "Mean Value";
			case ROW_DISTINCTCOUNT:
				return "Distinct Count";
			case ROW_NOTNULL:
				return "Special Props";
			case ROW_SORTING:
				return "Ordering";
			case ROW_HIST1:
				return "Hist #1";
			default:
			{
				if (row < ROW_HIST1 + m_numHistRows)
					return "Hist #" + (row - ROW_HIST1);
				else
					return "Unknown";
			}
			}
		}
		if (col - 1 > typeList.size())
		{
			myType = new MyStatistic(new MyTypeCheck("Example Column " + col, ""));
		} else
		{
			myType = (MyStatistic) typeList.get(col - 1);
		}
		switch (row)
		{
		case ROW_NAME:
			return myType.getName();
		case ROW_NULLCOUNT:
			return myType.getNullCount();
		case ROW_TYPE:
			return myType.getType();
		case ROW_ERRORS:
			return myType.getErrorCount();
		case ROW_MIN:
			return myType.getMin();
		case ROW_MAX:
			return myType.getMax();
		case ROW_MEAN:
			return myType.getMean();
		case ROW_DISTINCTCOUNT:
			return myType.getCountDistinct();
		case ROW_SORTING:
			return myType.getOrdering();
		case ROW_NOTNULL:
			return myType.getSpecialProps();
		case ROW_HIST1:
			return myType.getHistogram(0);
		default:
			if (row < ROW_HIST1 + m_numHistRows)
				return myType.getHistogram(row - ROW_HIST1);
			else
				return "";
		}
	}

	public boolean isCellEditable(int row, int col)
	{
		return false;
	}

	public void setValueAt(Object aValue, int row, int col)
	{
	}

	public int useLessMem()
	{
		int j = 0;
		long maxSize = 0;
		for (int i = 0; i < typeList.size(); i++)
		{
			if (((MyStatistic) typeList.get(i)).getlongCountDistinct() > maxSize)
			{
				j = i;
				maxSize = ((MyStatistic) typeList.get(i)).getlongCountDistinct();
			}
		}
		((MyStatistic) typeList.get(j)).DisableDist();
		return j;
	}

	public void checkMem()
	{
		Runtime r = Runtime.getRuntime();
		long before, during, after;
		long total = r.totalMemory();
		before = r.freeMemory();
		if (before < 1048576 || (before / total < 0.1))
		{
			r.gc();
			during = r.freeMemory();
			if (during < 1048576)
			{
				int j = useLessMem();
				r.gc();
				after = r.freeMemory();
				System.err.println("Disabled column " + j + " increasing  memory from " + during + " to " + after);
			}
		}
	}

	public void reset()
	{
		for (int i = 0; i < typeList.size(); i++)
		{
			MyStatistic ms = (MyStatistic) typeList.get(i);
			ms.reset();
		}
	}

	public void finish()
	{
		for (int i = 0; i < typeList.size(); i++)
		{
			MyStatistic ms = (MyStatistic) typeList.get(i);
			ms.finish();
		}
	}
}