package com.xqbase.util.concurrent;

public class LockMap<K> extends CountMap<K, CountLock> {
	private static final long serialVersionUID = 1L;

	public LockMap() {
		this(false);
	}

	public LockMap(boolean fair) {
		super(() -> new CountLock(fair));
	}
}