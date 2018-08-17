package org.dlj.concurrent.thinkingInJava;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

public class ConcurrentApplications {

	public static void main(String[] args) throws IOException, InterruptedException {
//		BankTellerSimulation.main();
		
//		RestaurantWithQueues.main();
		
		CarBuilder.main();
	}
}

/**
 * 银行出纳员仿真
 * @author zhxg
 *
 */
class BankTellerSimulation {
	static class Customer {
		private final int serviceTime;
		public Customer(int tm) {serviceTime = tm; }
		public String toString() {
			return "[" + serviceTime + "]";
		}
		public int getServiceTime() {
			return serviceTime;
		}
	}
	// Teach the customer line to display itself
	static class CustomerLine extends ArrayBlockingQueue<Customer> {
		public CustomerLine(int maxLineSize) {
			super(maxLineSize);
		}
		public String toString() {
			if(this.size() == 0) {
				return "[Empty]";
			}
			StringBuilder result = new StringBuilder();
			for (Customer customer : this) {
				result.append(customer);
			}
			return result.toString();
		}
	}
	// Randomly add customers to a queue
	static class CustomerGenerator implements Runnable {
		private CustomerLine customers;
		private static Random rand = new Random(47);
		public CustomerGenerator(CustomerLine cq) {
			customers = cq;
		}
		public void run() {
			try {
				while (!Thread.interrupted()) {
					TimeUnit.MILLISECONDS.sleep(rand.nextInt(300));
					customers.put(new Customer(rand.nextInt(1000)));
				}
			} catch (InterruptedException e) {
				System.out.println("CustomerGenerator interrupted");
			}
			System.out.println("CustomerGenerator terminating");
		}
	}
	static class Teller implements Runnable, Comparable<Teller> {
		private static int counter = 0;
		private final int id = counter++;
		// Customers served during this shift
		private int customersServed = 0;
		private CustomerLine customers;
		private boolean servingCustomerLine = true;
		public Teller(CustomerLine cq) {customers = cq; }
		public void run() {
			try {
				while (!Thread.interrupted()) {
					Customer customer = customers.take();
					TimeUnit.MILLISECONDS.sleep(customer.getServiceTime());
					synchronized(this) {
						customersServed++;
						while (!servingCustomerLine)
							wait();
					}
				}
			} catch (InterruptedException e) {
				System.out.println(this + "interrupted");
			}
			System.out.println(this + "terminating");
		}
		public synchronized void doSomethingElse() {
			customersServed = 0;
			servingCustomerLine = false;
		}
		public synchronized void serveCustomerLine() {
			assert !servingCustomerLine:"already serving: " + this;
			servingCustomerLine = true;
			notifyAll();
		}
		public String toString() {return "Teller " + id + " "; }
		public String shortString() {return "T" + id; }
		// Used by priority queue
		public synchronized int compareTo(Teller other) {
			return customersServed < other.customersServed 
					? -1 : (customersServed == other.customersServed ? 0 : 1);
		}
	}
	static class TellerManager implements Runnable {
		private ExecutorService exec;
		private CustomerLine customers;
		private PriorityQueue<Teller> workingTellers = new PriorityQueue<Teller>();
		private Queue<Teller> tellersDoingOtherThings = new LinkedList<Teller>();
		private int adjustmentPeriod;
		private static Random rand = new Random(47);
		public TellerManager(ExecutorService e, CustomerLine customers, int adjustmentPeriod) {
			exec = e;
			this.customers = customers;
			this.adjustmentPeriod = adjustmentPeriod;
			// Start with a single teller;
			Teller teller = new Teller(customers);
			exec.execute(teller);
			workingTellers.add(teller);
		}
		public void adjustTellerNumber() {
			/*
			 * This is actually a control system. By adjusting
			 * the numbers, you can reveal stability issues in
			 * the control mechanism
			 * if line is too long. add another teller
			 */
			if (customers.size() / workingTellers.size() > 2) {
				// if tellers are on break or doing another job, bring one back
				if (tellersDoingOtherThings.size() > 0) {
					Teller teller = tellersDoingOtherThings.remove();
					teller.serveCustomerLine();
					workingTellers.offer(teller);
					return;
				}
				// else create (hire) a new tell
				Teller teller = new Teller(customers);
				exec.execute(teller);
				workingTellers.add(teller);
				return;
			}
			// if line is short enough, remove a teller
			if (workingTellers.size() > 1 && customers.size() / workingTellers.size() < 2)
				reassignOneTeller();
			// if there is no line, we only need one teller
			if (customers.size() == 0)
				while (workingTellers.size() > 1)
					reassignOneTeller();
		}
		// Give a teller a different job or a break
		private void reassignOneTeller() {
			Teller teller = workingTellers.poll();
			teller.doSomethingElse();
			tellersDoingOtherThings.offer(teller);
		}
		public void run() {
			try {
				while (!Thread.interrupted()) {
					TimeUnit.MILLISECONDS.sleep(adjustmentPeriod);
					adjustTellerNumber();
					System.out.print(customers + " { ");
					for (Teller teller : workingTellers)
						System.out.print(teller.shortString() + " ");
					System.out.println("}");
				}
			} catch (InterruptedException e) {
				System.out.println(this + "interrupted");
			}
			System.out.println(this + "terminating");
		}
		public String toString() {return "TellerManager "; }
	}

