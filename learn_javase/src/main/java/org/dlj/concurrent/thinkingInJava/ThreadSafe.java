package org.dlj.concurrent.thinkingInJava;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadSafe {

	public static void main(String[] args) throws InterruptedException {
		// EvenGenerator.main();

		// SynchronizedEvenGenerator.main();

		// MutexEvenGenerator.main();

		// AttemptLocking.main();

		// AtomicityTest.main();

		// SerialNumberChecker.main();

		// AtomicIntegerTest.main();

		// CirticalSection.main();
		// ExplicitCriticalSection.main();

		// DualSynch.main();

		ThreadLocalVariableHolder.main();
	}
}

/**
 * 共享受限资源，不正确访问受限资源
 * 
 * @author zhxg
 *
 */
class EvenChecker implements Runnable {
	private IntGenerator generator;
	private final int id;

	public EvenChecker(IntGenerator g, int ident) {
		generator = g;
		id = ident;
	}

	public void run() {
		while (!generator.isCanceled()) {
			int val = generator.next();
			if (val % 2 != 0) {
				ConcurrentOtherFeature.log.debug("{} not even!", val);
				generator.cancel(); // cancels all EventCheckers
			}
		}
	}

	// test any type of intgenerator
	public static void test(IntGenerator gp, int count) {
		ConcurrentOtherFeature.log.debug("Press Control-C to exit");
		ExecutorService exec = Executors.newCachedThreadPool();
		for (int i = 0; i < count; i++) {
			exec.execute(new EvenChecker(gp, i));
		}
		exec.shutdown();
	}

	// default value for count
	public static void test(IntGenerator gp) {
		test(gp, 10);
	}
}

abstract class IntGenerator {
	private volatile boolean canceled = false;

	public abstract int next();

	// allow this to be canceled
	public void cancel() {
		canceled = true;
	}

	public boolean isCanceled() {
		return canceled;
	}
}

class EvenGenerator extends IntGenerator {
	private int currentEvenValue = 0;

	public int next() {
		++currentEvenValue; // danger point here!
		++currentEvenValue;
		return currentEvenValue;
	}

	public static void main() {
		EvenChecker.test(new EvenGenerator());
	}
}

/**
 * 同步控制EvenGenerator
 * 
 * @author zhxg
 *
 */
class SynchronizedEvenGenerator extends IntGenerator {
	private int currentEvenValue = 0;

	public synchronized int next() {
		++currentEvenValue;
		Thread.yield(); // cause failure faster
		++currentEvenValue;
		return currentEvenValue;
	}

	public static void main() {
		EvenChecker.test(new SynchronizedEvenGenerator());
	}
}

/**
 * 显式的Lock对象
 * 
 * @author zhxg
 *
 */
class MutexEvenGenerator extends IntGenerator {
	private int currentEvenValue = 0;
	private Lock lock = new ReentrantLock();

	public int next() {
		lock.lock();
		try {
			++currentEvenValue;
			Thread.yield();
			++currentEvenValue;
			return currentEvenValue;
		} finally {
			lock.unlock();
		}
	}

	public static void main() {
		EvenChecker.test(new MutexEvenGenerator());
	}
}

/**
 * lock 锁超时
 * 
 * @author zhxg
 *
 */
class AttemptLocking {
	private ReentrantLock lock = new ReentrantLock();

	public void untimed() {
		boolean captured = lock.tryLock();
		try {
			System.out.println("tryLock(): " + captured);
		} finally {
			if (captured)
				lock.unlock();
		}
	}

