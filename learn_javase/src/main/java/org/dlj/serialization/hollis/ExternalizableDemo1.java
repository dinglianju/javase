package org.dlj.serialization.hollis;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ExternalizableDemo1 {

	public static void main(String[] args) {
		// Write Obj to file
		//User1 user = new User1("hollis", 23);
		
		User1 user = new User1();
		user.setName("hollis");
		user.setAddress("beijing");
		user.setAge(23);
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("tempFile"))) {
			oos.writeObject(user);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Read Obj from file
		File file = new File("tempFile");
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
			User1 newInstance = (User1)ois.readObject();
			// output
			System.out.println(newInstance);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
