package org.dlj.concurrent.thinkingInJava;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class ConcurrentNewTools {

	public static void main(String[] args) throws InterruptedException {
//		CountDownLatchDemo.main();
		
//		HorseRace.main();
		
//		DelayQueueDemo.main();
		
//		PriorityBlockingQueueDemo.main();
		
//		GreenhouseScheduler.main();
		
//		SemaphoreDemo.main();
		
//		ExchangerDemo.main();
		
		ExchangerDemo.main();
	}
}

/**
 * CountDownLatch
 * 同步一个或多个任务，强制它们等待有其他任务执行的一组操作完成
 * @author zhxg
 *
 */
class CountDownLatchDemo {
	static final int SIZE = 100;
	static class TaskPortion implements Runnable {
		private static int counter = 0;
		private final int id = counter++;
		private static Random rand = new Random(47);
		private final CountDownLatch latch;
		TaskPortion(CountDownLatch latch) {
			this.latch = latch;
		}
		public void run() {
			try {
				doWork();
				latch.countDown();
			} catch (InterruptedException e) {
				System.out.println("Interrupted...");
			}
		}
		public void doWork() throws InterruptedException {
			TimeUnit.MILLISECONDS.sleep(rand.nextInt(2000));
			System.out.println(this + "completed");
		}
		public String toString() {
			return String.format("%1$-3d ", id);
		}
	}
	// waits on the CountDownLatch
	static class WaitingTask implements Runnable {
		private static int counter = 0;
		private final int id = counter++;
		private final CountDownLatch latch;
		WaitingTask(CountDownLatch latch) {
			this.latch = latch;
		}
		public void run() {
			try {
				latch.await();
				System.out.println("latch barrier passed for " + this);
			} catch (InterruptedException ex) {
				System.out.println(this + " interrupted");
			}
		}
		public String toString() {
			return String.format("WaitingTask %1$-3d ", id);
		}
	}
	
	public static void main() {
		ExecutorService exec = Executors.newCachedThreadPool();
		// All must share a single countdownlatch object
		CountDownLatch latch = new CountDownLatch(SIZE);
		for (int i = 0; i < 10; i++)
			exec.execute(new WaitingTask(latch));
		for (int i = 0; i < SIZE; i++)
			exec.execute(new TaskPortion(latch));
		System.out.println("launched all tasks");
		exec.shutdown(); // quit when all tasks complete
	}
}

/**
 * CyclicBarrier
 * 创建一组任务，它们并行地执行工作，然后在进行下一个步骤之前等待，直到所有的任务都完成
 * CyclicBarrier提供一个‘栅栏动作’,它是一个Runnable，当计数值到达0时自动执行，
 * 一旦所有的任务都越过了栅栏，它就会自动地为下一回合比赛做好准备
 * 
 * @author zhxg
 *
 */
class HorseRace {
	static class Horse implements Runnable {
		private static int counter = 0;
		private final int id = counter++;
		private int strides = 0;
		private static Random rand = new Random(47);
		private static CyclicBarrier barrier;
		public Horse(CyclicBarrier b) {barrier = b; }
		public synchronized int getStrides() {return strides; }
		public void run() {
			try {
				while (!Thread.interrupted()) {
					synchronized(this) {
						strides += rand.nextInt(3); // produces 0, 1 or 2
					}
					barrier.await();
				}
			} catch (InterruptedException e) {
				System.out.println("A legitimate way to exit");
			} catch (BrokenBarrierException e) {
				System.out.println("This one we want to know about");
				throw new RuntimeException(e);
			}
		}
		public String toString() {return "Horse " + id + " "; }
		public String tracks() {
			StringBuilder s = new StringBuilder();
			for (int i = 0; i < getStrides(); i++)
				s.append("*");
			s.append(id);
			return s.toString();
		}
	}
	
	static final int FINISH_LINE = 75;
	private List<Horse> horses = new ArrayList<Horse>();
	private ExecutorService exec = Executors.newCachedThreadPool();
	private CyclicBarrier barrier;
	public HorseRace(int nHorses, final int pause) {
		barrier = new CyclicBarrier(nHorses, new Runnable() {
			public void run() {
				StringBuilder s = new StringBuilder();
				for (int i = 0; i < FINISH_LINE; i++)
					s.append("="); // The fence on the racetrace
				System.out.println(s);
				for (Horse horse : horses)
					System.out.println(horse.tracks());
				for (Horse horse : horses)
					if (horse.getStrides() >= FINISH_LINE) {
						System.out.println(horse + "won!");
						exec.shutdownNow();
						return;
					}
				try {
					TimeUnit.MILLISECONDS.sleep(pause);
				} catch (InterruptedException e) {
					System.out.println("barrier-action sleep interrupted");
				}
			}
		});
		for (int i = 0; i < nHorses; i++) {
			Horse horse = new Horse(barrier);
			horses.add(horse);
			exec.execute(horse);
		}
	}
	public static void main() {
		int nHorses = 7;
		int pause = 200;
		new HorseRace(nHorses, pause);
	}
}


