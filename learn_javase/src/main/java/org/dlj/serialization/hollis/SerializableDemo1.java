package org.dlj.serialization.hollis;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.dlj.serialization.User1;

public class SerializableDemo1 {

	public static void main(String[] args) {
		// Initializes The Object
		User1 user = new User1();
		user.setName("hollis");
		user.setAge(23);
		System.out.println(user);

		// write Obj to File
		try (FileOutputStream fos = new FileOutputStream("tempFile");
				ObjectOutputStream oos = new ObjectOutputStream(fos)) {
			oos.writeObject(user);
		
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Read Obj from file
		File file = new File("tempFile");
		System.out.println(file.getAbsolutePath());
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
			User1 newUser = (User1)ois.readObject();
			System.out.println(newUser);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
