package org.dlj.concurrent.blog;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * 参考纯洁的微笑公众号文章：面试-线程池的成长之路
 * https://mp.weixin.qq.com/s/5dexEENTqJWXN_17c6Lz6A
 */
public class ThreadPool {
	public static void main(String[] args) throws InterruptedException, ExecutionException {
//		SingleThreadPoolTest.main();
		
//		FixedThreadPoolTest.main();
		
//		ScheduledThreadPoolTest.main();
		
//		RejectedExecutionTest.main();
		
//		SubmitAExecuteTest.main();
		
//		SubmitAExecuteTest.main2();
		
//		SubmitAExecuteTest.main3();
		
//		ThreadPoolShutdownTest.main();
		
//		ThreadPoolShutdownTest.main2();
		
//		ThreadPoolShutdownTest.main3();
		
		FangjiaThreadPoolExecutor.main();
	}
}
class SingleThreadPoolTest {
	public static void main() {
		ExecutorService pool = Executors.newSingleThreadExecutor();
		for (int i = 0; i < 10; i++) {
			pool.execute(() -> {
				System.out.println(Thread.currentThread().getName() + "\t 开始发车啦。。。");
			});
		}
		pool.shutdown();
	}
}
class FixedThreadPoolTest {
	public static void main() {
		ExecutorService pool = Executors.newFixedThreadPool(10);
		for (int i = 0; i< 10; i++) {
			pool.execute(() -> {
				System.out.println(Thread.currentThread().getName() + "\t 开始发车了。。。");
			});
		}
		pool.shutdown();
	}
}
class CachedThreadPoolTest {
	public static void main() {
		ExecutorService pool = Executors.newCachedThreadPool();
		for (int i = 0; i< 10; i++) {
			pool.execute(() -> {
				System.out.println(Thread.currentThread().getName() + "\t 开始发车了。。。");
			});
		}
		pool.shutdown();
	}
}
class ScheduledThreadPoolTest {
	public static void main() {
		ScheduledExecutorService pool = Executors.newScheduledThreadPool(10);
		for (int i = 0; i < 10; i++) {
			/*
			 * 延迟10执行
			 */
//			pool.schedule(() -> {
//				System.out.println(Thread.currentThread().getName() + "\t开始发车了。。。");
//			}, 10, TimeUnit.SECONDS);
			/*
			 * 每秒执行一次
			 */
			pool.scheduleAtFixedRate(() -> {
				System.out.println(Thread.currentThread().getName() + "\t开始发车了。。。");
			}, 1, 1, TimeUnit.SECONDS);
		}
		//pool.shutdown();
	}
}

/**
 * 线程池的拒绝策略
 * AbortPolicy策略：直接抛出异常，阻止系统正常工作
 * @author zhxg
 *
 */
class AbortPolicy implements RejectedExecutionHandler {
	public AbortPolicy() {}
	public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
		throw new RejectedExecutionException("Task " + r.toString() + " rejected from " + e.toString());
	}
}

/**
 * 只要线程池未关闭，该策略直接在调用者线程中，运行当前的被丢弃的任务
 * @author zhxg
 *
 */
class CallerRunsPolicy implements RejectedExecutionHandler {
	public CallerRunsPolicy() {}
	public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
		if (!e.isShutdown()) {
			r.run();
		}
	}
}

/**
 * 丢弃最老的一个请求，也就是即将被执行的任务，并尝试再次提交当前任务
 * @author zhxg
 *
 */
class DiscardOldestPolicy implements RejectedExecutionHandler {
	public DiscardOldestPolicy() {}
	public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
		if (!e.isShutdown()) {
			e.getQueue().poll();
			e.execute(r);
		}
	}
}

/**
 * 默默丢弃无法处理的任务，不予任何处理
 * @author zhxg
 *
 */
class DiscardPolicy implements RejectedExecutionHandler {
	public DiscardPolicy() {}
	public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
		
	}
}
class RejectedExecutionTest {
	public static void main() {
		BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(100);
		ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 100, 10, TimeUnit.SECONDS, workQueue, new DiscardPolicy());
		for (int i = 0; i < 10; i++) {
			executor.execute(() -> {
				System.out.println(Thread.currentThread().getName() + "\t发车了。。。");
			});
		}
	}
}

/**
 * submit和execute区别
 * @author zhxg
 *
 */
