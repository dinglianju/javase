package org.dlj.serialization.hollis;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

public class ArrayListSerializableProblem {

	public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException {
		List<String> stringList = new ArrayList<String>();
		stringList.add("hello");
		stringList.add("world");
		stringList.add("hollis");
		stringList.add("chuang");
		stringList.add(null);
		
		System.out.println("init StringList: " + stringList);
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream("stringlist"));
		objectOutputStream.writeObject(stringList);
		
		IOUtils.closeQuietly(objectOutputStream);
		File file = new File("stringlist");
		ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(file));
		List<String> newStringList = (List<String>)objectInputStream.readObject();
		IOUtils.closeQuietly(objectInputStream);
		if (file.exists()) {
			file.delete();
		}
		System.out.println("new StringList: " + newStringList);
		
	}
}
