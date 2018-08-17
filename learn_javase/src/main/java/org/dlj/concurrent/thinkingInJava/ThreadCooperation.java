package org.dlj.concurrent.thinkingInJava;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadCooperation {

	public static void main(String[] args) throws InterruptedException, IOException {
		// WaxOMatic.main();

		// NotifyVsNotifyAll.main();

		// Restaurant.main();

		// WaxOMatic2.main();

		// TestBlockingQueues.main();

		// ToastOMatic.main();

//		PipedIO.main();
		
//		DeadlockingDiningPhilosophers.main();
		
		FixedDiningPhilosophers.main();
	}
}

/**
 * wait and notifyAll
 * 
 * @author zhxg
 *
 */
class WaxOMatic {
	class Car {
		private boolean waxOn = false;

		public synchronized void waxed() { // 涂蜡
			waxOn = true; // ready to buff
			notifyAll();
		}

		public synchronized void buffed() { // 抛光
			waxOn = false; // ready for another coat of wax
			notifyAll();
		}

		public synchronized void waitForWaxing() throws InterruptedException {
			while (waxOn == false)
				wait();
		}

		public synchronized void waitForBuffing() throws InterruptedException {
			while (waxOn == true)
				wait();
		}
	}

	class WaxOn implements Runnable {
		private Car car;

		public WaxOn(Car c) {
			car = c;
		}

		public void run() {
			try {
				while (!Thread.interrupted()) {
					System.out.println("Wax On! ");
					TimeUnit.MILLISECONDS.sleep(200);
					car.waxed();
					car.waitForBuffing();
				}
			} catch (InterruptedException e) {
				System.out.println("Exiting via interrupt");
			}
			System.out.println("Ending Wax on task");
		}
	}

	class WaxOff implements Runnable {
		private Car car;

		public WaxOff(Car c) {
			car = c;
		}

		public void run() {
			try {
				while (!Thread.interrupted()) {
					car.waitForWaxing();
					System.out.println("Wax Off! ");
					TimeUnit.MILLISECONDS.sleep(200);
					car.buffed();
				}
			} catch (InterruptedException e) {
				System.out.println("Exiting via interrupt");
			}
			System.out.println("Ending Wax Off task");
		}
	}

	public static void main() throws InterruptedException {
		WaxOMatic wom = new WaxOMatic();
		Car car = wom.new Car();
		ExecutorService exec = Executors.newCachedThreadPool();
		exec.execute(wom.new WaxOff(car));
		exec.execute(wom.new WaxOn(car));
		TimeUnit.SECONDS.sleep(5);
		exec.shutdownNow(); // Interrupt all tasks
	}
}

/**
 * notify与notifyAll的区别
 * 
 * @author zhxg
 *
 */
class NotifyVsNotifyAll {
	static class Blocker {
		synchronized void waitingCall() {
			try {
				while (!Thread.interrupted()) {
					wait();
					System.out.print(Thread.currentThread() + " ");
				}
			} catch (InterruptedException e) {
				System.out.println("Catch InterruptedException");
			}
		}

		synchronized void prod() {
			notify();
		}

		synchronized void prodAll() {
			notifyAll();
		}
	}

	static class Task implements Runnable {
		static Blocker blocker = new Blocker();

		public void run() {
			blocker.waitingCall();
		}
	}

	static class Task2 implements Runnable {
		static Blocker blocker = new Blocker();

		public void run() {
			blocker.waitingCall();
		}
	}

