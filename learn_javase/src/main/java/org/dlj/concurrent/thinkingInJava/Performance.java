package org.dlj.concurrent.thinkingInJava;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.dlj.concurrent.thinkingInJava.utils.CountingGenerator;
import org.dlj.concurrent.thinkingInJava.utils.CountingIntegerList;
import org.dlj.concurrent.thinkingInJava.utils.Generated;
import org.dlj.concurrent.thinkingInJava.utils.MapData;
import org.dlj.concurrent.thinkingInJava.utils.RandomGenerator;

public class Performance {

	public static void main(String[] args) throws InterruptedException {
		// SimpleMicroBenchmark.main();

		// SynchronizationComparisons.main();

		// ListComparisons.main();

//		MapComparisons.main();
		
//		FastSimulation.main();
		
//		ReaderWriterList.main();
		
		ActiveObjectDemo.main();
	}
}

/**
 * 微基准测试 问题
 * 
 * @author zhxg
 *
 */
class SimpleMicroBenchmark {
	static abstract class Incrementable {
		protected long counter = 0;

		public abstract void increment();
	}

	static class SynchronizingTest extends Incrementable {
		public synchronized void increment() {
			++counter;
		}
	}

	static class LockingTest extends Incrementable {
		private Lock lock = new ReentrantLock();

		public void increment() {
			lock.lock();
			try {
				++counter;
			} finally {
				lock.unlock();
			}
		}
	}

	static long test(Incrementable incr) {
		long start = System.nanoTime();
		for (long i = 0; i < 10000000l; i++)
			incr.increment();
		return System.nanoTime() - start;
	}

	public static void main() {
		long synchTime = test(new SynchronizingTest());
		long lockTime = test(new LockingTest());
		System.out.printf("synchronized: %1$10d\n", synchTime);
		System.out.printf("Lock:		 %1$10d\n", lockTime);
		System.out.printf("lock/synchronized = %1$.3f", (double) lockTime / (double) synchTime);
	}
}

/**
 * 性能测试
 * 
 * @author zhxg
 *
 */

class SynchronizationComparisons {
	static BaseLine baseLine = new BaseLine();
	static SynchronizedTest synch = new SynchronizedTest();
	static LockTest lock = new LockTest();
	static AtomicTest atomic = new AtomicTest();

	static void test() {
		System.out.println("===============");
		System.out.printf("%-12s : %13d\n", "Cycles", Accumulator.cycles);
		baseLine.timedTest();
		synch.timedTest();
		lock.timedTest();
		atomic.timedTest();
		Accumulator.report(synch, baseLine);
		Accumulator.report(lock, baseLine);
		Accumulator.report(atomic, baseLine);
		Accumulator.report(synch, lock);
		Accumulator.report(synch, atomic);
		Accumulator.report(lock, atomic);
	}

	static abstract class Accumulator {
		public static long cycles = 50000l;
		// Number of Modifiers and Readers during each test
		private static final int N = 4;
		public static ExecutorService exec = Executors.newFixedThreadPool(N * 2);
		private static CyclicBarrier barrier = new CyclicBarrier(N * 2 + 1);
		protected volatile int index = 0;
		protected volatile long value = 0;
		protected long duration = 0;
		protected String id = "error";
		protected final static int SIZE = 100000;
		protected static int[] preLoaded = new int[SIZE];
		static {
			// load the array of random numbers
			Random rand = new Random(47);
			for (int i = 0; i < SIZE; i++)
				preLoaded[i] = rand.nextInt();
		}

		public abstract void accumulate();

		public abstract long read();

