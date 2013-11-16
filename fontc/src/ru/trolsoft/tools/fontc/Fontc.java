package ru.trolsoft.tools.fontc;

import java.io.IOException;

public class Fontc {

	public static void usage() {
		System.out.println("Usage: fontc: <font.xml>");
	}
	
	public static void main(String args[]) throws IOException {
		if ( args.length < 1 ) {
			usage();
			return;
		}
		Font fnt = new Font(args[0]);
		fnt.compile();
		fnt.save();
	}
}
