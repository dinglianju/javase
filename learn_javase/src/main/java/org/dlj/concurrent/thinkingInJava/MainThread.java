package org.dlj.concurrent.thinkingInJava;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MainThread {

	public static void main(String[] args) throws InterruptedException {
		// LiftOff launch = new LiftOff();
		// launch.run();

		// BasicThreads.main();

		// MoreBasicThreads.main();
		// 使用executor运行任务
		// CachedThreadPool.main();

		// FixedThreadPool.main();

		// SingleThreadExecutor.main();

		// CallableDemo.main();

		// SleepingTask.main();

		// SimplePriorities.main();

		// SimpleDaemons.main();

		// DaemonFromFactory.main();

		// Daemons.main();

		// ADaemon.main();

		// SimpleThread.main();

		SelfManaged.main();
	}
}

class LiftOff implements Runnable {
	protected int countDown = 10; // default
	private static int taskCount = 0;
	private final int id = taskCount++;

	public LiftOff() {
	}

	public LiftOff(int countDown) {
		this.countDown = countDown;
	}

	public String status() {
		return "#" + id + "(" + (countDown > 0 ? countDown : "Liftoff!") + "), ";
	}

	public void run() {
		while (countDown-- > 0) {
			System.out.println(status());
			Thread.yield();
		}
	}
}

class BasicThreads {
	public static void main() {
		Thread t = new Thread(new LiftOff());
		t.start();
		System.out.println("waiting for liftOff");
	}
}

class MoreBasicThreads {
	public static void main() {
		for (int i = 0; i < 5; i++) {
			new Thread(new LiftOff()).start();
		}
		System.out.println("Waiting for liftOff");
	}
}

/**
 * 线程池
 * 
 * @author zhxg
 *
 */
class CachedThreadPool {
	public static void main() {
		ExecutorService exec = Executors.newCachedThreadPool();
		for (int i = 0; i < 5; i++) {
			exec.execute(new LiftOff());
		}
		exec.shutdown();
	}
}

class FixedThreadPool {
	public static void main() {
		ExecutorService exec = Executors.newFixedThreadPool(2);
		for (int i = 0; i < 5; i++) {
			exec.execute(new LiftOff());
		}
		exec.shutdown();
	}
}

class SingleThreadExecutor {
	public static void main() {
		ExecutorService exec = Executors.newSingleThreadExecutor();
		for (int i = 0; i < 5; i++) {
			exec.execute(new LiftOff());
		}
		exec.shutdown();
	}
}

/**
 * 调用线程池执行任务，获取任务执行完成后的返回值
 * 
 * @author zhxg
 *
 */
class TaskWithResult implements Callable<String> {
	private int id;

	public TaskWithResult(int id) {
		this.id = id;
	}

	// public String call() throws InterruptedException {
	public String call() {
		/*
		 * 对sleep()调用可以抛出InterruptedException异常，它在run()
		 * 中被捕获。因为异常不能跨线程传播回main(),所以必须在本地处理所有在任务内部产生的异常， 不能再call() 上声明throws
		 * InterruptedException
		 */
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return "result of TaskWithResult " + id;

	}
}

class CallableDemo {
	public static void main() {
		ExecutorService exec = Executors.newCachedThreadPool();
		ArrayList<Future<String>> results = new ArrayList<Future<String>>();
		for (int i = 0; i < 10; i++) {
			results.add(exec.submit(new TaskWithResult(i)));
		}
		for (Future<String> fs : results) {
			// get() blocks until completion:
			try {
				while (!fs.isDone()) {
					System.out.println("sleep 100ms");
					Thread.sleep(100);
				}
				System.out.println(fs.get());
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			} catch (ExecutionException e) {
				e.printStackTrace();
			} finally {
				exec.shutdown();
			}
		}
	}

}

/**
 * 休眠
 * 
 * @author zhxg
 *
 */
class SleepingTask extends LiftOff {
	public void run() {
		while (countDown-- > 0) {
			System.out.println(status());
			// old-style:
			// Thread.sleep(100);
			// java se5/6-style:
			try {
				TimeUnit.MILLISECONDS.sleep(100);
			} catch (InterruptedException e) {
				System.err.println("Interrupted");
			}

		}
	}

	public static void main() {
		ExecutorService exec = Executors.newCachedThreadPool();
		for (int i = 0; i < 5; i++) {
			exec.execute(new SleepingTask());
		}
		exec.shutdown();
	}
}

/**
 * 优先级
 * 
 * @author zhxg
 *
 */
class SimplePriorities implements Runnable {
	private int countDown = 5;
	/*
	 * volatile以努力确保d在进行浮点运算时不进行任何编译器优化
	 */
	private volatile double d; // no optimization
	private int priority;

	public SimplePriorities(int priority) {
		this.priority = priority;
	}

	public String toString() {
		return Thread.currentThread() + ": " + countDown;
	}

