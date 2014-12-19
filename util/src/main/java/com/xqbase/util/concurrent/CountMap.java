package com.xqbase.util.concurrent;

import java.util.concurrent.ConcurrentHashMap;

public abstract class CountMap<K, V extends Count>
		extends ConcurrentHashMap<K, V> {
	private static final long serialVersionUID = 1L;

	protected abstract V newCount();

	public V acquire(K key) {
		V count = get(key);
		if (count == null) {
			V newCount = newCount();
			count = putIfAbsent(key, newCount);
			if (count == null) {
				return newCount;
			}
		}
		count.incrementAndGet();
		return count;
	}

	public void release(K key, V count) {
		if (count.decrementAndGet() == 0) {
			remove(key, Count.ZERO);
		}
	}
}