package org.dlj.io.byteAchar;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringReader;

public class BufferedInputFile {

	public static String read(String filename) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(filename));
		String s;
		StringBuilder sb = new StringBuilder();
		while ((s = in.readLine()) != null) {
			sb.append(s + "\n");
		}
		in.close();
		return sb.toString();
	}

	public static void main(String[] args) throws IOException {
		// System.out.print(read("README.md"));

//		MemoryInput.main();
//
//		FormattedMemoryInput.main();
//
//		TestEOF.main();

		//BasicFileOutput.main();
		
		//FileOutputShortcut.main();
		
		//StoringAndRecoveringData.main();
		
		UsingRandomAccessFile.main();
	}
}

class MemoryInput {
	public static void main() throws IOException {
		StringReader in = new StringReader(BufferedInputFile.read("README.md"));
		int c;
		while ((c = in.read()) != -1) {
			System.out.print((char) c);
		}
	}
}

class FormattedMemoryInput {
	public static void main() throws IOException {
		try {
			DataInputStream in = new DataInputStream(
					new ByteArrayInputStream(BufferedInputFile.read("README.md").getBytes()));
			while (true)
				System.out.print((char) in.readByte());
		} catch (EOFException e) {
			System.err.println("End of stream");
		}
	}
}

class TestEOF {
	public static void main() throws IOException {
		DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream("README.md")));
		while (in.available() != 0) {
			System.out.print((char) in.readByte());
		}
	}
}

class BasicFileOutput {
	static String file = "BasicFileOutput.out";

	public static void main() throws IOException {
		BufferedReader in = new BufferedReader(new StringReader(BufferedInputFile.read("README.md")));
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
		int lineCount = 1;
		String s;
		while ((s = in.readLine()) != null)
			out.println(lineCount++ + ": " + s);
		// 如果不为所有的输出文件调用close()，缓冲区内容就不会被刷新清空，那么它们就不完整
		//out.close();
		out.close();
		// show the stored file:
		System.out.println(BufferedInputFile.read(file));
	}
}

/**
 * printWriter一个辅助构造函数，不必每次希望创建文本文件并向其中写入时
 * 都去执行所有的装饰工作，它仍旧进行缓存，只是不必自己去实现
 * @author zhxg
 *
 */
class FileOutputShortcut {
	static String file = "FileOutputShortcut.out";
	public static void main() throws IOException {
		BufferedReader in = new BufferedReader(new StringReader(BufferedInputFile.read("README.md")));
		// here's the shortcut:
		PrintWriter out = new PrintWriter(file);
		int lineCount = 1;
		String s;
		while ((s = in.readLine()) != null) {
			out.println(lineCount++ + ": " + s);
		}
		out.close();
		// show the stored file:
		System.out.println(BufferedInputFile.read(file));
	}
}

class StoringAndRecoveringData {
	public static void main() throws IOException {
		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream("Data.txt")));
		out.writeDouble(3.14159);
		out.writeUTF("That was pi");
		out.writeDouble(1.41413);
		out.writeUTF("Square(平方) root of 2");
		out.close();
		
		DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream("Data.txt")));
		System.out.println(in.readDouble());
		// Only readUTF() will recover the
		// Java-UTF String properly
		System.out.println(in.readUTF());
		System.out.println(in.readDouble());
//		System.out.println(in.readUTF());
//		System.out.println(in.readDouble());
		
	}
}

class UsingRandomAccessFile {
	static String file = "rtest.dat";
	static void display() throws IOException {
		RandomAccessFile rf = new RandomAccessFile(file, "r");
		for (int i = 0; i < 7; i++) {
			System.out.println("Value " + i + ": " + rf.readDouble());
		}
		System.out.println(rf.readUTF());
		rf.close();
	}
	
	public static void main() throws IOException {
		RandomAccessFile rf = new RandomAccessFile(file, "rw");
		for (int i = 0; i < 7; i++) {
			rf.writeDouble(i * 1.414);
		}
		rf.writeUTF("The end of the file");
		rf.close();
		display();
		rf = new RandomAccessFile(file, "rw");
		rf.seek(5*8);
		rf.writeDouble(47.0001);
		rf.close();
		display();
	}
}