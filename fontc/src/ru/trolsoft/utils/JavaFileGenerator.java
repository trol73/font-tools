package ru.trolsoft.utils;

import java.util.ArrayList;
import java.util.List;

public class JavaFileGenerator {
	public static final int DEFAULT_INTS_PER_LINE = 20;
	public static final int DEFAULT_STRINGS_PER_LINE = 1;
	
	private int intsPerLine = DEFAULT_INTS_PER_LINE;
	private int stringsPerLine = DEFAULT_STRINGS_PER_LINE;
	
	private String indent = "";	// line prefix, tab symbols
	
	/**
	 * Lines of a generated java-file
	 */
	private List<String> javaLines = new ArrayList<String>();
	
	private String fileName;



	/**
	 * 
	 */
	public JavaFileGenerator() {
		
	}
	
	/**
	 * 
	 * @param fileName
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * 
	 * @param s
	 */
	public void add(String s) {
		javaLines.add(indent+s);
	}
	
	/**
	 * 
	 */
	public void incIndent() {
		indent += '\t';
	}
	
	/**
	 * 
	 */
	public void decIndent() {
		indent = indent.substring(1);
	}	
	
	/**
	 * 
	 */
	public void addLine() {
		add(indent + "//-----------------------------------------------------------");
	}
	
	
	/**
	 * 
	 * @param name
	 * @param value
	 * @param comment
	 */
	public void addConst(String name, String value, String comment) {
		String s = indent + "public static final String " + name + " = " + value + ";";
		if ( comment != null ) {
			s += "\t\t\t//" + comment;
		}
		add(s);
	}

	
	/**
	 * 
	 * @param name
	 * @param value
	 * @param comment
	 */
	public void addConst(String name, int value, String comment) {
		String s = indent + "public static final int " + name + " = " + value + ";";
		if ( comment != null ) {
			s += "\t\t\t//" + comment;
		}
		add(s);
	}
	
	
	/**
	 * 
	 * @param name
	 * @param value
	 * @param comment
	 */
	public void addConst(String name, double value, String comment) {
		String s = indent + "public static final float " + name + " = " + value + ";";
		if ( comment != null ) {
			s += "\t\t\t//" + comment;
		}
		add(s);
	}
	
	
	
	/**
	 * 
	 * @param type
	 * @param name
	 * @param value
	 * @param comment
	 */
	public void addTypedConst(String type, String name, String[] value, String comment, int itemsPerLine) {
		String s = indent + "public static final " + type + "[] " + name + " = {";
		if ( comment != null ) {
			s += "\t\t\t//" + comment;
		}
		add(s);
		incIndent();
		int cnt = 0;
		s = "";
		for ( String val : value ) {
			s += val;
			cnt++;
			if ( cnt < value.length ) {
				s += ", ";
			}
			if ( cnt % itemsPerLine == 0 && cnt != 0 ) {
				add(s);
				s = "";
			}
		}
		if ( s.length() > 0 ) {
			add(s);
		}
		decIndent();
		add("};");
	}

	
	/**
	 * 
	 * @param name
	 * @param value
	 * @param comment
	 */
	public void addConst(String name, int[] value, String comment) {
		String strValue[] = new String[value.length];
		for ( int i = 0; i < value.length; i++ ) {
			strValue[i] = Integer.toString(value[i]);
		}
		addTypedConst("int", name, strValue, comment, intsPerLine);
	}
	
	
	/**
	 * 
	 * @param name
	 * @param value
	 * @param comment
	 */
	public void addConst(String name, String[] value, String comment) {
		String strValue[] = new String[value.length];
		for ( int i = 0; i < value.length; i++ ) {
			strValue[i] = "\"" + value[i] + "\"";
		}
		addTypedConst("String", name, strValue, comment, stringsPerLine);
	}
	
	
	/**
	 * 
	 * @param type
	 * @param name
	 * @param value
	 * @param comment
	 */
	public void addTypedConst(String type, String name, String[][] value, String comment) {
		String s = indent + "public static final " + type + "[][] " + name + " = {";
		if ( comment != null ) {
			s += "\t\t\t//" + comment;
		}
		add(s);
		incIndent();		
		for ( int i = 0; i < value.length; i++ ) {
			s = "{";
			for ( int j = 0; j < value[i].length; j++ ) {
				s += value[i][j];
				if ( j < value[i].length-1 ) {
					s += ", ";
				}
			}
			s += "}";
			if ( i < value.length-1 ) {
				s += ",";
			}
			add(s);
		}
		decIndent();
		add("};");
	}
	
	
	/**
	 * 
	 * @param name
	 * @param value
	 * @param comment
	 */
	public void addConst(String name, int[][] value, String comment) {
		String strValue[][] = new String[value.length][];
		for ( int i = 0; i < value.length; i++ ) {
			strValue[i] = new String[value[i].length];
			for ( int j = 0; j < value[i].length; j++ ) {
				strValue[i][j] = Integer.toString(value[i][j]);
			}
		}
		addTypedConst("int", name, strValue, comment);
	}
	
	
	/**
	 * 
	 * @param name
	 * @param type
	 * @param value
	 */
	public void addConst(String name, String value) {
		addConst(name, value, null);
	}
	
	
	/**
	 * 
	 * @param name
	 * @param value
	 */
	public void addConst(String name, int value) {
		addConst(name, value, null);
	}
	
	/**
	 * 
	 * @param name
	 * @param value
	 */
	public void addConst(String name, double value) {
		addConst(name, value, null);
	}
	
	
	/**
	 * 
	 * @param type
	 * @param name
	 * @param value
	 * @param itemsPerLine
	 */
	public void addTypedConst(String type, String name, String[] value, int itemsPerLine) {
		addTypedConst(type, name, value, null, itemsPerLine);
	}

	/**
	 * 
	 * @param name
	 * @param value
	 */
	public void addConst(String name, int[] value) {
		addConst(name, value, null);
	}
	
	
	/**
	 * 
	 * @param name
	 * @param value
	 */
	public void addConst(String name, String[] value) {
		addConst(name, value, null);
	}
	
	/**
	 * 
	 * @param name
	 * @param value
	 */
	public void addConst(String name, int[][] value) {
		addConst(name, value, null);
	}
	
	/**
	 * 
	 * @param type
	 * @param name
	 * @param value
	 */
	public void addTypedConst(String type, String name, String[][] value) {
		addTypedConst(type, name, value, null);
	}

	/**
	 * 
	 * @param intsPerLine
	 */
	public void setIntsPerLine(int intsPerLine) {
		this.intsPerLine = intsPerLine;
	}
	
	/**
	 * 
	 * @param stringsPerLine
	 */
	public void setStringsPerLine(int stringsPerLine) {
		this.stringsPerLine = stringsPerLine;
	}
	
	public void println() {
		for ( int i = 0; i < javaLines.size(); i++ ) {
			System.out.println(javaLines.get(i));
		}
	}


}