	public void run() {
		/*
		 * 优先级是在run()的开头部分设定，在构造器中设置它们不会有任何好处， 因为Executor在此刻还没有开始执行
		 */

		// Thread.currentThread().setPriority(priority);
		Thread.currentThread().setPriority(priority);
		while (true) {
			// an expensive 浮点运算. interruptable operation:
			for (int i = 1; i < 10000000; i++) {
				d += (Math.PI + Math.E) / (double) i;
				/*
				 * 调用yield()时，给线程调度器一个暗示，建议具有相同优先级的其他线程可以运行
				 */
				// if (i % 1000 == 0)
				// Thread.yield();
			}
			System.out.println(this);
			if (--countDown == 0)
				return;
		}
	}

	public static void main() {
		ExecutorService exec = Executors.newCachedThreadPool();
		for (int i = 0; i < 5; i++)
			exec.execute(new SimplePriorities(Thread.MIN_PRIORITY));
		exec.execute(new SimplePriorities(Thread.MAX_PRIORITY));
		exec.shutdown();
	}
}

/**
 * 后台线程 指在程序运行的时候在后台提供一种通用服务的线程，不属于程序中不可或缺的部分。
 * 所有非后台线程结束时，程序也就终止了，同时杀死进程中所有的后台线程。 只要有任何非后台线程还在运行，程序就不会终止
 * 
 * @author zhxg
 *
 */
class SimpleDaemons implements Runnable {
	public void run() {
		try {
			while (true) {
				TimeUnit.MILLISECONDS.sleep(100); // 调整sleep的时间可以观察后台线程行为
				System.out.println(Thread.currentThread() + " " + this);
			}
		} catch (InterruptedException e) {
			System.out.println("sleep() interrupted");
		}
	}

	public static void main() throws InterruptedException {
		for (int i = 0; i < 10; i++) {
			Thread daemon = new Thread(new SimpleDaemons());
			daemon.setDaemon(true); // Must call before start()
			daemon.start();
			// daemon.setDaemon(true);

		}
		System.out.println("all daemons started");
		TimeUnit.MILLISECONDS.sleep(175);
	}
}

/**
 * 使用ThreadFactory 实现使用线程池产生后台线程
 * 
 * @author zhxg
 *
 */
class DaemonThreadFactory implements ThreadFactory {
	public Thread newThread(Runnable r) {
		Thread t = new Thread(r);
		t.setDaemon(true);
		return t;
	}
}

class DaemonFromFactory implements Runnable {
	public void run() {
		try {
			while (true) {
				TimeUnit.MILLISECONDS.sleep(100);
				System.out.println(Thread.currentThread() + " " + this);
			}
		} catch (InterruptedException e) {
			System.out.println("Interrupted");
		}
	}

	public static void main() throws InterruptedException {
		ExecutorService exec = Executors.newCachedThreadPool(new DaemonThreadFactory());
		for (int i = 0; i < 10; i++) {
			exec.execute(new DaemonFromFactory());
		}
		System.out.println("All Daemon started");
		TimeUnit.MILLISECONDS.sleep(500); // run for a while
	}

}

class DaemonThreadPoolExecutor extends ThreadPoolExecutor {
	public DaemonThreadPoolExecutor() {
		super(0, Integer.MAX_VALUE, 60l, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new DaemonThreadFactory());
	}
}

/**
 * 后台线程创建的任何线程都被自动设置为后台线程
 * 
 * @author zhxg
 *
 */
class Daemon implements Runnable {
	private Thread[] t = new Thread[10];

	public void run() {
		for (int i = 0; i < t.length; i++) {
			t[i] = new Thread(new DaemonSpawn());
			t[i].start();
			System.out.println("DaemonSpawn " + i + " started, ");
		}
		for (int i = 0; i < t.length; i++) {
			System.out.println("t[" + i + "].isDaemon() = " + t[i].isDaemon() + ", ");
		}
		while (true)
			Thread.yield();
	}
}

class DaemonSpawn implements Runnable {
	public void run() {
		while (true) {
			Thread.yield();
		}
	}
}

class Daemons {
	public static void main() throws InterruptedException {
		Thread d = new Thread(new Daemon());
		d.setDaemon(true);
		d.start();
		System.out.println("d.isDaemon() = " + d.isDaemon() + ", ");
		// Allow the daemon threads to finish their startup processes:
		TimeUnit.SECONDS.sleep(1);
	}
}

/**
 * 后台进程在不执行finally子句的情况下就会终止其run()方法,
 * 因为最后一个非后台线程终止时，后台线程就会‘突然’终止，jvm就会立即关闭所有后台线程； 所以不能优雅的方式关闭后台线程
 * 
 * @author zhxg
 *
 */
class ADaemon implements Runnable {
	public void run() {
		try {
			System.out.println("Starting ADaemon");
			TimeUnit.SECONDS.sleep(2);
		} catch (InterruptedException e) {
			System.out.println("Exiting via InterruptedException");
		} finally {
			System.out.println("This should always run?");
		}
	}

