package org.dlj.regex;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexYUsageInThinkingJava {

	public static void integerMatch() {
		System.out.println("-1234".matches("-?\\d+"));
		System.out.println("5678".matches("-?\\d+"));
		System.out.println("+911".matches("-?\\d+"));
		System.out.println("+911".matches("(-|\\+)?\\d+"));
	}
	
	public static String knights = "Then, when you have found the shrubbery. you must "
			+ "cut down the mightiest tree in the forest... "
			+ "with... a herring!";
	
	public static void split(String regex) {
		System.out.println(Arrays.toString(knights.split(regex)));
	}
	
	public static void Rudolph() {
		for (String pattern : new String[] {
				"Rudolph", "[rR]udolph", "[rR][aeiou][a-z]ol.*",
				"R.*"
		}) {
			System.out.println("Rudolph".matches(pattern));
		}
	}
	
	public static void TestRegularExpression(String[] args) {
		for (String arg : args) {
			System.out.println("Regular expression: \"" + arg + "\"");
			
			Pattern p = Pattern.compile(arg);
			Matcher m = p.matcher(args[0]);
			while (m.find()) {
				System.out.println("Matche \"" + m.group() + "\" at positions " + m.start() + "-" + (m.end() - 1));
			}
		}
		
	}
	
	public static void Finding() {
		Matcher m = Pattern.compile("\\w+").matcher("Evening is full of the linnet's wings");
		while (m.find()) {
			System.out.println(m.group() + " ");
		}
		System.out.println("--------");
		int i = 0;
		while (m.find(i)) {
			System.out.println(m.group() + " ");
			i++;
		}
	}
	
	private static final String POEM = "Twas brillig, and the slithy tooves\n"
			+ "Did gyre and gimble in the wabe.\n"
			+ "All mimsy were the borogoves.\n"
			+ "And the mome raths outgrabe.\n\n"
			+ "Beware the Jabberwock, my son,\n"
			+ "The jaws that bite. the claws that catch.\n"
			+ "Beware the Jubjub bird, and shun\n"
			+ "The frumious Bandersnatch.";
	
	public static void Groups() {
		Matcher m = Pattern.compile("(?m)(\\S+)\\s+((\\S+)\\s+(\\S+))$").matcher(POEM);
		System.out.println(m.groupCount());
		while (m.find()) {
			for (int j = 0; j <= m.groupCount(); j++) {
				System.out.println("[" + m.group(j) + "]");
			}
			System.out.println();
		}
	}
	
	// matches() lookingAt()
	public static String input = "As long as there is injustice, whenever a\n"
			+ "Targathian body cries out, wherever a distress\n"
			+ "signal sounds among the stars ... We'll be there.\n"
			+ "This fine ship. and this fine crew ...\n"
			+ "Never give up! Never surrender!";
	static class StartEnd{
		private static class Display {
			private boolean regexPrinted = false;
			private String regex;
			Display(String regex) {this.regex = regex;}
			void display(String message) {
				if (!regexPrinted) {
					System.out.println(regex);
					regexPrinted = true;
				}
				System.out.println(message);
				
			}
		}
		
		static void examine(String s, String regex) {
			Display d = new Display(regex);
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(s);
			while (m.find())
				d.display("find() '" + m.group() + "' start = " + m.start() + " end = " + m.end());
			if (m.lookingAt()) // No reset() necessary
				d.display("lookingAt() start = " + m.start() + " end = " + m.end());
			if (m.matches()) // no reset() necessary
				d.display("matches() start = " + m.start() + " end = " + m.end());
		}
	}
	
	private static void ReFlags() {
		Pattern p = Pattern.compile("^java", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
		Matcher m = p.matcher("java has regex\nJava has regex\n"
				+ "JAVA has pretty good regular expressions\n"
				+ "Regular expressions are in Java");
		while (m.find()) {
			System.out.println(m.group());
		}
	}
	
	private static void SplitDemo() {
		String input = "This!!unusual use!!of exclamation!!points";
		System.out.print(Arrays.toString(Pattern.compile("!!").split(input)));
		// Only do the first three
		System.out.print(Arrays.toString(Pattern.compile("!!").split(input, 3)));
		
	}
	
	private static String textFile() {
		ClassLoader loader = RegexYUsageInThinkingJava.class.getClassLoader();
		InputStream in = loader.getResourceAsStream("org/gradle/utils/TheReplacements.txt");
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			byte[] buf = new byte[1024];
			int len = 0;
			while ((len = in.read(buf)) != -1) {
				output.write(buf, 0, len);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return output.toString();
	}
	
//	private static void appendReplaceFirstParam(StringBuffer sb, StringBuffer sbuf) {
//		System.out.println("-->>" + sb);
//		sbuf.append(sb);
//	}
	
	public static void TheReplacements() {
		String s = textFile();
		//System.out.println(s);
		Matcher mInput = Pattern.compile("/\\*!(.*)!\\*/", Pattern.DOTALL).matcher(s);
		if (mInput.find()) {
			s = mInput.group(1);
		}
		
		s = s.replaceAll(" {2,}", " ");
		/*
		 * replace one or more spaces at the beginning of
		 * each line with no spaces. Must enable MULTILINE mode
		 */
		s = s.replaceAll("^ +", "");
		System.out.println(s);
		// 开启多行模式，删除每一行前面的空格
		s = s.replaceAll("(?m)^ +", "");
		System.out.println(s);
		
		s = s.replaceFirst("[aeiou]", "(VOWEL1)");
		StringBuffer sbuf = new StringBuffer();
		String sbuf3 = "";
		StringBuffer sbuf2 = new StringBuffer();
		Pattern p = Pattern.compile("[aeiou]");
		Matcher m = p.matcher(s);
		// Process the find information as you
		// perform the replacements
		int i = 0;
		while(m.find()) {
			// appendReplacement()方法在执行替换的过程中，操作用来替换的字符串
			// sbuf用来保存最终结果
			m.appendReplacement(sbuf, m.group().toUpperCase());
			System.out.println("--->>>" + sbuf);
			sbuf.delete(0, sbuf.length());
//			if (i == 3) {
//				m.appendTail(sbuf);
//				m.appendTail(sbuf2);
//				System.out.println("===\n" + sbuf2);
//				break;
//			}
//			
//			i++;
			
		}
		// put in the remainder of the text:
		//System.out.println("===========\n" + sbuf);
		//m.appendTail(sbuf);
		System.out.println(sbuf);
	}
	
	public static void Resetting() {
		Matcher m = Pattern.compile("[frb][aiu][gx]")
				.matcher("fix the rug with bags");
		while(m.find()) {
			System.out.print(m.group(0) + " ");
		}
		System.out.println();
		System.out.println("not reset");
		while(m.find()) {
			System.out.print(m.group(0) + " ");
		}
		System.out.println("reset");
		// 不带参数的reset()方法，可以将Matcher对象重新设置到当前字符序列的起始位置
		m.reset();
		while(m.find()) {
			System.out.print(m.group(0) + " ");
		}
		System.out.println();
		m.reset("fix the rig with rags");
		while(m.find())
			System.out.print(m.group() + " ");
	}
	
	public static void JGrep(String[] args) {
		if (args.length < 2) {
			System.out.println("Usage: java JGrep file regex");
			System.exit(0);
		}
		Pattern p = Pattern.compile(args[1]);
		int index = 0;
		Matcher m = p.matcher("");
		// 这个可以替换为从一个文件中读取每一行存入到一个list中
		List<String> list = new ArrayList<String>();
		for (String line : list) {
			m.reset(line);
			while(m.find())
				System.out.println(index++ + ": " + m.group() + ": " + m.start());
		}
	}
	
	public static void main(String[] args) {
//		integerMatch();
//		
//		split(" ");
//		split("\\W+");
//		split("n\\W+");
//		
//		System.out.println(knights.replaceFirst("f\\w+", "located"));
//		System.out.println(knights.replaceAll("shrubbery|tree|herring", "banana"));
//
//		Rudolph();
		
//		String[] regular = {"abcabcabcdefabc", "abc+", "(abc)+", "(abc)+?", "(abc){2,}", "(abc){2,}?"};
//		TestRegularExpression(regular);
		
//		Finding();
		
//		Groups();
		
//		for(String in : input.split("\n")) {
//			System.out.println("input : " + in);
//			for (String regex : new String[] {
//					"\\w*ere\\w*", "\\w*ever", "T\\w+", "Never.*?!"
//			}) {
//				StartEnd.examine(in, regex);
//			}
//		}
		
//		ReFlags();
//		
//		SplitDemo();
		
		TheReplacements();
		
//		Resetting();
	}
}
