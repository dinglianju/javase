package org.dlj.io;

import java.io.File;

public class DirectoryDemo {

	public static void main(String[] args) {
		// All directories:
		PPrint.pprint(Directory.walk(".").files);
		// all files beginning with 'T'
		for(File file : Directory.local(".", ".*")) {
			System.out.println(file);
		}
		System.out.println("---------------");
		// all java files beginning with 'T'
		for(File file : Directory.walk(".", "S.*\\.java")) {
			System.out.println(file);
		}
		System.out.println("===============");
		// class files containing "Z" or "z"
		for(File file : Directory.walk(".", ".*[Zz].*\\.class")) {
			System.out.println(file);
		}
	}
}
