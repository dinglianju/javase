package org.dlj.io;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.regex.Pattern;

public class DirList {

	public static FilenameFilter filter(String regex) {
		// Creation of anonymous inner class
		return new FilenameFilter() {
			private Pattern pattern = Pattern.compile(regex);
			public boolean accept(File dir, String name) {
				return pattern.matcher(name).matches();
			}
		};
	}
	
	public static void main(String[] args) {
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