	public static void main() throws InterruptedException {
		ExecutorService exec = Executors.newCachedThreadPool();
		for (int i = 0; i < 5; i++) {
			exec.execute(new Task());
		}
		exec.execute(new Task2());
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			boolean prod = true;

			public void run() {
				if (prod) {
					System.out.print("\nnotify() ");
					Task.blocker.prod();
					prod = false;
				} else {
					System.out.print("\nnotifyAll() ");
					Task.blocker.prodAll();
					prod = true;
				}
			}
		}, 400, 400); // run every .4 second
		TimeUnit.SECONDS.sleep(5); // run for a while...
		timer.cancel();
		System.out.println("\nTimer canceled");
		TimeUnit.MILLISECONDS.sleep(500);
		System.out.print("Task2.blocker.prodAll() ");
		Task2.blocker.prodAll();
		TimeUnit.MILLISECONDS.sleep(500);
		System.out.println("\nshutting down");
		exec.shutdownNow(); // interrupt all tasks
	}

}

/**
 * 生产者与消费者
 * 
 * @author zhxg
 *
 */
class Restaurant {
	class Meal {
		private final int orderNum;

		public Meal(int orderNum) {
			this.orderNum = orderNum;
		}

		public String toString() {
			return "Meal " + orderNum;
		}
	}

	class WaitPerson implements Runnable {
		private Restaurant restaurant;

		public WaitPerson(Restaurant r) {
			restaurant = r;
		}

		public void run() {
			try {
				while (!Thread.interrupted()) {
					synchronized (this) {
						while (restaurant.meal == null)
							wait(); // ... for the chef to produce a meal
					}
					System.out.println("Waitperson got " + restaurant.meal);
					synchronized (restaurant.chef) {
						restaurant.meal = null;
						restaurant.chef.notifyAll(); // ready for another
					}
				}
			} catch (InterruptedException e) {
				System.out.println("WaitPerson interrupted");
			}
		}
	}

	class Chef implements Runnable {
		private Restaurant restaurant;
		private int count = 0;

		public Chef(Restaurant r) {
			restaurant = r;
		}

		public void run() {
			try {
				while (!Thread.interrupted()) {
					synchronized (this) {
						while (restaurant.meal != null)
							wait(); // ... for the meal to be taken
					}
					if (++count == 10) {
						System.out.println("Out of food. closing");
						restaurant.exec.shutdownNow();
					}
					System.out.println("Order up! ");
					synchronized (restaurant.waitPerson) {
						restaurant.meal = new Meal(count);
						restaurant.waitPerson.notifyAll();
					}
					TimeUnit.MILLISECONDS.sleep(100);
				}
			} catch (InterruptedException e) {
				System.out.println("Chef interrupted");
			}
		}
	}

	Meal meal;
	ExecutorService exec = Executors.newCachedThreadPool();

	WaitPerson waitPerson = new WaitPerson(this);
	Chef chef = new Chef(this);

	public Restaurant() {
		exec.execute(chef);
		exec.execute(waitPerson);
	}

	public static void main() {
		new Restaurant();
	}
}

/**
 * 使用lock和condition实现的任务交互
 * 
 * @author zhxg
 *
 */
class WaxOMatic2 {
	class Car {
		private Lock lock = new ReentrantLock();
		private Condition condition = lock.newCondition();
		private boolean waxOn = false;

		public void waxed() {
			lock.lock();
			try {
				waxOn = true; // ready to buff
				condition.signalAll();
			} finally {
				lock.unlock();
			}
		}

		public void buffed() {
			lock.lock();
			try {
				waxOn = false; // ready for another coat of wax
				condition.signalAll();
			} finally {
				lock.unlock();
			}
		}

		public void waitForWaxing() throws InterruptedException {
			lock.lock();
			try {
				while (waxOn == false)
					condition.await();
			} finally {
				lock.unlock();
			}
		}

		public void waitForBuffing() throws InterruptedException {
			lock.lock();
			try {
				while (waxOn == true)
					condition.await();
			} finally {
				lock.unlock();
			}
		}
	}

	class WaxOn implements Runnable {
		private Car car;

		public WaxOn(Car c) {
			car = c;
		}