class SubmitAExecuteTest {
	public static void main() throws InterruptedException, ExecutionException {
		ExecutorService pool = Executors.newFixedThreadPool(10);
		Future<String> future = pool.submit(new Callable<String>() {
			@Override
			public String call() {
				return "Hello";
			}
		});
		String result = future.get();
		System.out.println(result);
	}
	public static void main2() throws InterruptedException, ExecutionException {
		ExecutorService pool = Executors.newFixedThreadPool(10);
		Data data = new Data();
		Future<Data> future = pool.submit(new MyRunnable(data), data);
		String result = future.get().getName();
		System.out.println(result);
		
	}
	public static void main3() throws InterruptedException, ExecutionException {
		ExecutorService pool = Executors.newFixedThreadPool(10);
		Data data = new Data();
		Future<?> future = pool.submit(new MyRunnable(data));
		Object result = future.get();
		System.out.println(result);
		
	}
	static class Data {
		String name;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		@Override
		public String toString() {
			return "Data [name=" + name + "]";
		}
		
	}
	static class MyRunnable implements Runnable {
		private Data data;
		public MyRunnable(Data data) {
			this.data = data;
		}
		@Override
		public void run() {
			data.setName("yinjihuan");
		}
	}
}

/**
 * 线程池关闭
 * @author zhxg
 *
 */
class ThreadPoolShutdownTest {
	public static void main() throws InterruptedException {
		ExecutorService pool = Executors.newFixedThreadPool(1);
		for (int i = 0; i < 5; i++) {
			System.err.println(i);
			pool.execute(() -> {
				try {
					Thread.sleep(30000);
					System.out.println("--");
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}
		Thread.sleep(1000);
		List<Runnable> runs = pool.shutdownNow();
		System.out.println(runs);
	}
	public static void main2() throws InterruptedException {
		ExecutorService pool = Executors.newFixedThreadPool(1);
		for (int i = 0; i < 5; i++) {
			System.err.println(i);
			pool.execute(() -> {
				try {
					Thread.sleep(30000);
					System.out.println("--");
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}
		Thread.sleep(1000);
		pool.shutdown();
		pool.execute(() -> {
			try {
				Thread.sleep(30000);
				System.out.println("--");
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
	
	public static void main3() throws InterruptedException {
		ExecutorService pool = Executors.newFixedThreadPool(1);
		for (int i = 0; i < 5; i++) {
			System.err.println(i);
			pool.execute(() -> {
				try {
					Thread.sleep(3000);
					System.out.println("--");
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}
		Thread.sleep(1000);
		pool.shutdown();
		while (true) {
			if (pool.isTerminated()) {
				System.out.println("所有的子线程都结束了！");
				break;
			}
			Thread.sleep(1000);
		}
	}
}

/**
 * 自定义线程池
 * 修改线程名称
 * @author zhxg
 *
 */
class FangjiaThreadPoolExecutor {
	private static ExecutorService executorService = newFixedThreadPool(50);
	private static ExecutorService newFixedThreadPool(int nThreads) {
		/*
		 * public ThreadPoolExecutor(
		 * 			int corePoolSize,  // 线程池大小
		 * 			int maximumPoolSize,	// 最大线程数
		 * 			long keepAliveTime,		// 在线程数量超过corePoolSize后，多余空闲线程的最大存活时间
		 * 			TimeUnit unit,		//时间单位
		 * 			BlockingQueue<Runnable> workQueue,	// 存放来不及处理的任务的队列，是一个BlockingQueue
		 * 			ThreadFactory threadFactory,	// 生产线程的工厂类，可以定义线程名，优先级等
		 * 			RejectedExecutionHandler handler // 拒绝策略，当任务来不及处理时该如何处理
		 * 		);
		 */
		
		return new ThreadPoolExecutor(nThreads, nThreads, 0l, TimeUnit.MILLISECONDS,
				new ArrayBlockingQueue<Runnable>(10000), new DefaultThreadFactory(), new CallerRunsPolicy());
	}
	public static void execute(Runnable command) {
		executorService.execute(command);
	}
	public static void shutdown() {
		executorService.shutdown();
	}
	static class DefaultThreadFactory implements ThreadFactory {
		private static final AtomicInteger poolNumber = new AtomicInteger(1);
		private final ThreadGroup group;
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		private final String namePrefix;
		DefaultThreadFactory() {
			SecurityManager s = System.getSecurityManager();
			group = (s != null) ? s.getThreadGroup() : 
				Thread.currentThread().getThreadGroup();
			namePrefix = "FSH-pool-" + poolNumber.getAndIncrement() + "-thread-";
		}
		public Thread newThread(Runnable r) {
			Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
			if (t.isDaemon())
				t.setDaemon(false);
			if (t.getPriority() != Thread.NORM_PRIORITY)
				t.setPriority(Thread.NORM_PRIORITY);
			return t;
		}
	}
	
	public static void main() {
		for (int i = 0; i< 10; i++) {
			executorService.execute(() -> {
				System.out.println(Thread.currentThread().getName() + "\t 开始发车了。。。");
			});
		}
	}
}