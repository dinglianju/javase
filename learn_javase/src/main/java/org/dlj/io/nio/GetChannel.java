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
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.MappedByteBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Iterator;
import java.util.SortedMap;

public class GetChannel {

	private static final int BSIZE = 1024;
	public static void main() throws IOException {
		// write a file
		FileChannel fc = new FileOutputStream("data.txt").getChannel();
		fc.write(ByteBuffer.wrap("Some text ".getBytes()));
		fc.close();
		// Add to the end of the file
		fc = new RandomAccessFile("data.txt", "rw").getChannel();
		fc.position(fc.size());	// move to the end
		fc.write(ByteBuffer.wrap("Some more".getBytes()));
		fc.close();
		// read the file
		fc = new FileInputStream("data.txt").getChannel();
		ByteBuffer buff = ByteBuffer.allocate(BSIZE);
		fc.read(buff);
		// 一旦调用read()来告知FileChannel向ByteBuffer存储字节
		// 就必须调用缓冲器上的flip()，让它做好让别人读取字节的准备
		// 如果我们打算使用缓冲器执行进一步的read()操作，也必须用clear()来为每个read()做好准备
		buff.flip();
		while(buff.hasRemaining()) {
			System.out.print((char)buff.get());
		}
		
	}
	
	public static void main(String[] args) throws IOException {
		// main();
		 
		// ChannelCopy.main();
		
		// TransferTo.main();
		
		// BufferToText.main();
		// AvailableCharSets.main();
		
		//GetData.main();
		
		//IntBufferDemo.main();
		
		//ViewBuffers.main();
		
		//Endians.main();
		
		UsingBuffers.main();
		
		
		
	}
}

class ChannelCopy{
	private static final int BSIZE = 200;
	public static void main() throws IOException {
		String[] args = {"README.md", "data.txt"};
		FileChannel
			in = new FileInputStream(args[0]).getChannel(),
			out = new FileOutputStream(args[1]).getChannel();
		ByteBuffer buffer = ByteBuffer.allocate(BSIZE);
		while (in.read(buffer) != -1) {
			buffer.flip(); // prepare for writing
			out.write(buffer);
			buffer.clear(); // prepare for reading
		}
	}
}

class TransferTo {
	public static void main() throws IOException {
		String[] args = {"README.MD", "data.txt"};
		FileChannel
			in = new FileInputStream(args[0]).getChannel(),
			out = new FileOutputStream(args[1]).getChannel();
		System.out.println(in.size());
		in.transferTo(0, in.size(), out);
		// or:
		//out.transferFrom(in, 0, in.size());
	}
}

/**
 * 转换数据
 * buff缓冲器中容纳的是普通的字节，为了把它们转换为字符，
 * 1.在输入它们的时候对其进行编码（这样，它们输出时才有意义）
 * 2.将其从缓冲器输出时对它们进行解码
 * @author zhxg
 *
 */
class BufferToText {
	private static final int BSIZE = 1024;
	public static void main() throws IOException {
		FileChannel fc = new FileOutputStream("data2.txt").getChannel();
		fc.write(ByteBuffer.wrap("some text".getBytes()));
		fc.close();
		fc = new FileInputStream("data2.txt").getChannel();
		ByteBuffer buff = ByteBuffer.allocate(BSIZE);
		
		fc.read(buff);
		buff.flip();
		// Doesn't work:
		System.out.println(buff.asCharBuffer());
		// Decode using this system's default charset:
		buff.rewind();
		String encoding = System.getProperty("file.encoding");
		System.out.println("decoded using " + encoding + ": " + Charset.forName(encoding).decode(buff));
		
		// or, we could encode with something that will print:
		fc = new FileOutputStream("data2.txt").getChannel();
		fc.write(ByteBuffer.wrap("some text".getBytes("UTF-16BE")));
		fc.close();
		// now try reading again:
		fc = new FileInputStream("data2.txt").getChannel();
		buff.clear();
		fc.read(buff);
		buff.flip();
		System.out.println(buff.asCharBuffer());
		// use a charbuffer to write through:
		fc = new FileOutputStream("data2.txt").getChannel();
		buff = ByteBuffer.allocate(24); // more than needed
		buff.asCharBuffer().put("some text");
		fc.write(buff);
		fc.close();
		// read and display
		fc = new FileInputStream("data2.txt").getChannel();
		buff.clear();
		fc.read(buff);
		buff.flip();
		System.out.println(buff.asCharBuffer());
	}
}

class AvailableCharSets{
	public static void main() {
		SortedMap<String, Charset> charSets = Charset.availableCharsets();
		Iterator<String> it = charSets.keySet().iterator();
		while(it.hasNext()) {
			String csName = it.next();
			System.out.print(csName);;
			Iterator aliases = charSets.get(csName).aliases().iterator();
			if (aliases.hasNext())
				System.out.print(": ");
			while(aliases.hasNext()) {
				System.out.println(aliases.next());
				if (aliases.hasNext())
					System.out.print(", ");
			}
			System.out.println();
			
		}
	}
}

/**
 * 获取基本类型
 * @author zhxg
 *
 */
