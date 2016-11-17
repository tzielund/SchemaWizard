package com.zielund.research.schemawizard;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

import com.zielund.research.schemawizard.util.DotCounter;

public class STSAnalyzer {

	static ArrayList<MyStatistic> f_stats = new ArrayList<MyStatistic>();
	static HashMap<String,String> f_fieldType = new HashMap<String,String>();
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String filename = args[0];
		System.err.println("Analyzing " + filename);
		String stsFile = args[1];
		readSTSFile(stsFile);
		BufferedReader br;
		if (filename.endsWith(".gz")) {
			br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(new File(filename)))));
		} else {
			br = new BufferedReader(new FileReader(new File (filename)));
		}
		String line;
		line = br.readLine();
		parseLabels(line);
		DotCounter dc = new DotCounter();
		while ((line = br.readLine()) != null) {
			dc.inc();
			String[] fields = line.split("\\t");
			for (int i = 0; i < fields.length; i++) {
				String thisVal = fields[i];
				if (thisVal.startsWith("\"") && thisVal.endsWith("\"")) {
					thisVal = thisVal.substring(1,thisVal.length()-1);
				}
				f_stats.get(i).add(thisVal);
			}
		}
		br.close();
		dc.finished();
		PrintStream out = new PrintStream(new FileOutputStream(new File(filename + ".stsAnalyzer")));
		for (int i = 0; i < f_stats.size(); i++) {
			MyStatistic ms = f_stats.get(i);
			System.out.println(ms.toStringLong());
			out.println(ms.toStringLong());
		}
		out.close();
	}


	private static void parseLabels(String line) {
		String[] fields = line.split("\\t");
		for (int i = 0; i < fields.length; i++) {
			String typeCode = f_fieldType.get(fields[i]);
			MyTypeCheck mtc = new MyTypeCheck(MyTypeCheck.TC_STRING);
			if (typeCode != null && typeCode.contains("F")) {
				mtc = new MyTypeCheck(MyTypeCheck.TC_DOUBLE);
			}
			mtc.setTrueName(fields[i]);
			f_stats.add(new MyStatistic(mtc));
		}
	}

	private static void readSTSFile(String stsFile) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File(stsFile)));
		String line;
		boolean inVars = false;
		while ((line = br.readLine()) != null) {
			if (inVars) {
				line = line.trim();
				String[] fields = line.split("[ \\t]+");
				if (fields.length > 0 && fields[0].length() > 0) {
					String fieldName = fields[0];
					String typeCode = fields[1];
					f_fieldType.put(fieldName, typeCode);
				}
			} else {
				if (line.matches("^VARIABLES")) {
					inVars = true;
				}
			}
		}
	}

}