/**
 * delayQueue 无界的BlockingQueue
 * 用于放置实现了Delayed接口的对象，其中的对象只能在其到期时才能从队列中取走。
 * 这种队列是有序的，队头对象的延迟到期时间最长
 * @author zhxg
 *
 */
class DelayQueueDemo {
	static class DelayedTask implements Runnable, Delayed {
		private static int counter = 0;
		private final int id = counter++;
		private final int delta;
		private final long trigger;
		protected static List<DelayedTask> sequence = new ArrayList<DelayedTask>();
		public DelayedTask(int delayInMilliseconds) {
			delta = delayInMilliseconds;
			trigger = System.nanoTime() + TimeUnit.NANOSECONDS.convert(delta, TimeUnit.MILLISECONDS);
			sequence.add(this);
		}
		public long getDelay(TimeUnit unit) {
			return unit.convert(trigger - System.nanoTime(), TimeUnit.NANOSECONDS);
		}
		public int compareTo(Delayed arg) {
			DelayedTask that = (DelayedTask)arg;
			if (trigger < that.trigger) return -1;
			if (trigger > that.trigger) return 1;
			return 0;
		}
		public void run() { System.out.println(this + " "); }
		public String toString() {
			return String.format("[%1$-4d]", delta) + " Task " + id;
		}
		public String summary() {
			return "(" + id + ":" + delta + ")";
		}
		
		public static class EndSentinel extends DelayedTask {
			private ExecutorService exec;
			public EndSentinel(int delay, ExecutorService e) {
				super(delay);
				exec = e;
			}
			public void run() {
				for (DelayedTask pt : sequence) {
					System.out.println(pt.summary() + " ");
				}
				System.out.println(this + " Calling shutdownNow()");
				exec.shutdownNow();
			}
		}
	}
	
	static class DelayedTaskConsumer implements Runnable {
		private DelayQueue<DelayedTask> q;
		public DelayedTaskConsumer(DelayQueue<DelayedTask> q) {
			this.q = q;
		}
		public void run() {
			try {
				while (!Thread.interrupted()) {
					q.take().run(); // run task with the current thread
				}
			} catch (InterruptedException e) {
				System.out.println("Acceptable way to exit");
			}
			System.out.println("Finished DelayedTaskConsumer");
		}
	}
	
	public static void main() {
		Random rand = new Random(47);
		ExecutorService exec = Executors.newCachedThreadPool();
		DelayQueue<DelayedTask> queue = new DelayQueue<DelayedTask>();
		// Fill with tasks that have random delays
		for (int i = 0; i < 20; i++)
			queue.put(new DelayedTask(rand.nextInt(5000)));
		// set the stopping point
		queue.add(new DelayedTask.EndSentinel(5000, exec));
		exec.execute(new DelayedTaskConsumer(queue));
	}
}

/**
 * PriorityBlockingQueue优先级队列
 * 具有可阻塞的读取操作
 * @author zhxg
 *
 */
