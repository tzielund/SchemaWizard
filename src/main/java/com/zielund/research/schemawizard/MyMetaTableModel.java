package com.zielund.research.schemawizard;

import java.util.*;
import javax.swing.table.*;
import java.io.*;

@SuppressWarnings("unchecked")
public class MyMetaTableModel extends AbstractTableModel
{
	public static final int ROW_GIVENNAME = 0;
	public static final int ROW_TRUENAME = 1;
	public static final int ROW_TYPE = 2;
	public static final int ROW_PATTERN = 3;
	public static final int ROW_ERRORS = 4;

	protected ArrayList typeList = new ArrayList(10);
	protected boolean fLockDisplay = false;

	public boolean getLockDisplay()
	{
		return fLockDisplay;
	}

	public void setLockDisplay(boolean ld)
	{
		if (fLockDisplay && !ld)
		{
			fLockDisplay = false;
			super.fireTableDataChanged();
		}
		fLockDisplay = ld;
	}

	public void fireTableCellUpdated(int row, int column)
	{
		if (!fLockDisplay)
		{
			super.fireTableCellUpdated(row, column);
		}
	}

	public void fireTableDataChanged()
	{
		if (!fLockDisplay)
		{
			super.fireTableDataChanged();
		}
	}

	public void fireTableRowsUpdated(int sr, int er)
	{
		if (!fLockDisplay)
		{
			super.fireTableRowsUpdated(sr, er);
		}
	}

	public int getColumnCount()
	{
		if (typeList.size() == 0)
		{
			return 10;
		} else
		{
			return typeList.size() + 1;
		}
	}

	public int getRowCount()
	{
		return 5;
	}

	public Object getValueAt(int row, int col)
	{
		MyTypeCheck myType;
		if (col == 0)
		{
			switch (row)
			{
			case ROW_GIVENNAME:
				return "Given Name";
			case ROW_TRUENAME:
				return "True Name";
			case ROW_TYPE:
				return "Type";
			case ROW_PATTERN:
				return "Size/Pattern";
			case ROW_ERRORS:
				return "Errors";
			}
		}
		if (typeList.size() == 0)
		{
			myType = new MyTypeCheck("Example Column " + col, "pattern");
		} else
		{
			myType = (MyTypeCheck) typeList.get(col - 1);
		}
		switch (row)
		{
		case ROW_GIVENNAME:
			return myType.getGivenName();
		case ROW_TRUENAME:
			return myType.getTrueName();
		case ROW_TYPE:
			return myType.getType();
		case ROW_PATTERN:
			return myType.getPattern();
		case ROW_ERRORS:
			if (myType.getErrorCount() == 0)
			{
				return "";
			} else
			{
				return "" + myType.getErrorCount();
			}
		}
		return "";
	}

	public String getGivenName(int col)
	{
		MyTypeCheck myType = (MyTypeCheck) typeList.get(col);
		return myType.getGivenName();
	}

	public String getTrueName(int col)
	{
		MyTypeCheck myType = (MyTypeCheck) typeList.get(col);
		return myType.getTrueName();
	}

	public void addField(MyTypeCheck myType)
	{
		typeList.add(myType);
		fireTableStructureChanged();
	}

	public MyTypeCheck getField(int col)
	{
		return (MyTypeCheck) typeList.get(col);
	}

	public int getFieldCount()
	{
		return getColumnCount() - 1;
	}

	public boolean isCellEditable(int row, int col)
	{
		if ((col > 0) && (col <= typeList.size()) && (!(row == ROW_GIVENNAME || row == ROW_ERRORS)))
		{
			return true;
		} else
		{
			return false;
		}
	}

	public void setValueAt(Object aValue, int row, int col)
	{
		MyTypeCheck myType = (MyTypeCheck) typeList.get(col - 1);
		switch (row)
		{
		case ROW_GIVENNAME:
		{/* do nothing */
		}
			break;
		case ROW_TRUENAME:
		{
			myType.setTrueName(aValue.toString());
		}
			break;
		case ROW_TYPE:
		{
			myType.setType(aValue.toString());
		}
			break;
		case ROW_PATTERN:
		{
			myType.setPattern(aValue.toString());
		}
			break;
		case ROW_ERRORS:
		{
		}
			break;
		}
		this.fireTableCellUpdated(row, col);
	}

	public void setColumnName(int col, String value)
	{
		this.setValueAt(value, ROW_TRUENAME, col);
	}

	public boolean testType(String parseValue, int col, boolean resize)
	{
		MyTypeCheck mtc = (MyTypeCheck) typeList.get(col);
		return mtc.testType(parseValue, resize);
	}

	public boolean setCheckAuto(String parseValue, int col, boolean detectTypes)
	{
		MyTypeCheck mtc = (MyTypeCheck) typeList.get(col);
		boolean result = mtc.setCheckAuto(parseValue, detectTypes);
		if (result)
		{
			fireTableCellUpdated(ROW_TYPE, col);
		}
		if (result)
		{
			fireTableCellUpdated(ROW_ERRORS, col);
		}
		return result;
	}

	public void setGivenName(String newName, int col)
	{
		MyTypeCheck mtc = (MyTypeCheck) typeList.get(col);
		mtc.setGivenName(newName);
		fireTableCellUpdated(ROW_GIVENNAME, col);
		fireTableCellUpdated(ROW_TRUENAME, col);
	}

	public void resetErrorStats()
	{
		for (int i = 0; i < typeList.size(); i++)
		{
			MyTypeCheck mtc = (MyTypeCheck) typeList.get(i);
			mtc.setErrorCount(0);
		}
		fireTableRowsUpdated(ROW_ERRORS, ROW_ERRORS);
	}

	public void clear()
	{
		typeList = new ArrayList(10);
		fireTableStructureChanged();
	}

	public void writeSQLLDRFile(String filename, String tableName, RecordSplitter rs)
	{
		try
		{
			BufferedWriter bw1 = new BufferedWriter(new PrintWriter(new FileWriter(filename)));
			bw1.write("LOAD DATA\nREPLACE\nINTO TABLE " + tableName + "\n");
			if (rs.isDelimited())
			{
				bw1.write("FIELDS TERMINATED BY '" + rs.getDelimStr() + "'");
				if (rs.isQuoteEnclosed())
				{
					bw1.write("OPTIONALLY ENCLOSED BY '\"'");
				}
				bw1.write("\n");
			}
			bw1.write("trailing nullcols\n(\n");
			int[] tabs =
			{};
			if (rs.isFixed())
			{
				tabs = rs.getTabStops();
			}
			for (int i = 0; i < typeList.size(); i++)
			{
				MyTypeCheck mtc = (MyTypeCheck) getField(i);
				bw1.write("\t" + mtc.getTrueName() + " ");
				if (rs.isFixed())
				{
					bw1.write("POSITION(" + tabs[i] + 1 + ":" + tabs[i + 1] + 1 + ") ");
				}
				if (mtc.isNumeric())
				{
					bw1.write("integer external");
				}
				if (mtc.isDate())
				{
					bw1.write(" DATE \"" + mtc.getPattern() + "\" ");
				}
				if (i < typeList.size())
				{
					bw1.write(",");
				}
				bw1.newLine();
			}
			bw1.write(")");
			bw1.flush();
			bw1.close();
		} catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
	}

}