	static final int MAX_LINE_SIZE = 50;
	static final int ADJUSTMENT_PERIOD = 1000;
	public static void main() throws IOException {
		ExecutorService exec = Executors.newCachedThreadPool();
		// if line is too long, customers will leave
		CustomerLine customers = new CustomerLine(MAX_LINE_SIZE);
		exec.execute(new CustomerGenerator(customers));
		// Manager will add and remove tellers as necessary
		exec.execute(new TellerManager(exec, customers, ADJUSTMENT_PERIOD));
		System.out.println("Press 'enter' to quit");
		System.in.read();
		exec.shutdownNow();
	}
}

interface Food {
	enum Appetizer implements Food {
		SALAD, SOUP, SPRING_ROLLS;
	}
	enum MainCourse implements Food {
		LASAGNE, BURRITO, PAD_THAI,
		LENTILS, HUMMOUS, VINDALOO;
	}
	enum Dessert implements Food {
		TIRAMISU, GELATO, BLACK_FOREST_CAKE,
		FRUIT, CREME_CARAMEL;
	}
	enum Coffee implements Food {
		BLACK_COFFEE, DECAF_COFFEE, ESPRESSO,
		LATTE, CAPPUCCINO, TEA, HERB_TEA;
	}
}
class Enums {
	private static Random rand = new Random(47);
	public static <T extends Enum<T>> T random(Class<T> ec) {
		return random(ec.getEnumConstants());
	}
	public static<T> T random(T[] values) {
		return values[rand.nextInt(values.length)];
	}
}
enum Course {
	APPETIZER(Food.Appetizer.class),
	MAINCOURSE(Food.MainCourse.class),
	DESSERT(Food.Dessert.class),
	COFFEE(Food.Coffee.class);
	private Food[] values;
	private Course(Class<? extends Food> kind) {
		values = kind.getEnumConstants();
	}
	public Food randomSelection() {
		return Enums.random(values);
	}
}

/**
 * 饭店服务仿真
 * @author zhxg
 *
 */
