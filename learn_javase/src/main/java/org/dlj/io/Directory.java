package org.dlj.io;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public final class Directory {
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
	
	public static void main(String[] args) {
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

