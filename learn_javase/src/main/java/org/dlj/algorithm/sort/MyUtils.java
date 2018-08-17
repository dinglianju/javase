package org.dlj.algorithm.sort;

import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyUtils {
	private static final Logger log = LoggerFactory.getLogger(MyUtils.class);
	
	public static <T extends Comparable<T>> int binarySearch(T[] x, T key) {
		return binarySearch(x, 0, x.length - 1, key);
	}
	
	/**
	 * 使用循环实现的二分查找
	 * @param x
	 * @param key
	 * @param comp
	 * @return
	 */
	public static <T> int binarySearch(T[] x, T key, Comparator<T> comp) {
		int low = 0;
		int high = x.length - 1;
		while (low <= high) {
			/*
			 * 注意：计算中间位置时不应该使用(high+low)/2的方式，
			 * 因为加法运算可能导致整数越界。
			 * 应该使用如下三种方式之一：
			 * 1. low + (high - low)/2
			 * 2. low + (high - low) >> 1
			 * 3. (low + high) >>> 1 （>>>是逻辑右移，不带符号位的右移）
			 */
			int mid = (low + high) >>> 1;
			int cmp = comp.compare(x[mid], key);
			if (cmp < 0) {
				low = mid + 1;
			} else if (cmp > 0) {
				high = mid - 1;
			} else {
				return mid;
			}
		}
		return -1;
	}
	
	private static <T extends Comparable<T>> int binarySearch(T[] x, int low, int high, T key) {
		if (low <= high) {
			int mid = low + ((high - low) >> 1);
			if (key.compareTo(x[mid]) == 0) {
				return mid;
			} else if (key.compareTo(x[mid]) < 0) {
				return binarySearch(x, low, mid - 1, key);
			} else {
				return binarySearch(x, mid + 1, high, key);
			}
		}
		return -1;
	}
	
	public static void main(String[] args) {
		/*
		 * 注意：计算中间位置时不应该使用(high+low)/2的方式，
		 * 因为加法运算可能导致整数越界。
		 * 应该使用如下三种方式之一：
		 * 1. low + (high - low)/2
		 * 2. low + (high - low) >> 1
		 * 3. (low + high) >>> 1 （>>>是逻辑右移，不带符号位的右移）
		 */
		int low = 2147483646, high = 2147483647, t = 3, tt = -3;
		int h1 = (low + high) / 2;
		int h2 = low + (high - low) / 2;
		int h3 = low + ((high - low) >> 1);
		int h4 = (low + high) >>> 1;
		int t1 = t >> 1;
		int t2 = t >>> 1;
		int t3 = tt >> 1;
		int tt1 = tt >>> 1;
		int tt2 = tt >>> 2;
		
		int t4 = 7 / 8;
		log.debug("h1: {}, h2: {}, h3: {}, h4: {}, t1: {}, t2: {}", h1, h2, h3, h4, t1, t2);
		log.debug("t: {}, t.binary: {}, tt: {}, tt.binary: {}", t, Integer.toBinaryString(t), tt, Integer.toBinaryString(tt));
		log.debug("t1: {}, t1.binary: {}, t2: {}, t2.binary: {}, t3: {}, t3.binary: {}, tt1: {}, tt1.binary: {}, tt2: {}, tt2.binary: {}", t1, Integer.toBinaryString(t1), t2, Integer.toBinaryString(t2), t3, Integer.toBinaryString(t3), tt1, Integer.toBinaryString(tt1), tt2, Integer.toBinaryString(tt2));
		log.debug("integer maxValue: {}, minValue: {}, t4: {}", Integer.MAX_VALUE, Integer.MIN_VALUE, t4);
		
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
	}
}
