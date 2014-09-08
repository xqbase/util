package com.xqbase.util.concurrent;

public class SimpleCountMap<K> extends CountMap<K, Count> {
	private static final long serialVersionUID = 1L;

	@Override
	protected Count newCount() {
		return new Count();
	}
}