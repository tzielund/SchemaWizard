package com.zielund.research.schemawizard;

import java.util.*;
import javax.swing.table.*;

@SuppressWarnings("unchecked")
public class MyTableModel extends AbstractTableModel
{
	public ArrayList data = new java.util.ArrayList(10);
	protected int fColumnCount = 0;

	public void setColumnCount(int c)
	{
		fColumnCount = c;
		data = new java.util.ArrayList(c);
		fireTableStructureChanged();
	}

	public Class getColumnClass(int columnindex)
	{
		return String.class;
	}

	public int getColumnCount()
	{
		if (fColumnCount == 0)
		{
			return 10;
		} else
		{
			return fColumnCount;
		}
	}

	public int getRowCount()
	{
		if (fColumnCount == 0)
		{
			return 10;
		} else
		{
			return data.size() / fColumnCount;
		}
	}

	public Object getValueAt(int row, int col)
	{
		if (fColumnCount == 0)
		{
			return new Integer(row * col);
		} else
		{
			// return data.toArray()[col+row*getColumnCount()];
			if (data.size() < col + row * getColumnCount())
			{
				return "<missing>";
			} else
			{
				return data.get(col + row * getColumnCount());
			}
		}
	}

	public void addValue(String value)
	{
		data.add(value);
	}

	public void clear()
	{
		data = new ArrayList(10);
		fireTableStructureChanged();
	}
}
