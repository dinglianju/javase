package org.dlj.concurrent;

import java.util.concurrent.CountDownLatch;

public class TestHarness {

	public long timeTasks(int nThreads, final Runnable task) throws InterruptedException {
		final CountDownLatch startGate = new CountDownLatch(1);
		final CountDownLatch endGate = new CountDownLatch(nThreads);
		
		for (int i = 0; i < nThreads; i++) {
			Thread t = new Thread() {
				public void run() {
					try {
						System.out.println("Thread: " + Thread.currentThread().getId() + " before startGate: " + startGate.getCount());
						startGate.await();
						System.out.println("Thread: " + Thread.currentThread().getId() + " after startGate: " + startGate.getCount());
						
						try {
							task.run();
						} finally {
							endGate.countDown();
							System.out.println("Thread: " + Thread.currentThread().getId() + "endGate: " +endGate.getCount());
						}
					} catch (InterruptedException ignored) {
						ignored.printStackTrace();
					}
				}
			};
			t.start();
		}
		
		long start = System.nanoTime();
		startGate.countDown();
		endGate.await();
		long end = System.nanoTime();
		return end - start;
	}
	
	public static void main(String[] args) throws InterruptedException {
		TestHarness th = new TestHarness();
		long time = th.timeTasks(5, new Runnable() {
			public void run() {
				System.out.println("task start, Thread: " + Thread.currentThread().getId());
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});
		
		System.out.println(time);
	}
}
