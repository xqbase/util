package com.xqbase.util;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.xqbase.util.function.ConsumerEx;
import com.xqbase.util.function.SupplierEx;

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
				try {
					finalizer.accept(object);
				} catch (Exception e) {
					// Ignored
				}
			} else {
				deque.offerFirst(this);
			}
		}
	}

	private int timeout;
	private AtomicLong accessed = new AtomicLong(System.currentTimeMillis());

	SupplierEx<? extends T, ? extends E> initializer;
	ConsumerEx<? super T, ?> finalizer;
	ConcurrentLinkedDeque<Entry> deque = new ConcurrentLinkedDeque<>();
	AtomicInteger activeCount = new AtomicInteger(0);
	volatile boolean closed = false;

	public Pool(SupplierEx<? extends T, ? extends E> initializer,
			ConsumerEx<? super T, ?> finalizer, int timeout) {
		this.initializer = initializer;
		this.finalizer = finalizer;
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
					try {
						finalizer.accept(entry.object);
					} catch (Exception e) {
						// Ignored
					}
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
		entry.object = initializer.get();
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
			try {
				finalizer.accept(entry.object);
			} catch (Exception e) {
				// Ignored
			}
		}
	}
}