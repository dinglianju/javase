package org.dlj.io;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * File类不仅仅代表存在的文件或目录，也可以用file对象来创建
 * 新的目录和尚不存在的整个目录，可以查看文件的特性（如：大小，对吼修改日期，读写），
 * 检查某个File对象代表的是一个文件还是一个目录，可以删除文件
 * @author zhxg
 *
 */
public class MakeDirectories {

	private static void usage() {
		System.err.println(
				"Usage: MakeDirectories path1 ...\n"
				+ "Creates each path\n"
				+ "Usage:MakeDirectories -d path1 ...\n"
				+ "Deletes each path\n"
				+ "Usage:MakeDirectories -r path1 path2\n"
				+ "Renames from path1 to path2"
				);
		System.exit(1);
	}
	
	private static void fileData(File f) {
		System.out.println("Absolute path: " + f.getAbsolutePath() + 
				"\n f: " + f +
				"\n absolute file: " + f.getAbsoluteFile() +
				"\n Can read: " + f.canRead() + 
				"\n Can write: " + f.canWrite() + 
				"\n getName: " + f.getName() +
				"\n getParent: " + f.getParent() +
				"\n getPath: " + f.getPath() +
				"\n length: " + f.length() +
				"\n lastModified: " + f.lastModified());
		if (f.isFile()) 
			System.out.println("It's a file");
		else if (f.isDirectory())
			System.out.println("It's a directory");
	}
	
	public static void main(String[] args) throws IOException {
		System.out.println(Arrays.asList(args));
		// 创建file1,f2两个平行目录
		String[] arg = {"file1", "f2"};
		// 创建整个目录file3/file4嵌套目录
		String[] arg2 = {"file3/file4"};
		// 重命名file1为file2
		String[] arg_r = {"-r", "file1" , "file2"};
		// 删除f2文件
		String[] arg_d = {"-d", "f2"};
		args = arg2;
		if(args.length < 1) usage();
		if(args[0].equals("-r")) {
			if (args.length != 3) usage();
			File
				old = new File(args[1]),
				rname = new File(args[2]);
			// renameTo() 用来把一个文件重命名（或移动）到由参数所指示的另一个完全不同的新路径（也就是另一个File对象）下面，
			// 这同样适用于任意长度的文件目录
			old.renameTo(rname);
			fileData(old);
			fileData(rname);
			return;
		}
		
		int count = 0;
		boolean del = false;
		if (args[0].equals("-d")) {
			count++;
			del = true;
		}
		count--;
		while(++count < args.length) {
			// System.out.println("count: " + count);
			File f = new File(args[count]);
			if (f.exists()) {
				System.out.println(f + " exists");
				if (del) {
					System.out.println("deleting..." + f);
					f.delete();
				}
			} else {
				if (!del) {
					if (count == 1) {
						// 创建文件
						boolean b = f.createNewFile();
						File tf1 = f.createTempFile("prefix", "suffix");
						File tf2 = f.createTempFile("pre", "suf", new File(args[count - 1]));
						System.out.println("count: " + count + "; b:" + b + "; tf1: " + tf1.getAbsolutePath() + "; tf2: " + tf2.getAbsolutePath());
					} else {
						f.mkdirs();
						System.out.println("created " + f);
					}
					
				}
			}
			fileData(f);
		}
	}
}