class PriorityBlockingQueueDemo {
	static class PrioritizedTask implements Runnable, Comparable<PrioritizedTask> {
		private Random rand = new Random(47);
		private static int counter = 0;
		private final int id = counter++;
		private final int priority;
		protected static List<PrioritizedTask> sequence = new ArrayList<PrioritizedTask>();
		public PrioritizedTask(int priority) {
			this.priority = priority;
			sequence.add(this);
		}
		public int compareTo(PrioritizedTask arg) {
			return priority < arg.priority ? 1 : (priority > arg.priority ? -1 : 0);
		}
		public void run() {
			try {
				TimeUnit.MILLISECONDS.sleep(rand.nextInt(250));
			} catch (InterruptedException e) {
				System.out.println("Acceptable way to exit");
			}
			System.out.println(this);
		}
		public String toString() {
			return String.format("[%1$-3d]", priority) + " Task " + id;
		}
		public String summary() {
			return "(" + id + ":" + priority + ")";
		}
		public static class EndSentinel extends PrioritizedTask {
			private ExecutorService exec;
			public EndSentinel(ExecutorService e) {
				super(-1); // Lowest priority in this program
				exec = e;
			}
			public void run() {
				int count = 0;
				for (PrioritizedTask pt : sequence) {
					System.out.println(pt.summary());
					if (++count % 5 == 0)
						System.out.println();
				}
				System.out.println(this + "Calling shutdownNow()");
				exec.shutdownNow();
			}
		}
	}
	static class PrioritizedTaskProducer implements Runnable {
		private Random rand = new Random(47);
		private Queue<Runnable> queue;
		private ExecutorService exec;
		public PrioritizedTaskProducer(Queue<Runnable> q, ExecutorService e) {
			queue = q;
			exec = e; // used for EndSentinel
		}
		public void run() {
			// Unbounded queue; never blocks
			// fill it up fast with random priorities;
			for (int i = 0; i < 20; i++) {
				queue.add(new PrioritizedTask(rand.nextInt(10)));
				Thread.yield();
			}
			// Trickle in highest-priority jobs
			try {
				for (int i = 0; i < 10; i++) {
					TimeUnit.MILLISECONDS.sleep(250);
					queue.add(new PrioritizedTask(10));
				}
				// Add jobs, lowest priority first
				for (int i = 0; i < 10; i++) {
					queue.add(new PrioritizedTask(i));
				}
				// A sentinel to stop all the tasks
				queue.add(new PrioritizedTask.EndSentinel(exec));
			} catch (InterruptedException e) {
				System.out.println("Acceptable way to exit");
			}
			System.out.println("Finished prioritizedTaskProducer");
		}
	}
	static class PrioritizedTaskConsumer implements Runnable {
		private PriorityBlockingQueue<Runnable> q;
		public PrioritizedTaskConsumer(PriorityBlockingQueue<Runnable> q) {
			this.q = q;
		}
		public void run() {
			try {
				while (!Thread.interrupted()) {
					// use current thread to run the task
					q.take().run();
				}
			} catch (InterruptedException e) {
				System.out.println("Acceptable way to exit");
			}
			System.out.println("Finished PrioritizedTaskConsumer");
		}
	}
	
	public static void main() {
		Random rand = new Random(47);
		ExecutorService exec = Executors.newCachedThreadPool();
		PriorityBlockingQueue<Runnable> queue = new PriorityBlockingQueue<Runnable>();
		exec.execute(new PrioritizedTaskProducer(queue, exec));
		exec.execute(new PrioritizedTaskConsumer(queue));
	}
}

/**
 * ScheduledExecutor的温室控制器
 * @author zhxg
 *
 */
