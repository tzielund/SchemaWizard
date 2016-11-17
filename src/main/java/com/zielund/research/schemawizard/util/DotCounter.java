/*
 * Copyright (c) 2004 Thomson West
 *   UNPUBLISHED--ALL RIGHTS RESERVED UNDER THE COPYRIGHT LAWS.
 *   THE INFORMATION CONTAINED HEREIN IS PROPRIETARY, TRADE SECRET
 *   INFORMATION.  ACCESS TO SAID INFORMATION IS STRICTLY LIMITED
 *   TO SPECIFICALLY AUTHORIZED PERSONNEL.  SAID INFORMATION
 *   SHALL BE KEPT STRICTLY CONFIDENTIAL AND SECRET. 
 * 
 * Created on Oct 27, 2004
 */

package com.zielund.research.schemawizard.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple utility component to help tell how far a process has gone. Dots will be printed to System.out.
 * 
 * @author thomas.zielund@thomson.com
 * 
 */
public class DotCounter
{
	public static final int NO_STEP = 0;
	public static final int MINOR_STEP = 1;
	public static final int MAJOR_STEP = 2;
	public static final int FINAL_STEP = 3;
	public static final int SILENT = 4;
	public static final String RESULT = "RESULT";

	private long m_counter = 0;
	private long m_major = 10;
	private long m_minor = 0;
	private long m_minorStep = 1;
	private int m_feedbackLevel = MINOR_STEP;
	private final long m_feedbackInterval;
	private final long m_startTime = new Date().getTime();
	private long m_finishTime = 0;
	private long m_majorStepTime = m_startTime;

	/**
	 * Increments the counter by 1 and provides feedback as a string on minor and major steps
	 * 
	 * @param anInfoStruct
	 *            is information to display at major steps
	 * @return a string with text output
	 */
	public int inc(Map<String, ?> anInfoStruct)
	{
		int result = NO_STEP;
		m_counter++;
		if (m_feedbackInterval > 0)
		{
			if (m_counter >= m_major)
			{
				result = majorStep(anInfoStruct);
				m_major = m_counter + m_feedbackInterval;
			}
			return result;
		}
		if (m_counter > m_minor)
		{
			result = minorStep();
		}
		if (m_counter > m_major)
		{
			result = majorStep(anInfoStruct);
		}
		return result;
	}

	/**
	 * Notes that the count has past an order-of-magnitude increment
	 * @return
	 */
	private int majorStep(Map<String, ?> anInfoStruct)
	{
		output(String.valueOf(m_major), MAJOR_STEP);
		if (anInfoStruct == null || anInfoStruct.size() == 0)
		{
			long now = new Date().getTime();
			double duration = (now - m_majorStepTime) / 1000.0;
			output("(totalSeconds=" + duration, MAJOR_STEP);
			output(", recordsPerSec=" + m_major / duration + ")", MAJOR_STEP);
		} else
		{
			output(iisToString(anInfoStruct), MAJOR_STEP);
		}
		output("\n", MAJOR_STEP);
		m_minor = m_major;
		m_minorStep = m_major;
		m_major *= 10;
		return MAJOR_STEP;
	}

	/**
     * 
     */
	private int minorStep()
	{
		output(("."), MINOR_STEP);
		m_minor += m_minorStep;
		return MINOR_STEP;
	}

	/**
	 * @param string
	 * @param minor_step2
	 */
	public void output(String string, int feedbackLevel)
	{
		if (m_feedbackLevel <= feedbackLevel)
		{
			System.err.print(string);
		}
	}

	public DotCounter()
	{
		m_feedbackInterval = -1;
	};

	public DotCounter(long feedbackInterval)
	{
		m_feedbackInterval = feedbackInterval;
		m_major = m_feedbackInterval;
	}

	public DotCounter(int feedbackLevel)
	{
		this();
		m_feedbackLevel = feedbackLevel;
	}

	/**
	 * Increments the counter by 1 and provides feedback as a string on minor and major steps
	 * 
	 * @return a string with text output
	 */
	public int inc()
	{
		return inc(null);
	}

	/**
	 * Prints a final report to System.err.
	 * 
	 * @param config
	 *            contains information to summarize
	 */
	@SuppressWarnings("unchecked")
	public void finished(Map config)
	{
		output(String.valueOf(m_counter), FINAL_STEP);
		m_finishTime = new Date().getTime();
		if (config.size() == 0)
		{
			double duration = (m_finishTime - m_startTime) / 1000.0;
			output("(totalSeconds=" + duration, FINAL_STEP);
			output(", recordsPerSec=" + m_counter / duration + ")", FINAL_STEP);
		} else
		{
			output(iisToString(config), FINAL_STEP);
		}
		output("\n", FINAL_STEP);
	}

	/** Prints a final report to System.err. */
	public void finished()
	{
		finished(new HashMap<String, Object>());
	}

	public long getTotalTime()
	{
		if (m_finishTime == 0)
		{
			return new Date().getTime() - m_startTime;
		}
		return m_finishTime - m_startTime;
	}

	public long getCounter()
	{
		return m_counter;
	}

	/**
	 * Formats HashMap as a string
	 * 
	 * @param iis
	 *            has information to be formatted
	 * @return string formatting
	 */
	public static String iisToString(Map<String, ?> iis)
	{
		StringBuffer sb = new StringBuffer("(");
		for (String element : iis.keySet())
		{
			sb.append(element + "=" + iis.get(element) + ", ");
		}
		sb.append(")");
		return sb.toString();
	}

	/**
	 * do nothing (required by IConfigurable).
	 * 
	 * @param configuration
	 *            is unused
	 */
	public void privateConfigure(Map<String, ?> configuration) throws Exception
	{
	}

	/**
	 * Simple demo of dotcounter behavior
	 * 
	 * @param args
	 *            is unused
	 */
	public static void main(String[] args)
	{
		DotCounter dc = new DotCounter();
		for (int i = 0; i < 32767; i++)
		{
			dc.inc();
		}
		dc.finished();
		dc = new DotCounter(1000);
		for (int i = 0; i < 32767; i++)
		{
			dc.inc();
		}
		dc.finished();
	}

}
