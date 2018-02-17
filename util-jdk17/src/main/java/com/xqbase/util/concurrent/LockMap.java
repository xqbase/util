package com.xqbase.util.concurrent;

public class LockMap<K> extends CountMap<K> {
	private static final long serialVersionUID = 1L;

	private boolean fair;

	@Override
	protected CountLock newCount() {
		return new CountLock(fair);
	}

	public LockMap() {
		this(false);
	}

	public LockMap(boolean fair) {
		this.fair = fair;
	}

	@Override
	public CountLock acquire(K key) {
		return (CountLock) super.acquire(key);
	}
}