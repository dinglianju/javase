package org.dlj.io.nio;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.concurrent.TimeUnit;

/**
 * 内存映射文件
 * @author zhxg
 *
 */
class LargeMappedFiles {
	static int length = 0x8ffffff; // 128Mb
	public static void main() throws FileNotFoundException, IOException {
		// MappedByBuffer 特殊类型的直接缓冲器
		MappedByteBuffer out = new RandomAccessFile("test.dat", "rw").getChannel().map(FileChannel.MapMode.READ_WRITE, 0, length);
		for (int i = 0; i < length; i++) {
			out.put((byte)'x');
		}
		System.out.println("Finished writing");
		for (int i = length/2; i < length/2 + 6; i++) {
			System.out.println((char)out.getChar(i));
		}
	}
	
	
}

/**
 * 测试stream io和nio对文件操作的性能
 * @author zhxg
 *
 */
public class MappedIO {
	private static int numOfInts = 4000000;
	private static int numOfUbuffInts = 200000;
	
	private abstract static class Tester {
		private String name;
		public Tester(String name) {
			this.name = name;
		}
		public void runTest() {
			System.out.print(name + ": ");
			try {
				long start = System.nanoTime();
				test();
				double duration = System.nanoTime() - start;
				System.out.format("%.2f\n", duration/1.0e9);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		public abstract void test() throws IOException;
	}
	
	private static Tester[] tests = {
			new Tester("Stream Write") {
				public void test() throws IOException {
					DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(new File("temp.tmp"))));
					for (int i = 0; i < numOfInts; i++)
						dos.writeInt(i);
					dos.close();
				}
			},
			new Tester("Mapped Write") {
				public void test() throws IOException {
					FileChannel fc = new RandomAccessFile("temp.tmp", "rw").getChannel();
					IntBuffer ib = fc.map(FileChannel.MapMode.READ_WRITE, 0, fc.size()).asIntBuffer();
					for (int i = 0; i < numOfInts; i++)
						ib.put(i);
					fc.close();
				}
			},
			new Tester("Stream Read") {
				public void test() throws IOException {
					DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream("temp.tmp")));
					for (int i = 0; i < numOfInts; i++)
						dis.readInt();
					dis.close();
				}
			},
			new Tester("Mapped Read") {
				public void test() throws IOException {
					FileChannel fc = new FileInputStream(new File("temp.tmp")).getChannel();
					IntBuffer ib = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size()).asIntBuffer();
					while (ib.hasRemaining())
						ib.get();
					fc.close();
				}
			},
			new Tester("Stream Read/Write") {
				public void test() throws IOException {
					RandomAccessFile raf = new RandomAccessFile(new File("temp.tmp"), "rw");
					raf.writeInt(1);
					for(int i = 0; i < numOfUbuffInts; i++) {
						raf.seek(raf.length() - 4);
						raf.writeInt(raf.readInt());
					}
					raf.close();
				}
			},
			new Tester("Mapped Read/Write") {
				public void test() throws IOException {
					FileChannel fc = new RandomAccessFile(new File("temp.tmp"), "rw").getChannel();
					IntBuffer ib = fc.map(FileChannel.MapMode.READ_WRITE, 0, fc.size()).asIntBuffer();
					ib.put(0);
					for (int i = 1; i < numOfUbuffInts; i++)
						ib.put(ib.get(i - 1));
					fc.close();
				}
			}
	};
	
	public static void main() {
		for (Tester test : tests)
			test.runTest();
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		// LargeMappedFilesmain();
		
		// MappedIO.main();
		
		// FileLocking.main();
		
		LockingMappedFiles.main();
	}
	
}

/**
 * 文件加锁
 * @author zhxg
 *
 */
class FileLocking {
	public static void main() throws IOException, InterruptedException {
		FileOutputStream fos = new FileOutputStream("file.txt");
		FileLock fl = fos.getChannel().tryLock();
		if (fl != null) {
			System.out.println("Locked File");
			TimeUnit.MILLISECONDS.sleep(100);
			fl.release();
			System.out.println("Released Lock");
		}
		
		fos.close();
	}
}

class LockingMappedFiles {
	static final int LENGTH = 0x8ffffff; // 128MB
	static FileChannel fc;
	public static void main() throws IOException {
		fc = new RandomAccessFile("test.dat", "rw").getChannel();
		MappedByteBuffer out = fc.map(FileChannel.MapMode.READ_WRITE, 0, LENGTH);
		for (int i = 0; i < LENGTH; i++)
			out.put((byte)'x');
		new LockAndModify(out, 0, 0 + LENGTH/3);
		new LockAndModify(out, LENGTH/2, LENGTH/2 + LENGTH/4);
	}
	
	private static class LockAndModify extends Thread {
		private ByteBuffer buff;
		private int start, end;
		LockAndModify(ByteBuffer mbb, int start, int end) {
			this.start = start;
			this.end = end;
			mbb.limit(end);
			mbb.position(start);
			buff = mbb.slice();
			start();
		}
		
		public void run() {
			try {
				// Exclusive lock with no overlap:
				FileLock fl = fc.lock(start, end, false);
				System.out.println("Locked: " + start + " to " + end);
				// Perform modification:
				while (buff.position() < buff.limit() - 1)
					buff.put((byte)(buff.get() + 1));
				fl.release();
				System.out.println("released: " + start + " to " + end);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}