class GetData{
	private static final int BSIZE = 1024;
	public static void main() {
		ByteBuffer bb = ByteBuffer.allocate(BSIZE);
		// Allocation automatically zeroes the ByteBuffer:
		int i = 0;
		while(i++ < bb.limit()) {
			if (bb.get() != 0) {
				System.out.println("nonzero");
			}
			System.out.print("i = " + i);
			bb.rewind();
			// Store and read a char array:
			bb.asCharBuffer().put("Howdy!");
			char c;
			while((c = bb.getChar()) != 0) {
				System.out.print(c + " ");
			}
			System.out.println();
			bb.rewind();
			// store and read a short:
			bb.asShortBuffer().put((short)471142);
			System.out.print(bb.getShort() + "  ");
			bb.rewind();
			// store and read an int:
			bb.asIntBuffer().put(99471142);
			System.out.print(bb.getInt() + "  ");
			bb.rewind();
			// store and read a long:
			bb.asLongBuffer().put(99471142);
			System.out.print(bb.getLong() + "  ");
			bb.rewind();
			// store and read a float:
			bb.asFloatBuffer().put(99471142);
			System.out.print(bb.getFloat() + "  ");
			bb.rewind();
			// store and read a double:
			bb.asDoubleBuffer().put(99471142);
			System.out.print(bb.getDouble() + "  ");
			bb.rewind();
		}
	}
}

/**
 * 通过IntBuffer操纵ByteBuffer中的int型数据
 * @author zhxg
 *
 */
class IntBufferDemo {
	private static final int BSIZE = 1024;
	public static void main() {
		ByteBuffer bb = ByteBuffer.allocate(BSIZE);
		IntBuffer ib = bb.asIntBuffer();
		// Store an array of int:
		ib.put(new int[]{11, 42, 47, 99, 143, 811,1016});
		// Absolute location read and write:
		System.out.println(ib.get(3));
		ib.put(3, 1811);
		// Setting a new limit before rewinding the buffer
		ib.flip();
		//ib.rewind();
		while(ib.hasRemaining()) {
			int i = ib.get();
			System.out.println(i);
		}
	}
}

class ViewBuffers {
	public static void main() {
		ByteBuffer bb = ByteBuffer.wrap(new byte[] {0, 0, 0, 0, 0, 0, 0, 'a'});
		bb.rewind();
		System.out.println("byte buffer ");
		while(bb.hasRemaining()) {
			System.out.print(bb.position() + " -> " + bb.get() + ", ");
		}
		System.out.println();
		
		CharBuffer cb = ((ByteBuffer)bb.rewind()).asCharBuffer();
		System.out.println("Char Buffer ");
		while(cb.hasRemaining()) {
			System.out.print(cb.position() + " -> " + cb.get() + ", ");
		}
		System.out.println();
		
		FloatBuffer fb = ((ByteBuffer)bb.rewind()).asFloatBuffer();
		System.out.println("Float Buffer ");
		while(fb.hasRemaining()) {
			System.out.print(fb.position() + " -> " + fb.get() + ", ");
		}
		System.out.println();
		
		IntBuffer ib = ((ByteBuffer)bb.rewind()).asIntBuffer();
		System.out.println("Int Buffer ");
		while(ib.hasRemaining()) {
			System.out.print(ib.position() + " -> " + ib.get() + ", ");
		}
		System.out.println();
		
		LongBuffer lb = ((ByteBuffer)bb.rewind()).asLongBuffer();
		System.out.println("Long Buffer ");
		while (lb.hasRemaining()) {
			System.out.print(lb.position() + " -> " + lb.get() + ", ");
		}
		System.out.println();
		
		ShortBuffer sb = ((ByteBuffer)bb.rewind()).asShortBuffer();
		System.out.println("Short Buffer ");
		while(sb.hasRemaining()) {
			System.out.print(sb.position() + " -> " + sb.get() + ", ");
		}
		System.out.println();
		
		DoubleBuffer db = ((ByteBuffer)bb.rewind()).asDoubleBuffer();
		System.out.println("Double Buffer ");
		while(db.hasRemaining()) {
			System.out.print(db.position() + " -> " + db.get() + ". ");
		}
		
	}
}

/**
 * 字节存放次序：大端（Big endian），小端（litter endian）
 * @author zhxg
 *
 */
class Endians {
	public static void main() {
		ByteBuffer bb = ByteBuffer.wrap(new byte[12]);
		bb.asCharBuffer().put("abcdef");
		System.out.println(Arrays.toString(bb.array()));
		
		bb.rewind();
		bb.order(ByteOrder.BIG_ENDIAN);
		bb.asCharBuffer().put("abcdef");
		System.out.println(Arrays.toString(bb.array()));
		
		bb.rewind();
		bb.order(ByteOrder.LITTLE_ENDIAN);
		// 通过charBuffer视图将charArray插入到ByteBuffer中
		bb.asCharBuffer().put("abcdef");
		// 展示底层的byteBuffer中的字节
		System.out.println(Arrays.toString(bb.array()));
	}
	
	
}

/**
 * 演示在缓冲器中插入和提取数据的方法会更新这些索引，用于反映所发生的变化
 * 交换相邻字符
 * @author zhxg
 *
 */
class UsingBuffers {
	private static void symmetricScramble(CharBuffer buffer) {
		while (buffer.hasRemaining()) {
			buffer.mark();
			char c1 = buffer.get();
			char c2 = buffer.get();
			buffer.reset();
			buffer.put(c2).put(c1);
		}
	}
	
	public static void main() {
		char[] data = "UsingBuffers".toCharArray();
		ByteBuffer bb = ByteBuffer.allocate(data.length * 2);
		CharBuffer cb = bb.asCharBuffer();
		cb.put(data);
		
		System.out.println(cb.rewind());
		symmetricScramble(cb);
		System.out.println(cb.rewind());
		symmetricScramble(cb);
		System.out.println(cb.rewind());
	}
}