class GreenhouseScheduler {
	private volatile boolean light = false;
	private volatile boolean water = false;
	private String thermostat = "Day";
	public synchronized String getThermostat() {
		return thermostat;
	}
	public synchronized void setThermostat(String value) {
		thermostat = value;
	}
	ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(10);
	public void schedule(Runnable event, long delay) {
		scheduler.schedule(event, delay, TimeUnit.MILLISECONDS);
	}
	public void repeat(Runnable event, long initialDelay, long period) {
		scheduler.scheduleAtFixedRate(event, initialDelay, period, TimeUnit.MILLISECONDS);
	}
	class LightOn implements Runnable {
		public void run() {
			// put hardware control code here to physically turn on the light
			System.out.println("Turning on lights");
			light = true;
		}
	}
	class LightOff implements Runnable {
		public void run() {
			// put hardware control code here to physically turn off the ligth.
			System.out.println("Turning off lights");
			light = false;
		}
	}
	class WaterOn implements Runnable {
		public void run() {
			// put hardware control code here
			System.out.println("Turning greenhouse water on");
			water = true;
		}
	}
	class WaterOff implements Runnable {
		public void run() {
			// put hardware control code here
			System.out.println("Turning greenhouse water off");
			water = false;
		}
	}
	class ThermostatNight implements Runnable {
		public void run() {
			// put hardware control code here
			System.out.println("Thermostat to night setting");
			setThermostat("Night");
		}
	}
	class ThermostatDay implements Runnable {
		public void run() {
			// put hardware control code here.
			System.out.println("Thermostat to day setting");
			setThermostat("Day");
		}
	}
	class Bell implements Runnable {
		public void run() {System.out.println("Bing!"); }
	}
	class Terminate implements Runnable {
		public void run() {
			System.out.println("Terminating");
			scheduler.shutdownNow();
			// Must start a separate task to do this job
			// since the scheduler has been shut down
			new Thread() {
				public void run() {
					for (DataPoint d : data) {
						System.out.println(d);
					}
				}
			}.start();
		}
	}
	// New featurn: data collection
	static class DataPoint{
		final Calendar time;
		final float temperature;
		final float humidity;
		public DataPoint(Calendar d, float temp, float hum) {
			time = d;
			temperature = temp;
			humidity = hum;
		}
		public String toString() {
			return time.getTime() + String.format(" temperature: %1$.1f humidity: %2$.2f", temperature, humidity);
		}
	}
	private Calendar lastTime = Calendar.getInstance();
	{
		// Adjust date to the half hour
		lastTime.set(Calendar.MINUTE, 30);
		lastTime.set(Calendar.SECOND, 60);
	}
	private float lastTemp = 65.0f;
	private int tempDirection = +1;
	private float lastHumidity = 50.0f;
	private int humidityDirection = +1;
	private Random rand = new Random(47);
	List<DataPoint> data = Collections.synchronizedList(new ArrayList<DataPoint>());
	class CollectData implements Runnable {
		public void run() {
			System.out.println("Collecting data");
			synchronized(GreenhouseScheduler.this) {
				// Pretend the interval is longer than it is:
				lastTime.set(Calendar.MINUTE, lastTime.get(Calendar.MINUTE) + 30);
				// One in 5 chances of reversing the direction
				if (rand.nextInt(5) == 4)
					tempDirection = -humidityDirection;
				lastHumidity = lastHumidity + humidityDirection * rand.nextFloat();
				// Calendar must be cloned, otherwise all 
				// Datapoints hold references to the same lastTime
				// For a basic object like calendar, clone() is Ok
				data.add(new DataPoint((Calendar) lastTime.clone(), lastTemp, lastHumidity));
			}
		}
	}
	public static void main() {
		GreenhouseScheduler gh = new GreenhouseScheduler();
		gh.schedule(gh.new Terminate(), 5000);
		// Former "Restart" class not necessary
		gh.repeat(gh.new Bell(), 0, 1000);
		gh.repeat(gh.new ThermostatNight(), 0, 2000);
		gh.repeat(gh.new LightOn(), 0, 200);
		gh.repeat(gh.new LightOff(), 0, 400);
		gh.repeat(gh.new WaterOn(), 0, 600);
		gh.repeat(gh.new WaterOff(), 0, 800);
		gh.repeat(gh.new ThermostatDay(), 0, 1400);
		gh.repeat(gh.new CollectData(), 500, 500);
		
	}
}

/**
 * 基于Semaphore计数信号量实现的对象池
 * @author zhxg
 *
 */