	public static void main() throws InterruptedException {
		Thread t = new Thread(new ADaemon());
		// t.setDaemon(true);
		t.setDaemon(true);
		t.start();
		TimeUnit.SECONDS.sleep(1);
	}
}

/**
 * 直接从Thread继承方式实现线程
 * 
 * @author zhxg
 *
 */
class SimpleThread extends Thread {
	private int countDown = 5;
	private static int threadCount = 0;

	public SimpleThread() {
		// store the thread name
		super(Integer.toString(++threadCount));
		start();
	}

	public String toString() {
		return "#" + getName() + "(" + countDown + "), ";
	}

	public void run() {
		while (true) {
			System.out.println(this);
			if (--countDown == 0)
				return;
		}
	}

	public static void main() {
		for (int i = 0; i < 5; i++)
			new SimpleThread();
	}
}

/**
 * 自管理runnable 在构造器中启动线程可能会变得很有问题，因为另一个任务可能会在构造器结束之前 开始执行，这意味着该任务能够访问处于不稳定状态的对象
 * 
 * @author zhxg
 *
 */
class SelfManaged implements Runnable {
	private int countDown = 5;
	private Thread t = new Thread(this);

	public SelfManaged() {
		t.start();
	}

	public String toString() {
		return Thread.currentThread().getName() + "(" + countDown + "), ";
	}

	public void run() {
		while (true) {
			System.out.println(this);
			if (--countDown == 0)
				return;
		}
	}

	public static void main() {
		for (int i = 0; i < 5; i++) {
			new SelfManaged();
		}
	}
}

/**
 * 将线程代码隐藏在内部类中的几种常用实现方式
 * 
 * @author zhxg
 *
 */
// using a named inner class
class InnerThread1 {
	private int countDown = 5;
	private Inner inner;

	private class Inner extends Thread {
		Inner(String name) {
			super(name);
			start();
		}

		public void run() {
			try {
				while (true) {
					System.out.println(this);
					if (--countDown == 0)
						return;
					sleep(10);
				}
			} catch (InterruptedException e) {
				System.out.println("interrupted");
			}
		}

		public String toString() {
			return getName() + ": " + countDown;
		}
	}

	public InnerThread1(String name) {
		inner = new Inner(name);
	}
}

// using an anonymous inner class
class InnerThread2 {
	private int countDown = 5;
	private Thread t;

	public InnerThread2(String name) {
		t = new Thread(name) {
			public void run() {
				while (true) {
					System.out.println(this);
					if (--countDown == 0)
						return;
					try {
						sleep(10);
					} catch (InterruptedException e) {
						System.out.println("sleep() interrupted");
					}
				}
			}

			public String toString() {
				return getName() + ": " + countDown;
			}
		};
		t.start();
	}
}

// using a named runnable implementation
class InnerRunnable1 {
	private int countDown = 5;
	private Inner inner;

	private class Inner implements Runnable {
		Thread t;

		Inner(String name) {
			t = new Thread(this, name);
			t.start();
		}

		public void run() {
			try {
				while (true) {
					System.out.println(this);
					if (--countDown == 0)
						return;
					TimeUnit.MILLISECONDS.sleep(10);
				}
			} catch (InterruptedException e) {
				System.out.println("sleep() interrupted");
			}
		}

		public String toString() {
			return t.getName() + ": " + countDown;
		}
	}

	public InnerRunnable1(String name) {
		inner = new Inner(name);
	}
}

// Using an anonymous runnable implementation
class InnerRunnable2 {
	private int countDown = 5;
	private Thread t;

	public InnerRunnable2(String name) {
		t = new Thread(new Runnable() {
			public void run() {
				try {
					while (true) {
						System.out.println(this);
						if (--countDown == 0)
							return;
						TimeUnit.MILLISECONDS.sleep(10);
					}
				} catch (InterruptedException e) {
					System.out.println("sleep() interrupted");
				}
			}

			public String toString() {
				return Thread.currentThread().getName() + ": " + countDown;
			}
		}, name);
		t.start();
	}
}

// a separate method to run some code as a task
class ThreadMethod {
	private int countDown = 5;
	private Thread t;
	private String name;

	public ThreadMethod(String name) {
		this.name = name;
	}

	public void runTask() {
		if (t == null) {
			t = new Thread(name) {
				public void run() {
					try {
						while (true) {
							System.out.println(this);
							if (--countDown == 0)
								return;
							sleep(10);
						}
					} catch (InterruptedException e) {
						System.out.println("sleep() interrupted");
					}
				}

				public String toString() {
					return getName() + ": " + countDown;
				}
			};
			t.start();
		}
	}

}

class ThreadVariations {
	public static void main(String[] args) {
		new InnerThread1("innerThread1");
		new InnerThread2("innerThread2");
		new InnerRunnable1("InnerRunable1");
		new InnerRunnable2("InnerRunable2");
		new ThreadMethod("ThreadMethod").runTask();
	}
}