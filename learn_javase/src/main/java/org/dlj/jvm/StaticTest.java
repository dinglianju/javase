package org.dlj.jvm;

/*
 * java 类加载
 * 参考：https://mp.weixin.qq.com/s/VBbNm2N20DotmGYlSEBS8g
 */
public class StaticTest {

	public static void main(String[] args) {
		staticFunction();
	}
	
	static StaticTest st = new StaticTest();
	
	static {
		System.out.println("1");
	}
	
	{
		System.out.println("2");
	}
	
	StaticTest() {
		System.out.println("3");
		System.out.println("a=" + a + " ,b=" + b);
	}
	
	public static void staticFunction() {
		System.out.println("4");
	}
	
	int a = 110;
	static int b = 112;
}
