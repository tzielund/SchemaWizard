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

public class STSColumnDistribution {

	static ArrayList<MyStatistic> f_stats = new ArrayList<MyStatistic>();
	static HashMap<String,String> f_fieldType = new HashMap<String,String>();
	static ArrayList<String> f_validColumns = new ArrayList<String>();
	static ArrayList<Integer> f_validColIndices = new ArrayList<Integer>();
	static ArrayList<Integer> f_warnCount = new ArrayList<Integer>();
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String filename = args[0];
		System.err.println("Analyzing " + filename);
		String stsFile = args[1];
		readRemainingFields(args);
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
		PrintStream completeOutput = new PrintStream(new FileOutputStream(new File(filename + ".STSColumnDistribution")));
		while ((line = br.readLine()) != null) {
			dc.inc();
			String[] fields = line.split("\\t");
			int j = 0;
			for (int i = 0; i < fields.length; i++) {
				if (f_validColIndices.contains(i)) {
					String thisVal = fields[i];
					if (thisVal.startsWith("\"") && thisVal.endsWith("\"")) {
						thisVal = thisVal.substring(1,thisVal.length()-1);
					}
					MyStatistic ms = f_stats.get(j);
					ms.add(thisVal);
					if (Integer.valueOf(ms.getCountDistinct()) > 10000) {
						completeOutput.println(ms.toStringGiant());
						ms.reset();
						if (f_warnCount.get(j) < 3) {
							System.err.print("Rolling " + f_validColumns.get(j));
							f_warnCount.set(j, new Integer(f_warnCount.get(j).intValue()+1));
						}
					}
					j++;
				}
			}
		}
		br.close();
		
		dc.finished();
		for (int i = 0; i < f_stats.size(); i++) {
			MyStatistic ms = f_stats.get(i);
			completeOutput.println(ms.toStringGiant());
		}
		completeOutput.close();
	}


	private static void readRemainingFields(String[] args) {
		// TODO Auto-generated method stub
		for(int i = 2; i < args.length; i++) {
			Integer index = Integer.valueOf(args[i]);
			f_validColIndices.add(index);
			f_warnCount.add(new Integer(0));
		}
	}


	private static void parseLabels(String line) {
		String[] fields = line.split("\\t");
		for (int i = 0; i < fields.length; i++) {
			if (f_validColIndices.contains(i)) {
				f_validColumns.add(fields[i]);
				String typeCode = f_fieldType.get(fields[i]);
				MyTypeCheck mtc = new MyTypeCheck(MyTypeCheck.TC_STRING);
//				if (typeCode != null && typeCode.contains("F")) {
//					mtc = new MyTypeCheck(MyTypeCheck.TC_DOUBLE);
//				}
				mtc.setTrueName(fields[i]);
				f_stats.add(new MyStatistic(mtc));
			}
		}
		System.err.print("Getting distribution for columns named: ");
		for (int i = 0; i < f_validColumns.size(); i++) {
			System.err.print (f_validColumns.get(i)+" ");
		}
		System.err.println();
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