		public void run() {
			try {
				while (!Thread.interrupted()) {
					System.out.println("Wax On! ");
					TimeUnit.MILLISECONDS.sleep(200);
					car.waxed();
					car.waitForBuffing();
				}
			} catch (InterruptedException e) {
				System.out.println("Exiting via interrupt");
			}
			System.out.println("Ending Wax On task");
		}
	}

	class WaxOff implements Runnable {
		private Car car;

		public WaxOff(Car c) {
			car = c;
		}

		public void run() {
			try {
				while (!Thread.interrupted()) {
					car.waitForWaxing();
					System.out.println("Wax Off! ");
					TimeUnit.MILLISECONDS.sleep(200);
					car.buffed();
				}
			} catch (InterruptedException e) {
				System.out.println("Exiting via interrupt");
			}
			System.out.println("Ending Wax off task");
		}
	}

	public static void main() throws InterruptedException {
		WaxOMatic2 wom = new WaxOMatic2();
		Car car = wom.new Car();
		ExecutorService exec = Executors.newCachedThreadPool();
		exec.execute(wom.new WaxOff(car));
		exec.execute(wom.new WaxOn(car));
		TimeUnit.SECONDS.sleep(5);
		exec.shutdownNow();

	}
}

/**
 * 生产者-消费者与队列
 * 
 * @author zhxg
 *
 */
class TestBlockingQueues {
	static class LiftOffRunner implements Runnable {
		private BlockingQueue<LiftOff> rockets;

		public LiftOffRunner(BlockingQueue<LiftOff> queue) {
			rockets = queue;
		}

		public void add(LiftOff lo) {
			try {
				rockets.put(lo);
			} catch (InterruptedException e) {
				System.out.println("Interrupted during put()");
			}
		}

		public void run() {
			try {
				while (!Thread.interrupted()) {
					LiftOff rocket = rockets.take();
					rocket.run(); // use this thread
				}
			} catch (InterruptedException e) {
				System.out.println("Waking from take()");
			}
			System.out.println("Exiting LiftOffRunner");
		}
	}

