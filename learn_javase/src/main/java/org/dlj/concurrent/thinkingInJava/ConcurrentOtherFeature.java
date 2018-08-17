package org.dlj.concurrent.thinkingInJava;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConcurrentOtherFeature {

	public static final Logger log = LoggerFactory.getLogger(ConcurrentOtherFeature.class);

	public static final ConcurrentOtherFeature cof = new ConcurrentOtherFeature();

	public static void main(String[] args) throws IOException, InterruptedException {

		// Joining.main();

		// ResponsiveUI.main();

		// ExceptionThread.main();

		// CaptureUncaughtException.main();

		// OrnamentalGarden.main();

		// Interrupting.main();

		// CloseResource.main();

		// NIOInterruption.main();

		// MultiLock.main();

//		Interrupting2.main();
		
		InterruptingIdiom.main();

	}

	/**
	 * 加入一个线程
	 * 
	 * @author zhxg
	 *
	 */
	class Sleeper extends Thread {
		private int duration;

		public Sleeper(String name, int sleepTime) {
			super(name);
			duration = sleepTime;
			start();
		}

		public void run() {
			try {
				sleep(duration);
			} catch (InterruptedException e) {
				log.debug(getName() + " was interrupted. isInterrupted(): " + isInterrupted());
				return;
			}
			log.debug(getName() + " has awakened");
		}
	}

	class Joiner extends Thread {
		private Sleeper sleeper;

		public Joiner(String name, Sleeper sleeper) {
			super(name);
			this.sleeper = sleeper;
			start();
		}

		public void run() {
			try {
				sleeper.join();
			} catch (InterruptedException e) {
				log.debug("Interrupted");
			}
			log.debug(getName() + " join completed");
		}
	}

	static class Joining {
		public static void main() {
			ConcurrentOtherFeature cof = new ConcurrentOtherFeature();
			Sleeper sleepy = cof.new Sleeper("Sleepy", 1500), grumpy = cof.new Sleeper("Grumpy", 1500);
			Joiner dopey = cof.new Joiner("Dopey", sleepy), doc = cof.new Joiner("Doc", grumpy);
			grumpy.interrupt();
		}
	}

	/**
	 * 创建有响应的用户界面
	 * 
	 * @author zhxg
	 *
	 */
	class UnresponsiveUI {
		private volatile double d = 1;

		public UnresponsiveUI() throws IOException {
			while (d > 0)
				d = d + (Math.PI + Math.E) / d;
			System.in.read(); // never gets here
		}
	}

	static class ResponsiveUI extends Thread {
		private static volatile double d = 1;

		public ResponsiveUI() {
			setDaemon(true);
			start();
		}

		public void run() {
			while (true) {
				d = d + (Math.PI + Math.E) / d;
			}
		}

		public static void main() throws IOException {
			// new ConcurrentOtherFeature().new UnresponsiveUI(); // must kill this process
			new ResponsiveUI();
			System.in.read();
			log.debug("d: {}", d); // shows progress
		}
	}

	/**
	 * 捕获异常 不能捕获从线程中逃逸的异常，一旦异常逃出任务的run()方法，它就会向外传播到控制台
	 * 
	 * @author zhxg
	 *
	 */
	static class ExceptionThread implements Runnable {
		public void run() {
			throw new RuntimeException();
		}

		public static void main() {
			// 使用try不能捕获到exec中线程抛出的异常
			try {
				ExecutorService exec = Executors.newCachedThreadPool();
				exec.execute(new ExceptionThread());
			} catch (Exception e) {
				log.debug("Exception has been handle!");
			}
		}
	}

	/**
	 * 使用线程池设置线程池工厂， 通过设置新生成的线程的UncaughtExceptionHandler解决从线程中抛出的异常
	 * 
	 * @author zhxg
	 *
	 */
	class ExceptionThread2 implements Runnable {
		public void run() {
			Thread t = Thread.currentThread();
			log.debug("run() by " + t);
			log.debug("eh = " + t.getUncaughtExceptionHandler());
			throw new RuntimeException();
		}
	}

	class MyUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
		public void uncaughtException(Thread t, Throwable e) {
			log.debug("caught ", e);
		}
	}

	class HandleThreadFactory implements ThreadFactory {
		public Thread newThread(Runnable r) {
			log.debug(this + " creating new Thread");
			Thread t = new Thread(r);
			log.debug("created {}", t);
			t.setUncaughtExceptionHandler(new MyUncaughtExceptionHandler());
			log.debug("eh = ", t.getUncaughtExceptionHandler());
			return t;
		}
	}

	static class CaptureUncaughtException {
		public static void main() {
			// 设置自定义的异常处理器
			// ExecutorService exec = Executors.newCachedThreadPool(new
			// ConcurrentOtherFeature().new HandleThreadFactory());
			// exec.execute(new ConcurrentOtherFeature().new ExceptionThread2());

			// 设置默认的未捕获异常处理器
			Thread.setDefaultUncaughtExceptionHandler(new ConcurrentOtherFeature().new MyUncaughtExceptionHandler());
			ExecutorService exec = Executors.newCachedThreadPool();
			exec.execute(new ExceptionThread());
		}
	}

	/**
	 * 终止任务 装饰性花园
	 */
	class Count {
		private int count = 0;
		private Random rand = new Random(47);

		// remove the synchronized keyword to see counting fail
		public synchronized int increment() {
			int temp = count;
			if (rand.nextBoolean())
				Thread.yield();
			return (count = ++temp);
		}

		public synchronized int value() {
			return count;
		}
	}

	static class Entrance implements Runnable {
		private static Count count = cof.new Count();
		private static List<Entrance> entrances = new ArrayList<Entrance>();
		private int number = 0;
		// doesn't need synchronization to read
		private final int id;
		private static volatile boolean canceled = false;

		// atomic operation on a volatile field
		public static void cancel() {
			canceled = true;
		}

		public Entrance(int id) {
			this.id = id;
			// keep this task in a list. also prevents
			// garbage collection of dead tasks
			entrances.add(this);
		}

		public void run() {
			while (!canceled) {
				synchronized (this) {
					++number;
				}
				log.debug("{} Total: {}", this, count.increment());
				try {
					TimeUnit.MILLISECONDS.sleep(100);
				} catch (InterruptedException e) {
					log.debug("sleep interrupted");
				}

			}
			log.debug("Stopping {}", this);
		}

		public synchronized int getValue() {
			return number;
		}

		public String toString() {
			return "Entrance " + id + ": " + getValue();
		}

		public static int getTotalCount() {
			return count.value();
		}

		public static int sumEntrances() {
			int sum = 0;
			for (Entrance entrance : entrances)
				sum += entrance.getValue();
			return sum;
		}
	}

	static class OrnamentalGarden {
		public static void main() throws InterruptedException {
			ExecutorService exec = Executors.newCachedThreadPool();
			for (int i = 0; i < 5; i++)
				exec.execute(new Entrance(i));
			// run for a while, then stop and collect the data
			TimeUnit.SECONDS.sleep(3);
			Entrance.cancel();
			exec.shutdown();
			if (!exec.awaitTermination(250, TimeUnit.MILLISECONDS))
				log.debug("Some tasks were not terminated!");
			log.debug("Total: {}", Entrance.getTotalCount());
			log.debug("Sum of Entrances: {}", Entrance.sumEntrances());
		}
	}

	/**
	 * 可中断阻塞
	 * 
	 * @author zhxg
	 *
	 */
	class SleepBlocked implements Runnable {
		public void run() {
			try {
				TimeUnit.SECONDS.sleep(100);
			} catch (InterruptedException e) {
				log.debug("InterruptedException");
			}
			log.debug("Exiting sleepblocked.run()");
		}
	}

	/**
	 * io阻塞不可中断； 这意味着i/o具有锁住多线程程序的潜在可能
	 * 
	 * @author zhxg
	 *
	 */
	class IOBlocked implements Runnable {
		private InputStream in;

		public IOBlocked(InputStream is) {
			in = is;
		}

		public void run() {
			try {
				log.debug("Waiting for read():");
				in.read();
			} catch (IOException e) {
				if (Thread.currentThread().isInterrupted()) {
					log.debug("Interrupted from blocked i/o");
				} else {
					throw new RuntimeException(e);
				}
			}
			log.debug("Exiting IOBlocked.run()");
		}
	}

	/**
	 * 在锁上的等待阻塞不可中断
	 * 
	 * @author zhxg
	 *
	 */
	class SynchronizedBlocked implements Runnable {
		public synchronized void f() {
			while (true) { // never releases lock
				Thread.yield();
			}
		}

		public SynchronizedBlocked() {
			new Thread() {
				public void run() {
					f(); // lock acquired by this thread
				}
			}.start();
		}

		public void run() {
			log.debug("Trying to call f()");
			f();
			log.debug("Exiting synchronizedBlocked.run()");
		}
	}

	static class Interrupting {
		private static ExecutorService exec = Executors.newCachedThreadPool();

		static void test(Runnable r) throws InterruptedException {
			Future<?> f = exec.submit(r);
			TimeUnit.MILLISECONDS.sleep(100);
			log.debug("Interrupting {}", r.getClass().getName());
			f.cancel(true); // interrupts if running
			log.debug("Interrupt sent to {}", r.getClass().getName());
		}

		public static void main() throws InterruptedException {
			test(cof.new SleepBlocked());
			test(cof.new IOBlocked(System.in));
			test(cof.new SynchronizedBlocked());
			TimeUnit.SECONDS.sleep(3);
			log.debug("Aborting with system.exit(0)");
			System.exit(0); // ... since last 2 interrupts failed
		}
	}

	/**
	 * 通过关闭底层资源以释放锁
	 * 
	 * @author zhxg
	 *
	 */
	static class CloseResource {
		public static void main() throws IOException, InterruptedException {
			ExecutorService exec = Executors.newCachedThreadPool();
			ServerSocket server = new ServerSocket(8080);
			InputStream socketInput = new Socket("localhost", 8080).getInputStream();
			exec.execute(cof.new IOBlocked(socketInput));
			exec.execute(cof.new IOBlocked(System.in));
			TimeUnit.MILLISECONDS.sleep(100);
			log.debug("Shutting down all threads");
			exec.shutdownNow();
			TimeUnit.SECONDS.sleep(1);
			log.debug("Closing " + socketInput.getClass().getName());
			socketInput.close(); // releases blocked thread
			TimeUnit.SECONDS.sleep(1);
			log.debug("Closing {}", System.in.getClass().getName());
			System.in.close();
		}
	}

	/**
	 * 被阻塞的nio通道会自动地响应中断
	 * 
	 * @author zhxg
	 *
	 */
	class NIOBlocked implements Runnable {
		private final SocketChannel sc;

		public NIOBlocked(SocketChannel sc) {
			this.sc = sc;
		}

		public void run() {
			log.debug("Waiting for read() in {}", this);
			try {
				sc.read(ByteBuffer.allocate(1));
			} catch (ClosedByInterruptException e) {
				log.debug("ClosedByInterruptException");
			} catch (AsynchronousCloseException e) {
				log.debug("AsynchronizeCloseException");
			} catch (IOException e) {
				log.debug("IOException");
			}
		}
	}

	static class NIOInterruption {
		public static void main() throws IOException, InterruptedException {
			ExecutorService exec = Executors.newCachedThreadPool();
			ServerSocket server = new ServerSocket(8080);
			InetSocketAddress isa = new InetSocketAddress("localhost", 8080);
			SocketChannel sc1 = SocketChannel.open(isa);
			SocketChannel sc2 = SocketChannel.open(isa);
			Future<?> f = exec.submit(cof.new NIOBlocked(sc1));
			exec.execute(cof.new NIOBlocked(sc2));
			exec.shutdown();
			TimeUnit.SECONDS.sleep(1);
			// produce an interrupt via cancel;
			f.cancel(true);
			TimeUnit.SECONDS.sleep(1);
			// release the block by closing the channel
			sc2.close();
		}
	}

	/**
	 * 可重入锁
	 * 
	 * @author zhxg
	 *
	 */
	static class MultiLock {
		public synchronized void f1(int count) {
			if (count-- > 0) {
				log.debug("f1() calling f2() with count {}", count);
				f2(count);
			}
		}

		public synchronized void f2(int count) {
			if (count-- > 0) {
				log.debug("f2 calling f1 whit count {}", count);
				f1(count);
			}
		}

		public static void main() {
			final MultiLock multiLock = new MultiLock();
			new Thread() {
				public void run() {
					multiLock.f1(10);
				}
			}.start();
		}
	}

	/**
	 * lock上的阻塞可以被中断 这与在synchronized方法或临界区上阻塞的任务完全不同；
	 * 在线程上调用interrupt()时，中断发生的唯一时刻是在任务要进入到阻塞操作中， 或者已经在阻塞操作内部时
	 * 
	 * @author zhxg
	 *
	 */
	class BlockedMutex {
		private Lock lock = new ReentrantLock();

		public BlockedMutex() {
			// acquire it right away, to demonstrate interruption
			// of a task blocked on a reentrantlock
			lock.lock();
		}

		public void f() {
			try {
				// this will never be available to a second task
				lock.lockInterruptibly(); // special call
				log.debug("lock acquired in f");
			} catch (InterruptedException e) {
				log.debug("Interrupted from lock acquisition in f()");
			}
		}
	}

	class Blocked2 implements Runnable {
		BlockedMutex blocked = new BlockedMutex();

		public void run() {
			log.debug("Waiting for f in BlockedMutex");
			blocked.f();
			log.debug("Broken out of blocked call");
		}
	}

	public static class Interrupting2 {
		public static void main() throws InterruptedException {
			Thread t = new Thread(cof.new Blocked2());
			t.start();
			TimeUnit.SECONDS.sleep(1);
			log.debug("Issuing t.interrupt");
			t.interrupt();
		}
	}

	/**
	 * 检查中断
	 * 
	 * @author zhxg
	 *
	 */
	class NeedsCleanup {
		private final int id;

		public NeedsCleanup(int ident) {
			id = ident;
			log.debug("NeedsCleanup {}", id);
		}

		public void cleanup() {
			log.debug("cleaning up {}", id);
		}
	}

	class Blocked3 implements Runnable {
		private volatile double d = 0.0;

		public void run() {
			try {
				while (!Thread.interrupted()) {
					// point1
					NeedsCleanup n1 = new NeedsCleanup(1);
					try {
						log.debug("sleeping");
						TimeUnit.SECONDS.sleep(1);
						// point2
						NeedsCleanup n2 = new NeedsCleanup(2);
						try {
							log.debug("Calculating");
							for (int i = 1; i < 2500000; i++)
								d = d + (Math.PI + Math.E) / d;
							log.debug("Finished time-consuming operation");
						} finally {
							n2.cleanup();
						}
					} finally {
						n1.cleanup();
					}
				}
				log.debug("Exiting via while() test");
			} catch (InterruptedException e) {
				log.debug("Exiting via InterruptedException");
			}
		}
	}
	public static class InterruptingIdiom {
		public static void main() throws InterruptedException {
			Thread t = new Thread(cof.new Blocked3());
			t.start();
			TimeUnit.MILLISECONDS.sleep(new Integer(1005));
			t.interrupt();
		}
	}
}
