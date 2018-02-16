package com.xqbase.util.concurrent;

import java.util.concurrent.ConcurrentHashMap;

import com.xqbase.util.function.Supplier;

public class CountMap<K, V extends Count> extends ConcurrentHashMap<K, V> {
	private static final long serialVersionUID = 1L;

	private Supplier<V> supplier;

	public CountMap(Supplier<V> supplier) {
		this.supplier = supplier;
	}

	public V acquire(K key) {
		V count = get(key);
		if (count == null) {
			V newCount = supplier.get();
			count = putIfAbsent(key, newCount);
			if (count == null) {
				count = newCount;
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