	static void getkey() {
		try {
			// compensate for windows/linux difference in the
			// length of the result produced by the entry key
			new BufferedReader(new InputStreamReader(System.in)).readLine();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	static void getkey(String message) {
		System.out.println(message);
		getkey();
	}

	static void test(String msg, BlockingQueue<LiftOff> queue) {
		System.out.println(msg);
		LiftOffRunner runner = new LiftOffRunner(queue);
		Thread t = new Thread(runner);
		t.start();
		for (int i = 0; i < 5; i++) {
			runner.add(new LiftOff(5));
		}
		getkey("Press 'enter' (" + msg + ")");
		t.interrupt();
		System.out.println("Finished " + msg + " test");
	}

	public static void main() {
		test("LinkedBlockingQueue", new LinkedBlockingQueue<LiftOff>()); // Unlimited size
		test("ArrayBlockingQueue", new ArrayBlockingQueue<LiftOff>(3)); // fixed size
		test("SynchronousQueue", new SynchronousQueue<LiftOff>()); // size of 1
	}
}

/**
 * 吐司BlockingQueue 使用队列实现生产者和消费者模式
 * 
 * @author zhxg
 *
 */
class ToastOMatic {
	static class Toast {
		public enum Status {
			DAY, BUTTERED, JAMMED
		}

		private Status status = Status.DAY;
		private final int id;

		public Toast(int idn) {
			id = idn;
		}

		public void butter() {
			status = Status.BUTTERED;
		}

		public void jam() {
			status = Status.JAMMED;
		}

		public Status getStatus() {
			return status;
		}

		public int getId() {
			return id;
		}

		public String toString() {
			return "Toast " + id + ": " + status;
		}
	}

	class ToastQueue extends LinkedBlockingQueue<Toast> {
	}

	class Toaster implements Runnable {
		private ToastQueue toastQueue;
		private int count = 0;
		private Random rand = new Random(47);

		public Toaster(ToastQueue tq) {
			toastQueue = tq;
		}

		public void run() {
			try {
				while (!Thread.interrupted()) {
					TimeUnit.MILLISECONDS.sleep(100 + rand.nextInt(500));
					// Make toast
					Toast t = new Toast(count++);
					System.out.println(t);
					// Insert into queue
					toastQueue.put(t);
				}
			} catch (InterruptedException e) {
				System.out.println("Toaster interrupted");
			}
			System.out.println("Toaster off");
		}
	}

	class Butterer implements Runnable {
		private ToastQueue dryQueue, butteredQueue;

		public Butterer(ToastQueue dry, ToastQueue buttered) {
			dryQueue = dry;
			butteredQueue = buttered;
		}

		public void run() {
			try {
				while (!Thread.interrupted()) {
					// Blocks until next piece of toast is available
					Toast t = dryQueue.take();
					t.butter();
					System.out.println(t);
					butteredQueue.put(t);
				}
			} catch (InterruptedException e) {
				System.out.println("Butterer interrupted");
			}
			System.out.println("Butterer off");
		}
	}

	// apply jam to buttered toast
	class Jammer implements Runnable {
		private ToastQueue butteredQueue, finishedQueue;

		public Jammer(ToastQueue buttered, ToastQueue finished) {
			butteredQueue = buttered;
			finishedQueue = finished;
		}

		public void run() {
			try {
				while (!Thread.interrupted()) {
					// Blocks until next piece of toast is available
					Toast t = butteredQueue.take();
					t.jam();
					System.out.println(t);
					finishedQueue.put(t);
				}
			} catch (InterruptedException e) {
				System.out.println("Jammer interrupted");
			}
			System.out.println("Jammer off");
		}
	}

	// consume the toast
	class Eater implements Runnable {
		private ToastQueue finishedQueue;
		private int counter = 0;

		public Eater(ToastQueue finished) {
			finishedQueue = finished;
		}

		public void run() {
			try {
				while (!Thread.interrupted()) {
					// blocks until next piece of toast is available
					Toast t = finishedQueue.take();
					// verify that the toast is coming in order.
					// and that all pieces are getting jammed
					if (t.getId() != counter++ || t.getStatus() != Toast.Status.JAMMED) {
						System.out.println(">>>> Error: " + t);
						System.exit(1);
					} else {
						System.out.println("Chomp! " + t);
					}
				}
			} catch (InterruptedException e) {
				System.out.println("Eater interrupted");
			}
			System.out.println("Eater off");
		}
	}

	public static void main() throws InterruptedException {
		ToastOMatic tom = new ToastOMatic();
		ToastQueue dryQueue = tom.new ToastQueue(), butteredQueue = tom.new ToastQueue(),
				finishedQueue = tom.new ToastQueue();
		ExecutorService exec = Executors.newCachedThreadPool();
		exec.execute(tom.new Toaster(dryQueue));
		exec.execute(tom.new Butterer(dryQueue, butteredQueue));
		exec.execute(tom.new Jammer(butteredQueue, finishedQueue));
		exec.execute(tom.new Eater(finishedQueue));
		TimeUnit.SECONDS.sleep(5);
		exec.shutdownNow();
	}
}

/**
 * 任务间使用管道进行输入输出
 * 
 * @author zhxg
 *
 */
class PipedIO {
	class Sender implements Runnable {
		private Random rand = new Random(47);
		private PipedWriter out = new PipedWriter();

		public PipedWriter getPipedWriter() {
			return out;
		}

		public void run() {
			try {
				while (true) {
					for (char c = 'A'; c <= 'z'; c++) {
						out.write(c);
						TimeUnit.MILLISECONDS.sleep(rand.nextInt(500));
					}
				}
			} catch (IOException e) {
				System.out.println(e + " Sender write exception");
			} catch (InterruptedException e) {
				System.out.println(e + " Sender sleep interrupted");
			}
		}
	}

	class Receiver implements Runnable {
		private PipedReader in;

		public Receiver(Sender sender) throws IOException {
			in = new PipedReader(sender.getPipedWriter());
		}

		public void run() {
			try {
				while (true) {
					// Blocks until characters are there
					System.out.println("Read: " + (char) in.read() + ", ");
				}
			} catch (IOException e) {
				System.out.println(e + " Receiver read execption");
			}
		}
	}

	public static void main() throws InterruptedException, IOException {
		PipedIO pipeIo = new PipedIO();
		Sender sender = pipeIo.new Sender();
		Receiver receiver = pipeIo.new Receiver(sender);
		ExecutorService exec = Executors.newCachedThreadPool();
		exec.execute(sender);
		exec.execute(receiver);
		TimeUnit.SECONDS.sleep(4);
		exec.shutdownNow();
	}
}

/**
 * 死锁
 * 
 * @author zhxg
 *
 */
class DeadlockingDiningPhilosophers {
	static class Chopstick {
		private boolean taken = false;

		public synchronized void take() throws InterruptedException {
			while (taken)
				wait();
			taken = true;
		}

		public synchronized void drop() {
			taken = false;
			notifyAll();
		}
	}

	static class Philosopher implements Runnable {
		private Chopstick left;
		private Chopstick right;
		private final int id;
		private final int ponderFactor;
		private Random rand = new Random(47);

		public Philosopher(Chopstick left, Chopstick right, int ident, int ponder) {
			this.left = left;
			this.right = right;
			id = ident;
			ponderFactor = ponder;
		}

		private void pause() throws InterruptedException {
			if (ponderFactor == 0)
				return;
			TimeUnit.MILLISECONDS.sleep(rand.nextInt(ponderFactor * 250));
		}

		public void run() {
			try {
				while (!Thread.interrupted()) {
					System.out.println(this + " " + "thinking");
					pause();
					// Philosopher becomes hungry
					System.out.println(this + " grabbing right");
					right.take();
					System.out.println(this + " grabbing left");
					left.take();
					System.out.println(this + " eating");
					pause();
					right.drop();
					left.drop();
				}
			} catch (InterruptedException e) {
				System.out.println(this + " exiting via interrupt");
			}
		}

		public String toString() {
			return "Philosopher " + id;
		}
	}

	public static void main() throws IOException {
		int ponder = 5;
		int size = 5;
		ExecutorService exec = Executors.newCachedThreadPool();
		Chopstick[] sticks = new Chopstick[size];
		for (int i = 0; i < size; i++)
			sticks[i] = new Chopstick();
		for (int i = 0; i < size; i++)
			exec.execute(new Philosopher(sticks[i], sticks[(i + 1) % size], i, ponder));
		// TimeUnit.SECONDS.sleep(5);
		System.out.println("Press 'Enter' to quit");
		System.in.read();
		exec.shutdownNow();
	}
}

/**
 * 通过消除循环等待，解决死锁问题
 * @author zhxg
 *
 */
class FixedDiningPhilosophers {
	public static void main() throws IOException {
		int ponder = 5;
		int size = 5;
		ExecutorService exec = Executors.newCachedThreadPool();
		DeadlockingDiningPhilosophers.Chopstick[] sticks = new DeadlockingDiningPhilosophers.Chopstick[size];
		for (int i = 0; i < size; i++)
			sticks[i] = new DeadlockingDiningPhilosophers.Chopstick();
		for (int i = 0; i < size; i++)
			if (i < size - 1)
				exec.execute(new DeadlockingDiningPhilosophers.Philosopher(sticks[i], sticks[i + 1], i, ponder));
			else
				exec.execute(new DeadlockingDiningPhilosophers.Philosopher(sticks[0], sticks[i], 1, ponder));
		
		System.out.println("Press 'Enter' to quit");
		System.in.read();
		exec.shutdownNow();
	}
}