package org.dlj.io.thinkingInJava;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public class FileDemo {

	public static void main(String[] args) {
//		DirList.main();
		
//		Directory.main();
		
		ProcessFiles.main();
	}
}

/**
 * 目录列表器
 * file对象包含的全部列表
 * @author zhxg
 *
 */
class DirList {
	public static FilenameFilter filter(String regex) {
		// Creation of anonymous inner class
		return new FilenameFilter() {
			private Pattern pattern = Pattern.compile(regex);
			public boolean accept(File dir, String name) {
				return pattern.matcher(name).matches();
			}
		};
	}
	public static void main() {
		String args[] = new String[] {};
		File path = new File(".");
		String[] list;
		if (args.length == 0) {
			list = path.list();
		} else {
			// 方法一：使用实现FilenameFilter内部类
			//list = path.list(new DirList().new DirFilter(args[0]));
			// 方法二：使用匿名内部类
			//list = path.list(filter(args[0]));
			// 方法三：直接使用匿名内部类
			list = path.list(new FilenameFilter() {
				private Pattern pattern = Pattern.compile(args[0]);
				public boolean accept(File dir, String name) {
					return pattern.matcher(name).matches();
				}
			});
		}
		Arrays.sort(list, String.CASE_INSENSITIVE_ORDER);
		for (String dirItem : list) {
			System.out.println(dirItem);
		}
	}
	
	class DirFilter implements FilenameFilter {
		private Pattern pattern;
		public DirFilter(String regex) {
			pattern = Pattern.compile(regex);
		}
		
		public boolean accept(File dir, String name) {
			return pattern.matcher(name).matches();
		}
	}
}

/**
 * 目录使用工具
 * 筛选指定文件夹下面的文件集
 * @author zhxg
 *
 */
final class Directory {
	public static File[] local(File dir, final String regex) {
		return dir.listFiles(new FilenameFilter() {
			private Pattern pattern = Pattern.compile(regex);
			public boolean accept(File dir, String name) {
				return pattern.matcher(new File(name).getName()).matches();
			}
		});
	}
	
	public static File[] local(String path, final String regex) {
		return local(new File(path), regex);
	}
	
	// A two-tuple for returning a pair of Objects
	public static class TreeInfo implements Iterable<File> {
		public List<File> files = new ArrayList<File>();
		public List<File> dirs = new ArrayList<File>();
		
		public Iterator<File> iterator() {
			return files.iterator();
		}
		
		void addAll(TreeInfo other) {
			files.addAll(other.files);
			dirs.addAll(other.dirs);
		}
		
		public String toString() {
			//return "dirs: " + dirs + "\n\nfiles: " + files;
			return "dirs: " + PPrint.pformat(dirs) + "\n\nfiles: " + PPrint.pformat(files);
		}
	}
	public static TreeInfo walk(String start, String regex) {
		return recurseDirs(new File(start), regex);
	}
	public static TreeInfo walk(File start, String regex) {
		return recurseDirs(start, regex);
	}
	public static TreeInfo walk(File start) {
		return recurseDirs(start, ".*");
	}
	public static TreeInfo walk(String start) {
		return recurseDirs(new File(start), ".*");
	}
	static TreeInfo recurseDirs(File startDir, String regex) {
		TreeInfo result = new TreeInfo();
		for (File item : startDir.listFiles()) {
			if (item.isDirectory()) {
				result.dirs.add(item);
				result.addAll(recurseDirs(item, regex));
			} else if(item.getName().matches(regex)) {
				result.files.add(item);
			}
		}
		return result;
	}
	
	public static void main() {
		String[] args = new String[] {};
		System.out.println(args.length);
		if (args.length == 0) {
			System.out.println(walk("E:\\BaiduNetdiskDownload\\2018图书"));
		} else {
			for (String arg : args) {
				System.out.println(walk(arg));
			}
		}
	}
}
class PPrint {
	public static String pformat(Collection<?> c) {
		if (c.size() == 0)
			return "[]";
		StringBuilder result = new StringBuilder("[");
		for (Object elem : c) {
			if (c.size() != 1)
				result.append("\n ");
			result.append(elem);
		}
		if (c.size() != 1)
			result.append("\n");
		result.append("]");
		return result.toString();
	}
	public static void pprint(Collection<?> c) {
		System.out.println(pformat(c));
	}
	public static void pprint(Object[] c) {
		System.out.println(pformat(Arrays.asList(c)));
	}
}

/**
 * 使用策略模式，在指定目录下根据指定的正则筛选出匹配的所有文件，并对筛选出来的
 * 文件根据不同的策略进行处理
 * @author zhxg
 *
 */
class ProcessFiles {
	public interface Strategy {
		void process(File file);
	}
	private Strategy strategy;
	private String ext;
	public ProcessFiles(Strategy strategy, String ext) {
		this.strategy = strategy;
		this.ext = ext;
	}
	public void start(String[] args) {
		try {
			if (args.length == 0) {
				processDirectoryTree(new File("."));
			} else {
				for (String arg : args) {
					File fileArg = new File(arg);
					if (fileArg.isDirectory()) {
						processDirectoryTree(fileArg);
					} else {
						// allow user to leave off extension:
						if (!arg.endsWith("." + ext)) {
							arg += "." + ext;
						}
						strategy.process(new File(arg).getCanonicalFile());
					}
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	public void processDirectoryTree(File root) throws IOException {
		for(File file : Directory.walk(root.getAbsolutePath(), ".*\\." + ext))
			strategy.process(file.getCanonicalFile());
	}
	
	public static void main() {
		String[] args = new String[] {};
		// 1. Strategy接口定义在ProcessFiles类内部，新建该接口对象方法
		new ProcessFiles(new ProcessFiles.Strategy() {
			public void process(File file) {
				System.out.println(file);
			}
		}, "java").start(args);
		
		// 2.Strategy接口定义在ProcessFiles类外部，新建该接口对象方法
//		new ProcessFiles(new Strategy() {
//			public void process(File file) {
//				System.out.println(file);
//			}
//		}, "java").start(args);
	}
}

/**
 * File类不仅仅代表存在的文件或目录，也可以用file对象来创建
 * 新的目录和尚不存在的整个目录，可以查看文件的特性（如：大小，对吼修改日期，读写），
 * 检查某个File对象代表的是一个文件还是一个目录，可以删除文件
 * @author zhxg
 *
 */
class MakeDirectories {
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
