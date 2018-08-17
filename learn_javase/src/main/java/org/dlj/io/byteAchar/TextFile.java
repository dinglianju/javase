package org.dlj.io.byteAchar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;

public class TextFile extends ArrayList<String> {

	// Read a file, split by any regular expression:
	public TextFile(String fileName, String splitter) {
		super(Arrays.asList(read(fileName).split(splitter)));
		// regular expression split() often leaves an empty
		// String at the first position
		if (get(0).equals(""))
			remove(0);
	}

	// normally read by lines
	public TextFile(String fileName) {
		this(fileName, "\n");
	}

	// read a file as a single string
	public static String read(String fileName) {
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new FileReader(new File(fileName).getAbsoluteFile()));
			try {
				String s;
				while ((s = in.readLine()) != null) {
					sb.append(s);
					sb.append("\n");
				}
			} finally {
				in.close();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return sb.toString();
	}

	// write a single file in one method call;
	public static void write(String fileName, String text) {
		PrintWriter out;
		try {
			out = new PrintWriter(new File(fileName).getAbsoluteFile());
			try {
				out.print(text);
			} finally {
				out.close();
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}

	}

	public void write(String fileName) {
		PrintWriter out;
		try {
			out = new PrintWriter(new File(fileName).getAbsoluteFile());
			try {
				for (String item : this) {
					out.println(item);
				}
			} finally {
				out.close();
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}

	}

	public static void main(String[] args) {
		// String file = read("TextFile.java");
		String file = read("README1.md");

		write("test.txt", file);
		TextFile text = new TextFile("test.txt");
		text.write("test2.txt");
		// Break into unique sorted list of words
		TreeSet<String> words = new TreeSet<String>(new TextFile("README.md", "\\W+"));
		// display the capitalized words
		System.out.println(words.headSet("a"));
	}

}