		private class Modifier implements Runnable {
			public void run() {
				for (long i = 0; i < cycles; i++)
					accumulate();
				try {
					barrier.await();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}

		private class Reader implements Runnable {
			private volatile long value;

			public void run() {
				for (long i = 0; i < cycles; i++)
					value = read();
				try {
					barrier.await();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}

		public void timedTest() {
			long start = System.nanoTime();
			for (int i = 0; i < N; i++) {
				exec.execute(new Modifier());
				exec.execute(new Reader());
			}
			try {
				barrier.await();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			duration = System.nanoTime() - start;
			System.out.printf("%-13s: %13d\n", id, duration);
		}

		public static void report(Accumulator acc1, Accumulator acc2) {
			System.out.printf("%-22s: %.2f\n", acc1.id + "/" + acc2.id,
					(double) acc1.duration / (double) acc2.duration);
		}
	}

	static class BaseLine extends Accumulator {
		{
			id = "BaseLine";
		}

		public void accumulate() {
			// 修复数据越界异常 方法一：使用局部变量
			// int i = index++;
			// if (i >= SIZE) {
			// System.out.println(Thread.currentThread() + "id :" + id + "; i: " + i + ";
			// index: " + index);
			// index = 0;
			// i = 0;
			// }
			// value += preLoaded[i];
			// 方法二：
			value += preLoaded[index++ % SIZE];
			if (index >= SIZE)
				index = 0;

			// value += preLoaded[index++];
			// if (index >= SIZE) index = 0;
		}

		public long read() {
			return value;
		}
	}

	static class SynchronizedTest extends Accumulator {
		{
			id = "synchronized";
		}

		public synchronized void accumulate() {
			value += preLoaded[index++];
			if (index >= SIZE)
				index = 0;
		}

		public synchronized long read() {
			return value;
		}
	}

	static class LockTest extends Accumulator {
		{
			id = "Lock";
		}
		private Lock lock = new ReentrantLock();

		public void accumulate() {
			lock.lock();
			try {
				value += preLoaded[index++];
				if (index >= SIZE)
					index = 0;
			} finally {
				lock.unlock();
			}
		}

		public long read() {
			lock.lock();
			try {
				return value;
			} finally {
				lock.unlock();
			}
		}
	}

	static class AtomicTest extends Accumulator {
		{
			id = "Atomic";
		}
		private AtomicInteger index = new AtomicInteger(0);
		private AtomicLong value = new AtomicLong(0);

		public void accumulate() {
			// oops! Relying on more than one atomic at
			// a time doesn't work. but it still gives us
			// a performance indicator
			// 使用局部变量修复数组越界异常
			// int i = index.getAndIncrement();
			// if (++i >= SIZE) {
			// System.out.println(Thread.currentThread() + "id :" + id + "; i: " + i + ";
			// index: " + index.get());
			// index.set(0);
			// i = 0;
			// }
			// value.getAndAdd(preLoaded[i]);

			int i = index.getAndIncrement();
			value.getAndAdd(preLoaded[i % SIZE]);
			if (++i >= SIZE)
				index.set(0);

			// int i = index.getAndIncrement();
			// value.getAndAdd(preLoaded[i]);
			// if (++ i >= SIZE)
			// index.set(0);
		}

		public long read() {
			return value.get();
		}
	}

	public static void main() {
		int iterations = 4; // default
		System.out.println("Warmup");
		// baseLine.timedTest();
		// Now the initial test doesn't include the cost
		// of starting the threads for the first time
		// Produce multiple data points
		for (int i = 0; i < iterations; i++) {
			test();
			Accumulator.cycles *= 2;
		}
		Accumulator.exec.shutdown();
	}
}

/**
 * 免锁容器 乐观锁
 * 
 * @author zhxg
 *
 * @param <C>
 */
abstract class Tester<C> {
	static int testReps = 10;
	static int testCycles = 1000;
	static int containerSize = 1000;

	abstract C containerInitializer();

	abstract void startReadersAndWriters();

	C testContainer;
	String testId;
	int nReaders;
	int nWriters;
	volatile long readResult = 0;
	volatile long readTime = 0;
	volatile long writeTime = 0;
	CountDownLatch endLatch;
	static ExecutorService exec = Executors.newCachedThreadPool();
	Integer[] writeData;

	Tester(String testId, int nReaders, int nWriters) {
		this.testId = testId + " " + nReaders + "r " + nWriters + "w";
		this.nReaders = nReaders;
		this.nWriters = nWriters;
		writeData = Generated.array(Integer.class, new RandomGenerator.Integer(), containerSize);
		for (int i = 0; i < testReps; i++) {
			runTest();
			readTime = 0;
			writeTime = 0;
		}
	}

	void runTest() {
		endLatch = new CountDownLatch(nReaders + nWriters);
		testContainer = containerInitializer();
		startReadersAndWriters();
		try {
			endLatch.await();
		} catch (InterruptedException e) {
			System.out.println("endLatch interrupted");
		}
		System.out.printf("%-27s %14d %14d\n", testId, readTime, writeTime);
		if (readTime != 0 && writeTime != 0)
			System.out.printf("%-27s %14d\n", "readTime + writeTime = ", readTime + writeTime);
	}

	abstract class TestTask implements Runnable {
		abstract void test();

		abstract void putResults();

		long duration;

		public void run() {
			long startTime = System.nanoTime();
			test();
			duration = System.nanoTime() - startTime;
			synchronized (Tester.this) {
				putResults();
			}
			endLatch.countDown();
		}
	}

	public static void initMain(String[] args) {
		if (args.length > 0)
			testReps = new Integer(args[0]);
		if (args.length > 1)
			testCycles = new Integer(args[1]);
		if (args.length > 2)
			containerSize = new Integer(args[2]);
		System.out.printf("%-27s %14s %14s\n", "Type", "Read time", "Write time");
	}
}

/**
 * synchronized list和CopyOnWrite list性能比较
 * 
 * @author zhxg
 *
 */
class ListComparisons {
	abstract class ListTest extends Tester<List<Integer>> {
		ListTest(String testId, int nReaders, int nWriters) {
			super(testId, nReaders, nWriters);
		}

		class Reader extends TestTask {
			long result = 0;

			void test() {
				for (long i = 0; i < testCycles; i++)
					for (int index = 0; index < containerSize; index++)
						result += testContainer.get(index);
			}

			void putResults() {
				readResult += result;
				readTime += duration;
			}
		}

		class Writer extends TestTask {
			void test() {
				for (int index = 0; index < containerSize; index++)
					testContainer.set(index, writeData[index]);
			}

			void putResults() {
				writeTime += duration;
			}
		}

		void startReadersAndWriters() {
			for (int i = 0; i < nReaders; i++)
				exec.execute(new Reader());
			for (int i = 0; i < nWriters; i++)
				exec.execute(new Writer());
		}
	}

	class SynchronizedArrayListTest extends ListTest {
		List<Integer> containerInitializer() {
			return Collections.synchronizedList(new ArrayList<Integer>(new CountingIntegerList(containerSize)));
		}

		SynchronizedArrayListTest(int nReaders, int nWriters) {
			super("Synched ArrayList", nReaders, nWriters);
		}
	}

	class CopyOnWriteArrayListTest extends ListTest {
		List<Integer> containerInitializer() {
			return new CopyOnWriteArrayList<Integer>(new CountingIntegerList(containerSize));
		}

		CopyOnWriteArrayListTest(int nReaders, int nWriters) {
			super("CopyOnWriteArrayList", nReaders, nWriters);
		}
	}

	public static void main() {
		ListComparisons lc = new ListComparisons();
		String[] args = new String[0];
		Tester.initMain(args);
		lc.new SynchronizedArrayListTest(10, 0);
		lc.new SynchronizedArrayListTest(9, 1);
		lc.new SynchronizedArrayListTest(5, 5);
		lc.new CopyOnWriteArrayListTest(10, 0);
		lc.new CopyOnWriteArrayListTest(9, 1);
		lc.new CopyOnWriteArrayListTest(5, 5);
		Tester.exec.shutdown();
	}
}

/**
 * synchronized hashmap与 concurrentHashMap性能比较
 * 
 * @author zhxg
 *
 */
class MapComparisons {
	abstract class MapTest extends Tester<Map<Integer, Integer>> {
		MapTest(String testId, int nReaders, int nWriters) {
			super(testId, nReaders, nWriters);
		}

		class Reader extends TestTask {
			long result = 0;

			void test() {
				for (long i = 0; i < testCycles; i++)
					for (int index = 0; index < containerSize; index++)
						result += testContainer.get(index);
			}

			void putResults() {
				readResult += result;
				readTime += duration;
			}
		}

		class Writer extends TestTask {
			void test() {
				for (long i = 0; i < testCycles; i++)
					for (int index = 0; index < containerSize; index++)
						testContainer.put(index, writeData[index]);
			}

			void putResults() {
				writeTime += duration;
			}
		}

		void startReadersAndWriters() {
			for (int i = 0; i < nReaders; i++)
				exec.execute(new Reader());
			for (int i = 0; i < nWriters; i++)
				exec.execute(new Writer());
		}
	}

	class SynchronizedHashMapTest extends MapTest {
		Map<Integer, Integer> containerInitializer() {
			return Collections.synchronizedMap(new HashMap<Integer, Integer>(
					MapData.map(new CountingGenerator.Integer(), new CountingGenerator.Integer(), containerSize)));
		}

		SynchronizedHashMapTest(int nReaders, int nWriters) {
			super("Synched HashMap", nReaders, nWriters);
		}
	}

	class ConcurrentHashMapTest extends MapTest {
		Map<Integer, Integer> containerInitializer() {
			return new ConcurrentHashMap<Integer, Integer>(
					MapData.map(new CountingGenerator.Integer(), new CountingGenerator.Integer(), containerSize));
		}

		ConcurrentHashMapTest(int nReaders, int nWriters) {
			super("ConcurrentHashMap", nReaders, nWriters);
		}
	}

	public static void main() {
		MapComparisons mc = new MapComparisons();
		String[] args = new String[0];
		Tester.initMain(args);
		mc.new SynchronizedHashMapTest(10, 0);
		mc.new SynchronizedHashMapTest(9, 1);
		mc.new SynchronizedHashMapTest(5, 5);
		mc.new ConcurrentHashMapTest(10, 0);
		mc.new ConcurrentHashMapTest(9, 1);
		mc.new ConcurrentHashMapTest(5, 5);
		Tester.exec.shutdown();
	}
}

/**
 * 乐观加锁CAS
 * 
 * @author zhxg
 *
 */
class FastSimulation {
	static final int N_ELEMENTS = 100000;
	static final int N_GENES = 30;
	static final int N_EVOLVERS = 50;
	static final AtomicInteger[][] GRID = new AtomicInteger[N_ELEMENTS][N_GENES];
	static Random rand = new Random(47);

	static class Evolver implements Runnable {
		public void run() {
			while (!Thread.interrupted()) {
				// Randomly select an element to work on
				int element = rand.nextInt(N_ELEMENTS);
				for (int i = 0; i < N_GENES; i++) {
					int previous = element - 1;
					if (previous < 0)
						previous = N_ELEMENTS - 1;
					int next = element + 1;
					if (next >= N_ELEMENTS)
						next = 0;
					int oldvalue = GRID[element][i].get();
					// Perform some kind of modeling calculation
					int newvalue = oldvalue + GRID[previous][i].get() + GRID[next][i].get();
					newvalue /= 3; // Average the three values;
					if (!GRID[element][i].compareAndSet(oldvalue, newvalue)) {
						// Policy here to deal with failure. Here we
						// just report it and ignore it: our model
						// will eventually deal with it
						System.out.println("Old value changed from " + oldvalue);
					}
				}
			}
		}
	}

	public static void main() throws InterruptedException {
		ExecutorService exec = Executors.newCachedThreadPool();
		for (int i = 0; i < N_ELEMENTS; i++)
			for (int j = 0; j < N_GENES; j++) {
				GRID[i][j] = new AtomicInteger(rand.nextInt(1000));
			}
		for (int i = 0; i < N_EVOLVERS; i++)
			exec.execute(new Evolver());
		TimeUnit.SECONDS.sleep(5);
		exec.shutdownNow();
	}
}

/**
 * ReadWriteLock读写锁
 * @author zhxg
 *
 * @param <T>
 */
class ReaderWriterList<T> {
	private ArrayList<T> lockedList;
	// Make the ordering fair
	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);

	public ReaderWriterList(int size, T initialValue) {
		lockedList = new ArrayList<T>(Collections.nCopies(size, initialValue));
	}

	public T set(int index, T element) {
		Lock wlock = lock.writeLock();
		wlock.lock();
		try {
			return lockedList.set(index, element);
		} finally {
			wlock.unlock();
		}
	}

	public T get(int index) {
		Lock rlock = lock.readLock();
		rlock.lock();
		try {
			// Show that multiple readers
			// may acquire the read lock
			if (lock.getReadLockCount() > 1)
				System.out.println(lock.getReadLockCount());
			return lockedList.get(index);
		} finally {
			rlock.unlock();
		}
	}

	static class ReaderWriterListTest {
		ExecutorService exec = Executors.newCachedThreadPool();
		private final static int SIZE = 100;
		private static Random rand = new Random(47);
		private ReaderWriterList<Integer> list = new ReaderWriterList<Integer>(SIZE, 0);

		private class Writer implements Runnable {
			public void run() {
				try {
					for (int i = 0; i < 20; i++) { // 2 second test
						list.set(i, rand.nextInt());
						TimeUnit.MILLISECONDS.sleep(100);
					}
				} catch (InterruptedException e) {
					// Acceptable way to exit
				}
				System.out.print("Writer finished, shutting down");
				exec.shutdownNow();
			}
		}

		private class Reader implements Runnable {
			public void run() {
				try {
					while (!Thread.interrupted()) {
						for (int i = 0; i < SIZE; i++) {
							list.get(i);
							TimeUnit.MILLISECONDS.sleep(1);
						}
					}
				} catch (InterruptedException e) {
					// Acceptable way to exit
				}
			}
		}

		public ReaderWriterListTest(int readers, int writers) {
			for (int i = 0; i < readers; i++)
				exec.execute(new Reader());
			for (int i = 0; i < writers; i++)
				exec.execute(new Writer());
		}
	}

	public static void main() {
		new ReaderWriterListTest(30, 1);
	}
}

/**
 * 活动对象
 * 
 * @author zhxg
 *
 */
class ActiveObjectDemo {
	private ExecutorService ex = Executors.newSingleThreadExecutor();
	private Random rand = new Random(47);

	// Insert a random delay to produce the effect
	// of a calculation time
	private void pause(int factor) {
		try {
			TimeUnit.MILLISECONDS.sleep(100 + rand.nextInt(factor));
		} catch (InterruptedException e) {
			System.out.print("sleep() interrupted");
		}
	}

	public Future<Integer> calculateInt(final int x, final int y) {
		return ex.submit(new Callable<Integer>() {
			public Integer call() {
				System.out.println("starting " + x + " + " + y);
				pause(500);
				return x + y;
			}
		});
	}

	public Future<Float> calculateFloat(final float x, final float y) {
		return ex.submit(new Callable<Float>() {
			public Float call() {
				System.out.println("starting " + x + " + " + y);
				pause(2000);
				return x + y;
			}
		});
	}

	public void shutdown() {
		ex.shutdown();
	}

	public static void main() {
		ActiveObjectDemo d1 = new ActiveObjectDemo();
		// Prevents ConcurrentModificationException
		List<Future<?>> results = new CopyOnWriteArrayList<Future<?>>();
		for (float f = 0.0f; f < 1.0f; f += 0.2f)
			results.add(d1.calculateFloat(f, f));
		for (int i = 0; i < 5; i++)
			results.add(d1.calculateInt(i, i));
		System.out.println("All asynch calls made");
		while (results.size() > 0) {
			for (Future<?> f : results)
				if (f.isDone()) {
					try {
						System.out.println(f.get());
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
					results.remove(f);
				}
		}
		d1.shutdown();
	}
}