package com.xqbase.util.concurrent;

public class LockMap<K> extends CountMap<K, CountLock> {
	private static final long serialVersionUID = 1L;

	private boolean fair;

	public LockMap() {
		this(false);
	}

	public LockMap(boolean fair) {
		this.fair = fair;
	}

	@Override
	protected CountLock newCount() {
		return new CountLock(fair);
	}
}