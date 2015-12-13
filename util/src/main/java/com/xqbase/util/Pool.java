package com.xqbase.util;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.xqbase.util.function.ConsumerEx;
import com.xqbase.util.function.SupplierEx;

/**
 * A simple and thread-safe pool implemented by deque:
 * active objects are returned and borrowed from the head,
 * and timeout objects are removed from the tail.<p>
 *
 * A pooled object is controlled by its pool entry. When an entry closed,
 * the pooled object is either returned to the pool or destroyed, e.g.
 * <pre><code>
 *	try (Pool&lt;Socket&gt;.Entry entry = pool.borrow()) {
 *		Socket socket = entry.getObject();
 *		// use the socket
 *		...
 *		// return if socket is ok
 *		entry.setValid(true);
 *	} catch (IOException e) {
 *		// handle exception
 *		...
 *		// destroy if exception thrown
 *		// entry.setValid(false) by default
 *	}
 * </code></pre>
 *
 * @param <T> type of pooled object
 * @param <E> type of exception when borrowing
 */
public class Pool<T, E extends Exception> implements AutoCloseable {
	public class Entry implements AutoCloseable {
		T object;
		long created, borrowed, borrows;
		boolean valid;

		/** @return the pooled object */
		public T getObject() {
			return object;
		}

		/** @return created time of the pooled object in milliseconds */
		public long getCreated() {
			return created;
		}

		/** @return last borrowed time in milliseconds */
		public long getBorrowed() {
			return borrowed;
		}

		/** @return how many times borrowed */
		public long getBorrows() {
			return borrows;
		}

		/** @return whether the entry is valid */
		public boolean isValid() {
			return valid;
		}

		/**
		 * Validate or invalidate the entry
		 *
		 * @see #close()
		 */
		public void setValid(boolean valid) {
			this.valid = valid;
		}

		/**
		 * A valid entry will return itself to the pool,
		 * while a invalid entry will destroy its pooled object.
		 */
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

	/**
	 * Create a pool by the given initializer, finalizer and timeout.
	 *
	 * @param initializer a supplier to create the pooled object, e.g. Socket::new
	 * @param finalizer a consumer to destroy the pooled object, e.g. Socket::close
	 * @param timeout a pooled object not, in milliseconds
	 */
	public Pool(SupplierEx<? extends T, ? extends E> initializer,
			ConsumerEx<? super T, ?> finalizer, int timeout) {
		this.initializer = initializer;
		this.finalizer = finalizer;
		this.timeout = timeout;
	}

	/**
	 * Borrow an object from the pool, or create a new object
	 * if no valid objects in the pool
	 *
	 * @return {@link Entry} to the pooled object
	 * @throws E exception thrown by initializer
	 */
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
			entry.borrowed = now;
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

	/**
	 * @return number of active (borrowed) entries from the pool
	 */
	public int getActiveCount() {
		return activeCount.get();
	}

	/**
	 * @return number of inactive (returned) entries in the pool
	 */
	public int getInactiveCount() {
		return deque.size();
	}

	/**
	 * Reopen the pool.<p>
	 *
	 * Closing of an entry (must be valid) will return its pooled object
	 * into the reopened pool.
	 */
	public void reopen() {
		closed = false;
	}

	/**
	 * Close the pool.<p>
	 *
	 * Closing of an entry (must be valid) will destroy its pooled object
	 * if the pool is closed.
	 */
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