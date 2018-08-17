package org.dlj;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class App {
	private static final Logger log = LoggerFactory.getLogger(App.class);
	
	public static void main() {
		System.out.println(new Long(100) == 100);
		System.out.println(100 == new Long(100));
		System.out.println(new Long(100) == new Long(100));
		//System.out.println(100 == null);
		System.out.println(new Long(100).equals(null));
		
		System.out.println("testsrc".indexOf("test"));
		String dir = "\\design\\星光数据平台\\v3.12需求文档\\resources";
		//System.out.println(Arrays.asList(dir.split("\\")));
		System.out.println(dir.indexOf(File.separator));
		//System.out.println(" ---: " + Integer.valueOf(""));
		System.out.println(new Long(19) - new Long(10));
		System.out.println(new Long(19) - 10);
		System.out.println(19 - new Long(10));
		//System.out.println(new Long(19) - null);
		System.out.println("--\\mfs\\user_space\\636988446722048\\report\\");
		System.out.println("\\mfs\\user_space\\636988446722048\\report\\".replace("\\", "/"));
		
		String grex = "http://\\.{6,6}";
		String grex2 = "http://......";
		String emailUrlTemplet = "链接如下：http://......";
		String str = "<p><img src=\"http://localhost:8080/gaea-web/report/ueditorDownloadFile.html?path=/404519785908224/report/upload/image/20180719/1531966561531026146.jpg\" title=\"\" alt=\"Tulips.jpg\"/></p>链接如下：http://\\.\\.\\.\\.\\.\\.";
		String str2 = "<p><img src=\"http://localhost:8080/gaea-web/report/ueditorDownloadFile.html?path=/404519785908224/report/upload/image/20180719/1531966561531026146.jpg\" title=\"\" alt=\"Tulips.jpg\"/></p>链接如下：http://......";
		System.out.println("----------");
		System.out.println(str.indexOf(grex));
		System.out.println(str2.indexOf(grex));
		System.out.println(str2.contains(grex));
		System.out.println("grex2: " + str2.contains(grex2));
		System.out.println(str2.replace(grex2, "===="));
		System.out.println(str2.replace(emailUrlTemplet, ""));
		
		String str3 = "<p style=\"text-align: center;\"><span style=\"color: rgb(192, 0, 0);\"><strong><em>发毒誓</em></strong><strong><em>上</em></strong><strong><em>的</em></strong></span></p><p style=\"text-align: center;\"><strong><em>防守打法饭地方</em></strong><strong><em><img src=\"链接如下：http://localhost:8080/gaea-web/report/ueditorDownloadFile.html?path=/404519785908224/report/upload/image/20180719/1531967946666019683.jpg\" title=\"\" alt=\"Desert.jpg\"/></em></strong></p>链接如下：http://......";
		System.out.println(str3.replace(emailUrlTemplet, "----"));
		Pattern p = Pattern.compile(grex);
		Matcher m = p.matcher(str3);
		while (m.find()) {
			System.out.println("Matche \"" + m.group() + "\" at positions " + m.start() + "-" + (m.end() - 1));
			System.out.println(str3.replace(grex2, "==="));
			break;
		}
		
		
		/*
		 * 注意：计算中间位置时不应该使用(high+low)/2的方式，
		 * 因为加法运算可能导致整数越界。
		 * 应该使用如下三种方式之一：
		 * 1. low + (high - low)/2
		 * 2. low + (high - low) >> 1
		 * 3. (low + high) >>> 1 （>>>是逻辑右移，不带符号位的右移）
		 */
		int low = 2147483646, high = 2147483647, t = 2;
		int h1 = (low + high) / 2;
		int h2 = low + (high - low) / 2;
		int h3 = low + ((high - low) >> 1);
		int h4 = (low + high) >>> 1;
		int t1 = t >> 1;
		int t2 = t >>> 1;
		int t3 = 7 / 8;
		log.debug("h1: {}, h2: {}, h3: {}, h4: {}, t1: {}, t2: {}", h1, h2, h3, h4, t1, t2);
		log.debug("t: {}, t.binary: {}, t1: {}, t1.binary: {}, t2: {}, t2.binary: {}", t, Integer.toBinaryString(t), t1, Integer.toBinaryString(t1), t2, Integer.toBinaryString(t2));
		log.debug("integer maxValue: {}, minValue: {}, t3: {}", Integer.MAX_VALUE, Integer.MIN_VALUE, t3);
		
//		boolean flag = false;
//	    for ( int i = 0; i < 2147483647L + 1; i ++ ) {  
//	        if( Integer.MAX_VALUE == i ) {  
//	            flag = true;  
//	        }  
//	         
//	        if( flag ) {  
//	            System.out.println( i );  
//	        }  
//	    }
		
		log.debug("Long 4 > 0 : {}, 0 < Long 4: {}", new Long(4) > 0, 0 < new Long(4));
		
		
		log.debug("1\\1" + "test" + "\\1" + "test2");
		
		
		log.debug("test: {}", "\1");
		int i = "[[test]]\1[[test2]]".indexOf("\1");
		String[] strArr = "[[test]]\1[[test2]]".split("\1");
		log.debug("\1 indexOf: {}, split: {}", i, Arrays.asList(strArr));
	}
	
	public static void main(String[] args) {
		String col = "remotedb0.info.web_url";
		int pos = col.lastIndexOf(".");
		log.debug("col dot pos: {}, col: {}", pos, col.substring(pos + 1));
		
		log.debug("blockspace pos: {}", " select	db.info.col".lastIndexOf(" "));
		
		String str = (String) null;
		ArrayList<String> array = (ArrayList<String>)null;
		Map<String, Object> map = new HashMap<String, Object>();
//		int i = (int)map.get("open");
//		log.debug("str: {}, array: {}, map.get open: {}, i: {}", str ,array, map.get("open"), i);
//		
		
		log.debug("-100: {}", Long.parseLong("-100"));
		
		Map<String, Object> map2 = new LinkedHashMap<String, Object>();
		map2.put(null, "key is null");
		map2.put("value is null", null);
		log.debug("map2 to json: {}", JSONObject.toJSONString(map2, SerializerFeature.WriteNullStringAsEmpty));
	}
}