class RestaurantWithQueues {
	static class Order {
		private static int counter = 0;
		private final int id = counter++;
		private final Customer customer;
		private final WaitPerson waitPerson;
		private final Food food;
		public Order(Customer cust, WaitPerson wp, Food f) {
			customer = cust;
			waitPerson = wp;
			food = f;
		}
		public Food item() {return food; }
		public Customer getCustomer() {return customer; }
		public WaitPerson getWaitPerson() {return waitPerson; }
		public String toString() {
			return "Order: " + id + " item: " + food + " for: " + customer + " served by: " + waitPerson;
		}
	}
	// This is what comes back from the chef
	static class Plate {
		private final Order order;
		private final Food food;
		public Plate(Order ord, Food f) {
			order = ord;
			food = f;
		}
		public Order getOrder() {return order; }
		public Food getFood() {return food; }
		public String toString() {return food.toString(); }
	}
	static class Customer implements Runnable {
		private static int counter = 0;
		private final int id = counter++;
		private final WaitPerson waitPerson;
		// Only one course at a time can be received
		private SynchronousQueue<Plate> placeSetting = new SynchronousQueue<Plate>();
		public Customer(WaitPerson w) { waitPerson = w; }
		public void deliver(Plate p) throws InterruptedException {
			// Only blocks if customer is still
			// eating the previous course
			placeSetting.put(p);
		}
		public void run() {
			for (Course course : Course.values()) {
				Food food = course.randomSelection();
				try {
					waitPerson.placeOrder(this, food);
					// Blocks until course has been delivered
					System.out.println(this + "eating " + placeSetting.take());
				} catch (InterruptedException e) {
					System.out.println(this + "waiting for " + course + " interrupted");
					break;
				}
			}
			System.out.println(this + "finished meal, leaving");
		}
		public String toString() {
			return "Customer " + id + " ";
		}
	}
	static class WaitPerson implements Runnable {
		private static int counter = 0;
		private final int id = counter++;
		private final Restaurant restaurant;
		BlockingQueue<Plate> filledOrders = new LinkedBlockingQueue<Plate>();
		public WaitPerson(Restaurant rest) {restaurant = rest; }
		public void placeOrder(Customer cust, Food food) {
			try {
				// Shouldn't actually block because this is a linkedBlockingQueue with no size limit
				restaurant.orders.put(new Order(cust, this, food));
			} catch (InterruptedException e) {
				System.out.println(this + " placeOrder interrupted");
			}
		}
		public void run() {
			try {
				while (!Thread.interrupted()) {
					// Blocks until a course is ready
					Plate plate = filledOrders.take();
					System.out.println(this + "received " + plate + " delivering to " + plate.getOrder().getCustomer());
					plate.getOrder().getCustomer().deliver(plate);
				}
			} catch (InterruptedException e) {
				System.out.println(this + " interrupted");
			}
			System.out.println(this + " off duty");
		}
		public String toString() {
			return "WaitPerson " + id + " ";
		}
	}
	static class Chef implements Runnable {
		private static int counter = 0;
		private final int id = counter++;
		private final Restaurant restaurant;
		private static Random rand = new Random(47);
		public Chef(Restaurant rest) {restaurant = rest; }
		public void run() {
			try {
				while (!Thread.interrupted()) {
					// Blocks until an order appears
					Order order = restaurant.orders.take();
					Food requestedItem = order.item();
					// Time to prepare order
					TimeUnit.MILLISECONDS.sleep(rand.nextInt(500));
					Plate plate = new Plate(order, requestedItem);
					order.getWaitPerson().filledOrders.put(plate);
				}
			} catch (InterruptedException e) {
				System.out.println(this + " interrupted");
			}
			System.out.println(this + " off duty");
		}
		public String toString() {return "Chef " + id + " "; }
	}
	static class Restaurant implements Runnable {
		private List<WaitPerson> waitPersons = new ArrayList<WaitPerson>();
		private List<Chef> chefs = new ArrayList<Chef>();
		private ExecutorService exec;
		private static Random rand = new Random(47);
		BlockingQueue<Order> orders = new LinkedBlockingQueue<Order>();
		public Restaurant(ExecutorService e, int nWaitPersons, int nChefs) {
			exec = e;
			for (int i = 0; i < nWaitPersons; i++) {
				WaitPerson waitPerson = new WaitPerson(this);
				waitPersons.add(waitPerson);
				exec.execute(waitPerson);
			}
			for (int i = 0; i < nChefs; i++) {
				Chef chef = new Chef(this);
				chefs.add(chef);
				exec.execute(chef);
			}
		}
		public void run() {
			try {
				while (!Thread.interrupted()) {
					// A new customer arrives; assign a waitPerson
					WaitPerson wp = waitPersons.get(rand.nextInt(waitPersons.size()));
					Customer c = new Customer(wp);
					exec.execute(c);
					TimeUnit.MILLISECONDS.sleep(100);
				}
			} catch (InterruptedException e) {
				System.out.println("Restaurant interrupted");
			}
			System.out.println("Restaurant closing");
		}
	}
	
	public static void main() throws IOException {
		ExecutorService exec = Executors.newCachedThreadPool();
		Restaurant restaurant = new Restaurant(exec, 5, 2);
		exec.execute(restaurant);
		System.out.println("press 'enter' to quit");
		System.in.read();
		exec.shutdownNow();
	}
}


