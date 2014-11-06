package com.xqbase.util;

public class Lazy<T, E extends Exception> implements AutoCloseable {
	private SupplierEx<T, E> supplier;
	private volatile T instance = null;

	public Lazy(SupplierEx<T, E> supplier) {
		this.supplier = supplier;
	}

	public T get() throws E {
		if (instance == null) {
			synchronized (this) {
				if (instance == null) {
					instance = supplier.get();
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
				supplier.close(instance_);
			}
		}
	}
}