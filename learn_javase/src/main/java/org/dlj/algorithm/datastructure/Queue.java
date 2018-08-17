package org.dlj.algorithm.datastructure;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class Queue<Item> implements Iterable<Item> {

	private Item[] q;
	private int N;			// 队列中的元素数量
	private int first;		// 队头元素的下标
	private int last;		// 队尾元素的后一个位置的下标
	
	public Queue() {
		q = (Item[]) new Object[2];
		N = 0;
		first = 0;
		last = 0;
	}
	
	public boolean isEmpty() {
		return N == 0;
	}
	
	public int size() {
		return N;
	}
	
	private void resize(int max) {
		assert max >= N;
		Item[] temp = (Item[]) new Object[max];
		/*
		 * 注意：把N个元素放入总大小为max的队列（max>=N）
		 * 因为循环使用数组，从first开始的第i个元素可能保存在
		 * 前面（即last在first前面）
		 */
		for (int i = 0; i < N; i++) {
			temp[i] = q[(first + i) % q.length];
		}
		q = temp;
		// 把小队列按顺序复制到大队列后重置队头和队尾
		first = 0;
		last = N;
	}
	
	/**
	 * 元素入列
	 * @param item
	 */
	public void enqueue(Item item) {
		if (N == q.length) resize(2*q.length);
		q[last++] = item; // 元素入列
		if (last == q.length) last = 0;  // 如果last超出数组下标，把list置零循环利用数组
		N++;
	}
	
	/**
	 * 元素出列
	 * @return
	 */
	public Item dequeue() {
		if (isEmpty()) throw new NoSuchElementException();
		Item item = q[first];
		q[first] = null; // 防止对象游离
		N--;
		first++;
		if (first == q.length) first = 0; // 循环利用数组，下一个队头在下标为零的地方
		if (N > 0 && N == q.length/4) resize(q.length/2);
		return item;
	}
	
	/**
	 * 返回队头元素但不出列
	 * @return
	 */
	public Item peek() {
		if (isEmpty()) throw new NoSuchElementException();
		return q[first];
	}
	
	/**
	 * 实现Iteratle接口
	 */
	public Iterator<Item> iterator() {
		return new ArrayIterator();
	}
	
	private class ArrayIterator implements Iterator<Item> {
		// 维护一个i用于迭代
		private int i = 0;
		public boolean hasNext() {return i < N; }
		public void remove() {throw new UnsupportedOperationException(); }
		
		// 直接利用first进行遍历，注意可能存在数组的循环利用
		public Item next() {
			if (!hasNext()) throw new NoSuchElementException();
			Item item = q[(i + first) % q.length];
			i++;
			return item;
		}
	}
	
	public static void main(String[] args) {
		Queue<String> q = new Queue<String>();
		while (!StdIn.isEmpty()) {
			String item = StdIn.readString();
			if (!item.equals("-")) q.enqueue(item);
			else if (!q.isEmpty()) StdOut.print(q.dequeue());
		}
		StdOut.println("(" + q.size() + " left on queue)");
	}
}

class StdIn{
	private static final BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
	
	private static String readStr; 
	public static boolean isEmpty() {
		try {
			readStr = stdIn.readLine();
			if (readStr != null && !readStr.isEmpty())
				return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	
	public static String readString() {
		
		return readStr;
	}
}

class StdOut {
	public static void print(String str) {
		System.out.print(str);
	}
	
	public static void println(String str) {
		System.out.println(str);
	}
}

class QueueByLinkedList<Item> implements Iterable<Item> {
	private Node<Item> first; // 队头节点
	private Node<Item> last; // 队尾节点（注意和上面的last区分，last并不是队尾元素的下标
	private int N;
	
	/**
	 * 辅助类Node
	 * @author zhxg
	 *
	 * @param <Item>
	 */
	private static class Node<Item> {
		private Item item;
		private Node<Item> next;
	}
	
	/**
	 * 初始化队列
	 */
	public QueueByLinkedList() {
		first = null;
		last = null;
		N = 0;
	}
	
	public boolean isEmpty() {
		return first == null;
	}
	
	public int size() {
		return N;
	}
	
	public Item peek() {
		if (isEmpty()) throw new NoSuchElementException();
		return first.item;
	}
	
	public void enqueue(Item item) {
		// 记录尾节点
		Node<Item> oldlast = last;
		// 创建新的尾节点
		last = new Node<Item>();
		last.item = item;
		last.next = null;
		// 如果队列为空，将first置为last，因为这个时候队列中只有一个元素
		if (isEmpty()) first = last;
		// 否则执行正常的在尾节点插入新节点的操作
		else oldlast.next = last;
		N++;
	}
	
	public Item dequeue() {
		if (isEmpty()) throw new NoSuchElementException();
		// 队头元素出列
		Item item = first.item;
		first = first.next;
		N--;
		// 如果这时候队列为空，表示原来只有一个元素，这时候也将list置为null
		if (isEmpty()) last = null;
		return item;
	}
	
	public String toString() {
		StringBuilder s = new StringBuilder();
		for (Item item : this) {
			s.append(item + " ");
		}
		return s.toString();
	}
	
	public Iterator<Item> iterator() {
		return new ListIterator<Item>(first);
	}
	
	private class ListIterator<Item> implements Iterator<Item> {
		private Node<Item> current;
		// 要实现迭代，只需要维护一个节点，并在开始的时候将它置为first
		public ListIterator(Node<Item> first) {
			current = first;
		}
		
		public boolean hasNext() {return current != null; }
		public void remove() {throw new UnsupportedOperationException(); }
		
		public Item next() {
			if (!hasNext()) throw new NoSuchElementException();
			Item item = current.item;
			current = current.next;
			return item;
		}
	}
	
	public static void main(String[] args) {
		QueueByLinkedList<String> q = new QueueByLinkedList<String>();
		while (!StdIn.isEmpty()) {
			String item = StdIn.readString();
			if (!item.equals("-")) q.enqueue(item);
			else if (!q.isEmpty()) StdOut.print(q.dequeue());
		}
		
		StdOut.println("(" + q.size() + " left on queuelinkedlist)");
	}
}