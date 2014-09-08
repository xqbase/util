package com.xqbase.util;

public abstract class Lazy<T> implements AutoCloseable {
	private volatile T instance = null;

	protected abstract T makeObject();
	protected abstract void destroyObject(T obj);

	public T get() {
		if (instance == null) {
			synchronized (this) {
				if (instance == null) {
					instance = makeObject();
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
				destroyObject(instance_);
			}
		}
	}
}