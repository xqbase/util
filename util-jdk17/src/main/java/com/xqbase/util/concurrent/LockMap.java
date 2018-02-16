package com.xqbase.util.concurrent;

import com.xqbase.util.function.Supplier;

public class LockMap<K> extends CountMap<K, CountLock> {
	private static final long serialVersionUID = 1L;

	public LockMap() {
		this(false);
	}

	public LockMap(final boolean fair) {
		super(new Supplier<CountLock>() {
			@Override
			public CountLock get() {
				return new CountLock(fair);
			}
		});
	}
}