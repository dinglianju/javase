package org.dlj.serialization;

import java.io.Serializable;

/*
 * 虚拟机是否允许序列化，不仅取决于类路径和功能代码是否一致，
 * 一个非常重要的一点是两个类的序列化id是否一致
 * 
 * 序列化并不保存静态变量
 * 
 * 要想将父类对象也序列化，就需要让父类也实现serializable接口
 * 
 * transient关键字的作用是控制变量的序列化，在变量声明前加上该
 * 关键字，可以阻止该变量被序列化到文件中，在被反序列化后，transient变量
 * 的值被初始化初始值，如int型的是0，对象型的是null
 */
public class User1 implements Serializable {
	
	private String name;
	private int age;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	
	@Override
	public String toString() {
		return "User{" + 
				"name='" + name + '\'' +
				", age=" + age + 
				'}';
	}

}
