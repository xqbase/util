package com.xqbase.util;

import com.xqbase.util.function.ConsumerEx;
import com.xqbase.util.function.SupplierEx;

public class Lazy<T, E extends Exception> implements AutoCloseable {
	private SupplierEx<? extends T, ? extends E> initializer;
	private ConsumerEx<? super T, ?> finalizer;
	private volatile T instance = null;

	public Lazy(SupplierEx<? extends T, ? extends E> initializer,
			ConsumerEx<? super T, ?> finalizer) {
		this.initializer = initializer;
		this.finalizer = finalizer;
	}

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