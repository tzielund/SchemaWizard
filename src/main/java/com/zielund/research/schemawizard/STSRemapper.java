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
import java.util.zip.GZIPOutputStream;

import com.zielund.research.schemawizard.util.DotCounter;

public class STSRemapper {

	public static void main (String[] argv) throws IOException {
		System.err.println("STSRemapper " + argv[0] + argv[1]);
		readSTSExpected (argv[0]);
		String stsActual = argv[1] + ".sts";
		readSTSActual (stsActual);
		rewriteData(argv[1]);
	}

	private static HashMap<String,Integer> fieldPos = new HashMap<String,Integer>();
	private static ArrayList<String> fieldNames = new ArrayList<String>();
	private static ArrayList<Integer> inToOutField = new ArrayList<Integer>();
//	private static ArrayList<MyStatistic> fieldStats;
	
	private static void readSTSExpected(String string) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File(string)));
		String line;
		while ((line = br.readLine()) != null) {
			String[] fields = line.split("\\,");
			if (fields.length == 1) {
				fieldPos.put(fields[0],fieldPos.size());
				fieldNames.add(fields[0]);
			}
		}
		br.close();
		System.err.println("Completed reading expected fields:");
		for (int i = 0; i < fieldNames.size()-1; i++) {
			System.err.print(fieldNames.get(i) + "\t");		
		}
		System.err.println(fieldNames.get(fieldNames.size()-1));
//		fieldStats = new ArrayList<MyStatistic>(fieldNames.size());
//		for (int i = 0; i < fieldNames.size(); i++) {
//			fieldStats.add(new MyStatistic(new MyTypeCheck(MyTypeCheck.TC_UNSET)));
//		}
	}
	
	private static void readSTSActual(String string) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File(string)));
		String line;
		boolean inVars = false;
		while ((line = br.readLine()) != null) {
			if (inVars) {
				line = line.trim();
				String[] fields = line.split("[ \\t]+");
				if (fields.length > 0 && fields[0].length() > 0) {
					if (fieldPos.get(fields[0]) == null) {
						throw new RuntimeException("Couldn't find '" + fields[0] + "'");
					}
					int mapToField = fieldPos.get(fields[0]);
					inToOutField.add(mapToField);
//					String typeCode = fields[1];
//					MyTypeCheck mtc = new MyTypeCheck(MyTypeCheck.TC_STRING);
//					if (typeCode.contains("F")) {
//						mtc = new MyTypeCheck(MyTypeCheck.TC_DOUBLE);
//					}
//					mtc.setTrueName(fields[0]);
//					fieldStats.set(mapToField, new MyStatistic(mtc));
				}
			} else {
				if (line.matches("^VARIABLES")) {
					inVars = true;
				}
			}
		}
		br.close();
		System.err.println("Completed reading STS fields:");
		for (int i = 0; i < inToOutField.size()-1; i++) {
			System.err.print(inToOutField.get(i) + "\t");		
		}
		System.err.println(inToOutField.get(inToOutField.size()-1));
	}

	private static void rewriteData(String string) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(new File(string + ".dat.gz")))));
		PrintStream out = new PrintStream(new GZIPOutputStream(new FileOutputStream(new File(string + ".out.txt.gz"))));
//		PrintStream stats = new PrintStream(new FileOutputStream(new File(string + ".stats")));
		String line;
		String[] output = new String[fieldNames.size()];
		for (int i = 0; i < output.length-1; i++) {
			output[i] = "";
			out.print(fieldNames.get(i) + "\t");		
		}
		out.print(fieldNames.get(output.length-1));
		out.print("\n");
		DotCounter dc = new DotCounter();
		while ((line = br.readLine()) != null) {
			dc.inc();
			String[] fields = line.split("\\,");
			// Read through the fields in supplied order, writing them to a buffer array
			// in the output order
			for (int i = 0; i < fields.length; i++) {
				output[inToOutField.get(i)] = fields[i];
			}
			// Write the fields in output order.  Also update the field statistics
			String delim = "";
//			boolean isSampleRecord = true;
			for (int i = 0; i < output.length-1; i++) {
				out.print(delim);
				delim = "\t";
				out.print(output[i]);
				if (output[i].startsWith("\"") && output[i].endsWith("\"")) {
					output[i] = output[i].substring(1,output[i].length()-2);
				}
//				if (isSampleRecord) {
//					fieldStats.get(i).add(output[i]);
//				}
			}
			out.print("\n");
			out.flush();
		}
		br.close();
		dc.finished();
		out.close();
//		for(int i = 0; i < fieldNames.size(); i++) {
//			stats.println(fieldStats.get(i).toStringLong());
//		}
//		stats.close();
	}
}
