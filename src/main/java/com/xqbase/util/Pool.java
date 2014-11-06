package com.xqbase.util;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Pool<T, E extends Exception> implements AutoCloseable {
	public class Entry implements AutoCloseable {
		T object;
		long created, borrowed, borrows;
		boolean valid;

		public T getObject() {
			return object;
		}

		public long getCreated() {
			return created;
		}

		public long getBorrowed() {
			return borrowed;
		}

		public long getBorrows() {
			return borrows;
		}

		public boolean isValid() {
			return valid;
		}

		public void setValid(boolean valid) {
			this.valid = valid;
		}

		@Override
		public void close() {
			activeCount.decrementAndGet();
			if (closed || !valid) {
				supplier.close(object);
			} else {
				deque.offerFirst(this);
			}
		}
	}

	private int timeout;
	private AtomicLong accessed = new AtomicLong(System.currentTimeMillis());

	SupplierEx<T, E> supplier;
	ConcurrentLinkedDeque<Entry> deque = new ConcurrentLinkedDeque<>();
	AtomicInteger activeCount = new AtomicInteger(0);
	volatile boolean closed = false;

	public Pool(SupplierEx<T, E> supplier, int timeout) {
		this.supplier = supplier;
		this.timeout = timeout;
	}

	public Entry borrow() throws E {
		long now = System.currentTimeMillis();
		if (timeout > 0) {
			long accessed_ = accessed.get();
			if (now > accessed_ + Time.SECOND &&
					accessed.compareAndSet(accessed_, now)) {
				Entry entry;
				while ((entry = deque.pollLast()) != null) {
					if (now < entry.borrowed + timeout) {
						deque.offerLast(entry);
						break;
					}
					supplier.close(entry.object);
				}
			}
		}

		Entry entry = deque.pollFirst();
		if (entry != null) {
			entry.borrows ++;
			entry.valid = false;
			activeCount.incrementAndGet();
			return entry;
		}
		entry = new Entry();
		entry.object = supplier.get();
		entry.created = entry.borrowed = now;
		entry.borrows = 0;
		entry.valid = false;
		activeCount.incrementAndGet();
		return entry;
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
		Entry entry;
		while ((entry = deque.pollFirst()) != null) {
			activeCount.decrementAndGet();
			supplier.close(entry.object);
		}
	}
}