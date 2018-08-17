package org.dlj.serialization.hollis;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/*
 * Externalizable继承了Serializable，该接口中定义了两个
 * 抽象方法：writeExternal与readExternal方法，当使用Externalizable接口
 * 进行序列化与反序列化的时候需要开发人员重写这两个方法
 * 
 * 在使用Externalizable进行序列化的时候，在读取对象时，会调用
 * 被序列化类的无参构造函数去构建一个新的对象，然后再将被保存对象
 * 的字段的值分别填充到新对象中，所以实现Externalizable接口的类
 * 必须提供一个public的无参构造函数
 */
public class User1 implements Externalizable {

	private String name;
	private int age;
	private String address;
	
//	public User1(String name, int age) {
//		super();
//		this.name = name;
//		this.age = age;
//	}
	
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
	
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		// TODO Auto-generated method stub
		
		// 重写WriteExternal方法
		
		out.writeObject(name);
		out.writeObject(address);
		out.writeInt(age);
		
	}
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		// TODO Auto-generated method stub
		
		// 重写readExternal方法
		name = (String) in.readObject();
		address = (String) in.readObject();
		age = in.readInt();
		
	}
	@Override
	public String toString() {
		return "User1 [name=" + name + ", age=" + age + ", address=" + address + "]";
	}
	
	
}
