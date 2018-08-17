package org.dlj.io;

import java.io.File;
import java.io.IOException;

/**
 * 使用策略模式，在指定目录下根据指定的正则筛选出匹配的所有文件，并对筛选出来的
 * 文件根据不同的策略进行处理
 * @author zhxg
 *
 */
public class ProcessFiles {

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
	
	// Demonstration of how to use it
	public static void main(String[] args) {
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

interface Strategy {
	void process(File file);
}