class CarBuilder {
	class Car {
		private final int id;
		private boolean engine = false, driveTrain = false, wheels = false;
		public Car(int idn) {id = idn; }
		// empty car object
		public Car() {id = -1; }
		public synchronized int getId() {return id; }
		public synchronized void addEngine() {engine = true; }
		public synchronized void addDriveTrain() {driveTrain = true; }
		public synchronized void addWheels() { wheels = true; }
		public synchronized String toString() {
			return "Car " + id + " [" + " engine: " + engine + " driveTrain: " + driveTrain + " wheels: " + wheels + " ]";
		}
	}
	class CarQueue extends LinkedBlockingQueue<Car>{}
	class ChassisBuilder implements Runnable {
		private CarQueue carQueue;
		private int counter = 0;
		public ChassisBuilder(CarQueue cq) {carQueue = cq; }
		public void run() {
			try {
				while (!Thread.interrupted()) {
					TimeUnit.MILLISECONDS.sleep(500);
					// Make chassis
					Car c = new Car(counter++);
					System.out.println("ChassisBuilder created " + c);
					// Insert into queue
					carQueue.put(c);
				}
			} catch (InterruptedException e) {
				System.out.println("Interrupted: ChassisBuilder");
			}
			System.out.println("ChassisBuilder off");
		}
	}
	class Assembler implements Runnable {
		private CarQueue chassisQueue, finishingQueue;
		private Car car;
		private CyclicBarrier barrier = new CyclicBarrier(4);
		private RobotPool robotPool;
		public Assembler(CarQueue cq, CarQueue fq, RobotPool rp) {
			chassisQueue = cq;
			finishingQueue = fq;
			robotPool = rp;
		}
		public Car car() {return car; }
		public CyclicBarrier barrier() {
			return barrier;
		}
		public void run() {
			try {
				while (!Thread.interrupted()) {
					// Blocks until chassis is available
					car = chassisQueue.take();
					// Hire robots to perform work
					robotPool.hire(EngineRobot.class, this);
					robotPool.hire(DriveTrainRobot.class, this);
					robotPool.hire(WheelRobot.class, this);
					barrier.await(); // until the robots finish
					// put car into finishingQueue for further work
					finishingQueue.put(car);
				}
			} catch (InterruptedException e) {
				System.out.println("Exiting Assembler via interrupt");
			} catch (BrokenBarrierException e) {
				// This one we want to know about
				throw new RuntimeException(e);
			}
			System.out.println("Assembler off");
		}
	}
	class Reporter implements Runnable {
		private CarQueue carQueue;
		public Reporter(CarQueue cq) {carQueue = cq; }
		public void run() {
			try {
				while (!Thread.interrupted()) {
					System.out.println(carQueue.take());
				}
			} catch (InterruptedException e) {
				System.out.println("Exiting Reporter via interrupt");
			}
			System.out.println("Reporter off");
		}
	}
	abstract class Robot implements Runnable {
		private RobotPool pool;
		public Robot(RobotPool p) {pool = p; }
		protected Assembler assembler;
		public Robot assignAssembler(Assembler assembler) {
			this.assembler = assembler;
			return this;
		}
		private boolean engage = false;
		public synchronized void engage() {
			engage = true;
			notifyAll();
		}
		// The part of run() that's different for each robot
		abstract protected void performService();
		public void run() {
			try {
				powerDown(); // wait until needed
				while (!Thread.interrupted()) {
					performService();
					assembler.barrier().await(); // synchronize
					// We're down with that job...
					powerDown();
				}
			} catch (InterruptedException e) {
				System.out.println("Exiting " + this + " via interrupt");
			} catch (BrokenBarrierException e) {
				// this one we want to know about
				throw new RuntimeException(e);
			}
			System.out.println(this + " off");
		}
		private synchronized void powerDown() throws InterruptedException {
			engage = false;
			assembler = null; // Disconnect from the assembler
			// put ourselves back in the available pool
			pool.release(this);
			while (engage == false) // power down
				wait();
		}
		public String toString() {return getClass().getName(); }
	}
	class EngineRobot extends Robot {
		public EngineRobot(RobotPool pool) {super(pool); }
		protected void performService() {
			System.out.println(this + " installing engine");
			assembler.car().addEngine();
		}
	}
	class DriveTrainRobot extends Robot {
		public DriveTrainRobot(RobotPool pool) {super(pool); }
		protected void performService() {
			System.out.println(this + " installing DriveTrain");
			assembler.car().addDriveTrain();
		}
	}
	class WheelRobot extends Robot {
		public WheelRobot(RobotPool pool) {super(pool); }
		protected void performService() {
			System.out.println(this + " installing wheels");
			assembler.car().addWheels();
		}
	}
	class RobotPool {
		// Quietly prevents identical entries
		private Set<Robot> pool = new HashSet<Robot>();
		public synchronized void add(Robot r) {
			pool.add(r);
			notifyAll();
		}
		public synchronized void hire(Class<? extends Robot> robotType, Assembler d) throws InterruptedException {
			for (Robot r : pool)
				if (r.getClass().equals(robotType)) {
					pool.remove(r);
					r.assignAssembler(d);
					r.engage(); // Power it up to do the task
					return;
				}
			wait(); // None available
			hire(robotType, d); // Try again, recursively
		}
		public synchronized void release(Robot r) {add(r); }
	}
	
	public static void main() throws InterruptedException {
		CarBuilder cb = new CarBuilder();
		CarQueue chassisQueue = cb.new CarQueue(), 
				finishingQueue = cb.new CarQueue();
		ExecutorService exec = Executors.newCachedThreadPool();
		RobotPool robotPool = cb.new RobotPool();
		exec.execute(cb.new EngineRobot(robotPool));
		exec.execute(cb.new DriveTrainRobot(robotPool));
		exec.execute(cb.new WheelRobot(robotPool));
		exec.execute(cb.new Assembler(chassisQueue, finishingQueue, robotPool));
		exec.execute(cb.new Reporter(finishingQueue));
		// Start everything running by producing chassis
		exec.execute(cb.new ChassisBuilder(chassisQueue));
		TimeUnit.SECONDS.sleep(7);
		exec.shutdownNow();
	}
}