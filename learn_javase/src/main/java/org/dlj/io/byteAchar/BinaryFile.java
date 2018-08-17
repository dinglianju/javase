package org.dlj.io.byteAchar;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class BinaryFile {

	public static byte[] read(File bFile) throws IOException {
		BufferedInputStream bf = new BufferedInputStream(new FileInputStream(bFile));
		try {
			byte[] data = new byte[bf.available()];
			bf.read(data);
			return data;
		} finally {
			bf.close();
		}
	}
	
	public static byte[] read(String bFile) throws IOException {
		return read(new File(bFile).getAbsoluteFile());
	}
	
	public static void main(String[] args) throws IOException {
		byte[] b = read("README.md");
		List<byte[]> blist = Arrays.asList(b);
		System.out.println(b.length + "; " + blist.get(0).length);
	}
}
