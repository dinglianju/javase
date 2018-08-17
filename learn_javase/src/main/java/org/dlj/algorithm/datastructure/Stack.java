package org.dlj.algorithm.datastructure;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class Stack<Item> implements Iterable<Item> {

	private Item[] a; // 数组标识栈，栈顶在最大的下标
	private int n;
	
	public Stack() {
		a = (Item[]) new Object[2];
		n = 0;
	}
	
	public boolean isEmpty() {
		return n == 0;
	}

	public int size() {
		return n;
	}
	
	private void resize(int capacity) {
		assert capacity >= n;
		// 注意不能直接创建泛型数组
		Item[] temp = (Item[]) new Object[capacity];
		for (int i = 0; i < n; i++) {
			temp[i] = a[i];
		}
		a = temp;
		// 也可以选择下面这种方式改变数组大小
		// a = java.util.Arrays.copyOf(a, capacity);
	}
	
	/**
	 * 压入元素
	 * @param item
	 */
	public void push(Item item) {
		// 先判断n的大小，如果栈满则改变栈的大小
		if (n == a.length) resize(2 * a.length);
		a[n++] = item;
	}
	
	/**
	 * 弹出并返回元素
	 * @return
	 */
	public Item pop() {
		if (isEmpty()) throw new NoSuchElementException();
		Item item = a[n - 1];
		a[n - 1] = null; // 防止对象游离
		n--;
		// 如果有必要则调整栈的大小
		if (n > 0 && n == a.length/4) resize(a.length/2);
		return item;
	}
	
	/**
	 * 返回但不弹出栈顶元素
	 * @return
	 */
	public Item peek() {
		if (isEmpty()) throw new NoSuchElementException();
		return a[n - 1];
	}
	
	@Override
	public Iterator<Item> iterator() {
		return new ReverseArrayIterator();
	}
	
	// 使用内部类实现迭代器接口，实现从栈顶往栈底的先进后出迭代，没有实现remove()方法
	private class ReverseArrayIterator implements Iterator<Item> {
		private int i;
		public ReverseArrayIterator() {
			i = n - 1;
		}
		public boolean hasNext() {
			return i >= 0;
		}
		public void remove() {
			throw new UnsupportedOperationException();
		}
		public Item next() {
			if (!hasNext()) throw new NoSuchElementException();
			return a[i--];
		}
	}
	
	public static void main(String[] args) {
		Stack<String> stack = new Stack<String>();
		while (!StdIn.isEmpty()) {
			String item = StdIn.readString();
			if (!item.equals("-")) stack.push(item);
			else if (!stack.isEmpty()) StdOut.print(stack.pop() + " ");
		}
		StdOut.println("(" + stack.size() + " left on stack)");
	}
}

class StackByLinkList<Item> implements Iterable<Item> {
	private Node<Item> first;	// 栈顶节点
	private int N;	// 栈内元素数量
	
	// 辅助类Node，用于形成链表
	private static class Node<Item> {
		private Item item;
		private Node<Item> next;
	}
	
	/**
	 * 初始化栈
	 */
	public StackByLinkList() {
		first = null;
		N = 0;
	}
	
	public boolean isEmpty() {
		return first == null;
		// return N == 0;
	}
	
	public int size() {
		return N;
	}
	
	public void push(Item item) {
		Node<Item> oldfirst = first;
		first = new Node<Item>();
		first.item = item;
		first.next = oldfirst;
		N++;
	}
	
	public Item pop() {
		if (isEmpty()) throw new NoSuchElementException();
		Item item = first.item; // 需弹出的元素
		first = first.next;
		N--;
		return item;
	}
	
	/**
	 * 返回但不弹出元素
	 * @return
	 */
	public Item peek() {
		if (isEmpty()) throw new NoSuchElementException();
		return first.item;
	}
	
	public String toString() {
		StringBuilder s = new StringBuilder();
		for (Item item : this) 
			s.append(item + " ");
		return s.toString();
	}
	
	public Iterator<Item> iterator() {
		return new ListIterator<Item>(first);
	}
	
	// 实现Iterator接口用于迭代
	private class ListIterator<Item> implements Iterator {
		private Node<Item> current;
		
		public ListIterator(Node<Item> first) {
			current = first;
		}
		
		public boolean hasNext() {
			return current != null;
		}
		
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
		public Item next() {
			if (!hasNext()) throw new NoSuchElementException();
			Item item = current.item;
			current = current.next;
			return item;
		}
	}
	
	public static void  main(String[] args) {
		Stack<String> s = new Stack<String>();
		while (!StdIn.isEmpty()) {
			String item = StdIn.readString();
			if (!item.equals("-")) s.push(item);
			else if (!s.isEmpty()) StdOut.print(s.pop() + " ");
		}
		StdOut.println("(" + s.size() + " left on stack)");
	}
}