	public void timed() {
		boolean captured = false;
		try {
			captured = lock.tryLock(2, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		try {
			System.out.println("tryLock(2, TimeUnit.seconds): " + captured);
		} finally {
			if (captured)
				lock.unlock();
		}
	}

	public static void main() {
		final AttemptLocking al = new AttemptLocking();
		al.untimed();
		al.timed();

		new Thread() {
			{
				setDaemon(true);
			}

			public void run() {
				al.lock.lock();
				System.out.println("acquired");

			}
		}.start();
		Thread.yield();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		al.untimed();
		al.timed();
	}
}

/**
 * 原子性测试，读取和设置i变量的值都需要synchronized设置同步 i变量不是volatile还存在可视化问题
 * 
 * @author zhxg
 *
 */
class AtomicityTest implements Runnable {
	private int i = 0;

	public int getValue() {
		return i;
	}

	private synchronized void evenIncrement() {
		i++;
		i++;
	}

	public void run() {
		while (true)
			evenIncrement();
	}

	public static void main() {
		ExecutorService exec = Executors.newCachedThreadPool();
		AtomicityTest at = new AtomicityTest();
		exec.execute(at);
		while (true) {
			int val = at.getValue();
			if (val % 2 != 0) {
				System.out.println(val);
				System.exit(0);
			}
		}
	}
}

/**
 * i++自增加操作非原子性操作问题
 * 
 * @author zhxg
 *
 */
class SerialNumberGenerator {
	private static volatile int serialNumber = 0;

	/**
	 * nextSerialNumber()在没有同步的情况下对共享可变值进行了访问
	 * 
	 * @return
	 */
	public static int nextSerialNumber() {
		return serialNumber++;
	}
}

class CircularSet {
	private int[] array;
	private int len;
	private int index = 0;

	public CircularSet(int size) {
		array = new int[size];
		len = size;
		// initialize to a value not produced
		// by the serialnumberGenerator;
		for (int i = 0; i < size; i++)
			array[i] = -1;
	}

	public synchronized void add(int i) {
		array[index] = i;
		// wrap index and write over old elements
		index = ++index % len;
	}

	public synchronized boolean contains(int val) {
		for (int i = 0; i < len; i++)
			if (array[i] == val)
				return true;
		return false;
	}
}

class SerialNumberChecker {
	private static final int SIZE = 10;
	private static CircularSet serials = new CircularSet(1000);
	private static ExecutorService exec = Executors.newCachedThreadPool();

	static class SerialChecker implements Runnable {
		public void run() {
			while (true) {
				int serial = SerialNumberGenerator.nextSerialNumber();
				if (serials.contains(serial)) {
					System.out.println("Duplicate: " + serial);
					System.exit(0);
				}
				serials.add(serial);
			}
		}
	}

	public static void main() {
		for (int i = 0; i < SIZE; i++) {
			exec.execute(new SerialChecker());
		}

	}
}

/**
 * 原子类
 * 
 * @author zhxg
 *
 */
class AtomicIntegerTest implements Runnable {
	private AtomicInteger i = new AtomicInteger(0);

	public int getValue() {
		return i.get();
	}

	private void evenIncrement() {
		i.addAndGet(2);
	}

	public void run() {
		while (true)
			evenIncrement();
	}

	public static void main() {
		new Timer().schedule(new TimerTask() {
			public void run() {
				System.err.println("Aborting");
				System.exit(0);
			}
		}, 5000); // Terminate after 5 seconds
		ExecutorService exec = Executors.newCachedThreadPool();
		AtomicIntegerTest ait = new AtomicIntegerTest();
		exec.execute(ait);
		while (true) {
			int val = ait.getValue();
			if (val % 2 != 0) {
				System.out.println(val);
				System.exit(0);
			}
		}
	}
}

/**
 * 使用原子类AtomicInteger重写MutexEvenGenerator， 消除synchronized关键字
 * 
 * @author zhxg
 *
 */
class AtomicEvenGenerator extends IntGenerator {
	private AtomicInteger currentEvenValue = new AtomicInteger(0);

	public int next() {
		return currentEvenValue.addAndGet(2);
	}

	public static void main() {
		EvenChecker.test(new AtomicEvenGenerator());
	}
}

/**
 * 临界区，缩小synchronized锁定的范围
 * 
 * @author zhxg
 *
 */
class Pair {
	private int x, y;

	public Pair(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public Pair() {
		this(0, 0);
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public void incrementX() {
		x++;
	}

	public void incrementY() {
		y++;
	}

	public String toString() {
		return "x: " + x + " y: " + y;
	}

	public class PairValuesNotEqualException extends RuntimeException {
		public PairValuesNotEqualException() {
			super("pair values not equal: " + Pair.this);
		}
	}

	// arbitrary invariant -- both variables must be equal:
	public void checkState() {
		if (x != y) {
			throw new PairValuesNotEqualException();
		}
	}
}

// protect a pair inside a thread-safe class
abstract class PairManager {
	AtomicInteger checkCounter = new AtomicInteger(0);
	protected Pair p = new Pair();
	private List<Pair> storage = Collections.synchronizedList(new ArrayList<Pair>());

	public synchronized Pair getPair() {
		// make a copy to keep the original safe
		return new Pair(p.getX(), p.getY());
	}

	// assume this is a time consuming operation
	protected void store(Pair p) {
		storage.add(p);
		try {
			TimeUnit.MILLISECONDS.sleep(50);
		} catch (InterruptedException ignore) {
		}
	}

	public abstract void increment();
}

// synchronize the entire method
class PairManager1 extends PairManager {
	/*
	 * synchronized关键字不属于方法特征签名的组成部分，可以在覆盖方法的时候加上去
	 * 
	 * @see org.dlj.concurrent.thinkingInJava.PairManager#increment()
	 */
	public synchronized void increment() {
		p.incrementX();
		p.incrementY();
		store(getPair());
	}
}

// Use a critical section
class PairManager2 extends PairManager {
	public void increment() {
		Pair temp;
		synchronized (this) {
			p.incrementX();
			p.incrementY();
			temp = getPair();
		}
		store(temp);
	}
}

// synchronize the entire method
class ExplicitPairManager1 extends PairManager {
	private Lock lock = new ReentrantLock();

	public synchronized void increment() {
		lock.lock();
		try {
			p.incrementX();
			p.incrementY();
			store(getPair());
		} finally {
			lock.unlock();
		}
	}
}

// use a critical section
class ExplicitPairManager2 extends PairManager {
	private Lock lock = new ReentrantLock();

	public void increment() {
		Pair temp;
		lock.lock();
		try {
			p.incrementX();
			p.incrementY();
			temp = getPair();
		} finally {
			lock.unlock();
		}
		store(temp);
	}
}

class PairManipulator implements Runnable {
	private PairManager pm;

	public PairManipulator(PairManager pm) {
		this.pm = pm;
	}

	public void run() {
		while (true)
			pm.increment();
	}

	public String toString() {
		return "Pair: " + pm.getPair() + " checkCounter = " + pm.checkCounter.get();
	}
}

class PairChecker implements Runnable {
	private PairManager pm;

	public PairChecker(PairManager pm) {
		this.pm = pm;
	}

	public void run() {
		while (true) {
			pm.checkCounter.incrementAndGet();
			pm.getPair().checkState();
		}
	}
}

class CirticalSection {
	// test the two different approaches
	static void testApproaches(PairManager pman1, PairManager pman2) {
		ExecutorService exec = Executors.newCachedThreadPool();
		PairManipulator pm1 = new PairManipulator(pman1), pm2 = new PairManipulator(pman2);
		PairChecker pcheck1 = new PairChecker(pman1), pcheck2 = new PairChecker(pman2);
		exec.execute(pm1);
		exec.execute(pm2);
		exec.execute(pcheck1);
		exec.execute(pcheck2);
		try {
			TimeUnit.MILLISECONDS.sleep(500);
		} catch (InterruptedException e) {
			System.out.println("Sleep interrupted");
		}
		System.out.println("pm1: " + pm1 + "\npm2: " + pm2);
		System.exit(0);
	}

	public static void main() {
		PairManager pman1 = new PairManager1(), pman2 = new PairManager2();
		testApproaches(pman1, pman2);
	}

}

class ExplicitCriticalSection {
	public static void main() {
		PairManager pman1 = new ExplicitPairManager1(), pman2 = new ExplicitPairManager2();
		CirticalSection.testApproaches(pman1, pman2);
	}
}

/**
 * 在其他对象上同步
 * 
 * @author zhxg
 *
 */
class DualSynch {
	private Object syncObject = new Object();

	public synchronized void f() {
		for (int i = 0; i < 5; i++) {
			System.out.println("f()");
			// Thread.yield();
		}
	}

	public void g() {
		synchronized (syncObject) {
			for (int i = 0; i < 5; i++) {
				System.out.println("g()");
				// Thread.yield();
				try {
					TimeUnit.MILLISECONDS.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void main() {
		final DualSynch ds = new DualSynch();
		new Thread() {
			public void run() {
				ds.f();
			}
		}.start();
		ds.g();
	}
}

/**
 * 线程本地存储 通过根除变量的共享，防止任务在共享资源上产生冲突
 * 
 * @author zhxg
 *
 */
class Accessor implements Runnable {
	private final int id;

	public Accessor(int idn) {
		id = idn;
	}

	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			ThreadLocalVariableHolder.increment();
			System.out.println(this);
			Thread.yield();
		}
	}

	public String toString() {
		return "#" + id + ": " + ThreadLocalVariableHolder.get();
	}
}

class ThreadLocalVariableHolder {
	private static ThreadLocal<Integer> value = new ThreadLocal<Integer>() {
		private Random rand = new Random(47);

		protected synchronized Integer initialValue() {
			return rand.nextInt(10000);
		}
	};

	public static void increment() {
		value.set(value.get() + 1);
	}

	public static int get() {
		return value.get();
	}

	public static void main() throws InterruptedException {
		ExecutorService exec = Executors.newCachedThreadPool();
		for (int i = 0; i < 5; i++)
			exec.execute(new Accessor(i));
		TimeUnit.SECONDS.sleep(3);
		exec.shutdownNow(); // all accessors will quit
	}
}