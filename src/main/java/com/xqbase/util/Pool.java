package com.xqbase.util;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public abstract class Pool<T, E extends Exception> implements AutoCloseable {
	public static class Entry<T> {
		public T obj;
		public long created;
		public long borrowed;
		public long borrows;
	}

	protected abstract T makeObject() throws E;

	protected void destroyObject(T obj) {
		if (obj instanceof AutoCloseable) {
			try {
				((AutoCloseable) obj).close();
			} catch (Exception e) {/**/}
		}
	}

	private int timeout;
	private ConcurrentLinkedDeque<Entry<T>> deque = new ConcurrentLinkedDeque<>();
	private AtomicInteger activeCount = new AtomicInteger(0);
	private AtomicLong accessed = new AtomicLong(System.currentTimeMillis());
	private volatile boolean closed = false;

	public Pool(int timeout) {
		this.timeout = timeout;
	}

	public Entry<T> borrow() throws E {
		long now = System.currentTimeMillis();
		if (timeout > 0) {
			long accessed_ = accessed.get();
			if (now > accessed_ + Time.SECOND &&
					accessed.compareAndSet(accessed_, now)) {
				Entry<T> entry;
				while ((entry = deque.pollLast()) != null) {
					if (now < entry.borrowed + timeout) {
						deque.offerLast(entry);
						break;
					}
					destroyObject(entry.obj);
				}
			}
		}

		Entry<T> entry = deque.pollFirst();
		if (entry != null) {
			entry.borrows ++;
			activeCount.incrementAndGet();
			return entry;
		}
		entry = new Entry<>();
		entry.obj = makeObject();
		entry.created = entry.borrowed = now;
		entry.borrows = 0;
		activeCount.incrementAndGet();
		return entry;
	}

	public void return_(Entry<T> entry, boolean valid) {
		activeCount.decrementAndGet();
		if (closed || !valid) {
			destroyObject(entry.obj);
		} else {
			deque.offerFirst(entry);
		}
	}

	public int getActiveCount() {
		return activeCount.get();
	}

	public int getInactiveCount() {
		return deque.size();
	}

	public void reopen() {
		closed = false;
	}

	@Override
	public void close() {
		closed = true;
		Entry<T> entry;
		while ((entry = deque.pollFirst()) != null) {
			activeCount.decrementAndGet();
			destroyObject(entry.obj);
		}
	}
}