package org.dlj.serialization.hollis.singleton;

import java.io.Serializable;

/**
 * 使用双重校验锁方式实现单例
 * @author zhxg
 *
 */
public class Singleton implements Serializable {
	
	private volatile static Singleton singleton;
	
	private Singleton() {}
	
	public static Singleton getSingleton() {
		if (singleton == null) {
			synchronized (Singleton.class) {
				if (singleton == null) {
					singleton = new Singleton();
				}
			}
		}
		return singleton;
	}
	
	/**
	 * 重写该方法可以防止序列化破坏单例模式
	 * @return
	 */
	private Object readResolve() {
		return singleton;
	}
}
