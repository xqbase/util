package com.xqbase.util;

import com.xqbase.util.function.ConsumerEx;
import com.xqbase.util.function.SupplierEx;

/**
 * A lazy factory for singletons implemented by double-checked locking.
 *
 * @param <T> type of the instance
 * @param <E> type of exception when initializing
 */
public class Lazy<T, E extends Exception> implements AutoCloseable {
	private SupplierEx<? extends T, ? extends E> initializer;
	private ConsumerEx<? super T, ?> finalizer;
	private volatile T instance = null;

	/**
	 * Create a lazy factory by the given initializer and finalizer.
	 *
	 * @param initializer a supplier to create the instance
	 * @param finalizer a consumer to destroy the instance
	 */
	public Lazy(SupplierEx<? extends T, ? extends E> initializer,
			ConsumerEx<? super T, ?> finalizer) {
		this.initializer = initializer;
		this.finalizer = finalizer;
	}

	/**
	 * Create or get the instance.
	 *
	 * @return the instance
	 * @throws E exception thrown by initializer
	 */
	public T get() throws E {
		if (instance == null) {
			synchronized (this) {
				if (instance == null) {
					instance = initializer.get();
				}
			}
		}
		return instance;
	}

	/**
	 * Close the lazy factory and destroy the instance if created.
	 */
	@Override
	public void close() {
		synchronized (this) {
			if (instance != null) {
				T instance_ = instance;
				instance = null;
				try {
					finalizer.accept(instance_);
				} catch (Exception e) {
					// Ignored
				}
			}
		}
	}
}