package org.dlj.serialization.hollis.singleton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SerializableDemo1 {

	public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("tempFile"));
		oos.writeObject(Singleton.getSingleton());
		
		File file = new File("tempFile");
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
		Singleton newInstance = (Singleton) ois.readObject();
		System.out.println(file.getAbsolutePath());
		// 判断是否是同一个对象
		System.out.println(newInstance == Singleton.getSingleton());
	}
	
	
}