class SemaphoreDemo {
	static class Pool<T> {
		private int size;
		private List<T> items = new ArrayList<T>();
		private volatile boolean[] checkedOut;
		private Semaphore available;
		public Pool(Class<T> classObject, int size) {
			this.size = size;
			checkedOut = new boolean[size];
			available = new Semaphore(size, true);
			// load pool with objects that can be checked out
			for (int i = 0; i < size; ++i) {
				try {
					// Assumes a default constructor
					items.add(classObject.newInstance());
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
		public T checkOut() throws InterruptedException {
			available.acquire();
			return getItem();
		}
		public void checkIn(T x) {
			if (releaseItem(x))
				available.release();
		}
		private synchronized T getItem() {
			for (int i = 0; i < size; ++i) {
				if (!checkedOut[i]) {
					checkedOut[i] = true;
					return items.get(i);
				}
			}
			return null; // semaphore prevents reaching here
		}
		private synchronized boolean releaseItem(T item) {
			int index = items.indexOf(item);
			if (index == -1) return false; // not in the list
			if (checkedOut[index]) {
				checkedOut[index] = false;
				return true;
			}
			return false; // wasn't checked out
		}
	}
	static class Fat {
		private volatile double d; // prevent optimization
		private static int counter = 0;
		private final int id = counter++;
		public Fat() {
			// Expensive. interruptible operation
			for (int i = 1; i < 10000; i++) {
				d += (Math.PI + Math.E) / (double)i;
			}
		}
		public void operation() {System.out.println(this); }
		public String toString() {return "Fat id: " + id; }
	}
	static class CheckoutTask<T> implements Runnable {
		private static int counter = 0;
		private final int id = counter++;
		private Pool<T> pool;
		public CheckoutTask(Pool<T> pool) {
			this.pool = pool;
		}
		public void run() {
			try {
				T item = pool.checkOut();
				System.out.println(this + " checkout out " + item);
				TimeUnit.SECONDS.sleep(1);
				System.out.println(this + " checking in " + item);
				pool.checkIn(item);
			} catch (InterruptedException e) {
				System.out.println("Acceptable way to terminate");
			}
		}
		public String toString() {
			return "CheckoutTask " + id + " ";
		}
	}
	
	final static int SIZE = 25;
	public static void main() throws InterruptedException {
		final Pool<Fat> pool = new Pool<Fat>(Fat.class, SIZE);
		ExecutorService exec = Executors.newCachedThreadPool();
		for (int i = 0; i < SIZE; i++)
			exec.execute(new CheckoutTask<Fat>(pool));
		System.out.println("All CheckoutTasks created");
		List<Fat> list = new ArrayList<Fat>();
		for (int i = 0; i < SIZE; i++) {
			Fat f = pool.checkOut();
			System.out.println(i + ": main() thread checked out");
			f.operation();
			list.add(f);
		}
		Future<?> blocked = exec.submit(new Runnable() {
			public void run() {
				try {
					// semaphore prevents additional checkout
					// so call is blocked
					pool.checkOut();
				} catch (InterruptedException e) {
					System.out.println("Checkout() Interrupted");
				}
			}
		});
		TimeUnit.SECONDS.sleep(2);
		blocked.cancel(true); // break out of blocked call
		System.out.println("Checking in objects in " + list);
		for (Fat f : list)
			pool.checkIn(f);
		for (Fat f : list)
			pool.checkIn(f); // second checkin ignored
		exec.shutdown();
	}
}

/**
 * Exchanger 实现在两个任务之间交换对象的栅栏
 * @author zhxg
 *
 */
class ExchangerDemo {
	interface Generator<T> {
		public T next();
	}
	static class BasicGenerator<T> implements Generator<T> {
		private Class<T> type;
		public BasicGenerator(Class<T> type) {this.type = type; }
		public T next() {
			try {
				// Assumes type is a public class
				return type.newInstance();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		// produce a default generator given a type token
		public static <T> Generator<T> create(Class<T> type) {
			return new BasicGenerator<T>(type);
		}
	}


	static class ExchangerProducer<T> implements Runnable {
		private Generator<T> generator;
		private Exchanger<List<T>> exchanger;
		private List<T> holder;
		ExchangerProducer(Exchanger<List<T>> exchg, Generator<T> gen, List<T> holder) {
			exchanger =exchg;
			generator = gen;
			this.holder = holder;
		}
		public void run() {
			try {
				while (!Thread.interrupted()) {
					for (int i = 0; i < ExchangerDemo.size; i++)
						holder.add(generator.next());
					// Exchange full for empty
					holder = exchanger.exchange(holder);
				}
			} catch (InterruptedException e) {
				System.out.println("Ok to terminate this way");
			}
		}
	}
	static class ExchangerConsumer<T> implements Runnable {
		private Exchanger<List<T>> exchanger;
		private List<T> holder;
		private volatile T value;
		ExchangerConsumer(Exchanger<List<T>> ex, List<T> holder) {
			exchanger = ex;
			this.holder = holder;
		}
		public void run() {
			try {
				while (!Thread.interrupted()) {
					holder = exchanger.exchange(holder);
					for (T x : holder) {
						value = x; // fetch out value
						holder.remove(x); // ok for copyonwriteArraylist
					}
				}
			} catch (InterruptedException e) {
				System.out.println("Interrupted...");
			}
			System.out.println("Final value: " + value);
		}
	}
	
	static int size = 10;
	static int delay = 5; // seconds
	public static void main() throws InterruptedException {
		ExecutorService exec = Executors.newCachedThreadPool();
		Exchanger<List<SemaphoreDemo.Fat>> xc = new Exchanger<List<SemaphoreDemo.Fat>>();
		List<SemaphoreDemo.Fat> producerList = new CopyOnWriteArrayList<SemaphoreDemo.Fat>(),
				consumerList = new CopyOnWriteArrayList<SemaphoreDemo.Fat>();
		exec.execute(new ExchangerProducer<SemaphoreDemo.Fat>(xc, BasicGenerator.create(SemaphoreDemo.Fat.class), producerList));
		exec.execute(new ExchangerConsumer<SemaphoreDemo.Fat>(xc, consumerList));
		TimeUnit.SECONDS.sleep(delay);
		exec.shutdownNow();
	}
}