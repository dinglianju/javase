package org.dlj.io.byteAchar;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;

public class Echo {

	public static void main() throws IOException {
		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
		String s;
		while ((s = stdin.readLine()) != null && s.length() != 0) {
			System.out.println(s);
		}
	}
	
	public static void main(String[] args) throws IOException {
		//main();
		//ChangeSystemOut.main();
		Redirecting.main();
	}
}

/**
 * System.out是一个PrintStream，PrintStream是一个OutputStream
 * 可以使用PrintWriter的构造函数，把system.out转换成PrintWriter
 * @author zhxg
 *
 */
class ChangeSystemOut {
	public static void main() {
		// printWirte构造函数的第二个参数为true，开启自动清空功能
		PrintWriter out = new PrintWriter(System.out, true);
		out.println("Hello, world");
	}
}

class Redirecting {
	public static void main() throws IOException {
		PrintStream console = System.out;
		BufferedInputStream in = new BufferedInputStream(new FileInputStream("README.md"));
		PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream("test.out")));
		System.setIn(in);
		System.setOut(out);
		System.setErr(out);
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String s;
		while ((s = br.readLine()) != null) {
			System.out.println(s);
		}
		out.close();
		System.setOut(console);
	}
}