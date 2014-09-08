package com.xqbase.util;

import java.util.Iterator;
import java.util.LinkedHashMap;

public class TimeoutMap<K, V> {
	class TimeoutEntry {
		V value;
		long expire;
	}

	private long accessed = 0;
	private int timeout, interval;
	private boolean accessOrder;
	private LinkedHashMap<K, TimeoutEntry> map;

	public TimeoutMap(int timeout, int interval) {
		this(timeout, interval, false);
	}

	public TimeoutMap(int timeout, int interval, boolean accessOrder) {
		this.timeout = timeout;
		this.interval = interval;
		this.accessOrder = accessOrder;
		map = new LinkedHashMap<>(16, 0.75f, accessOrder);
	}

	private V get_(K key) {
		TimeoutEntry entry = map.get(key);
		if (entry == null) {
			return null;
		}
		if (accessOrder) {
			entry.expire = System.currentTimeMillis() + timeout;
		}
		return entry.value;
	}

	private void put_(K key, V value) {
		TimeoutEntry entry = new TimeoutEntry();
		entry.value = value;
		entry.expire = System.currentTimeMillis() + timeout;
		map.put(key, entry);
	}

	private V remove_(K key) {
		TimeoutEntry entry = map.remove(key);
		return entry == null ? null : entry.value;
	}

	private void expire() {
		long now = System.currentTimeMillis();
		if (now < accessed + interval) {
			return;
		}
		accessed = now;
		Iterator<TimeoutEntry> i = map.values().iterator();
		while (i.hasNext() && now > i.next().expire) {
			i.remove();
		}
	}

	public synchronized V get(K key) {
		return get_(key);
	}

	public synchronized V expireAndGet(K key) {
		expire();
		return get_(key);
	}

	public synchronized void put(K key, V value) {
		put_(key, value);
	}

	public synchronized void expireAndPut(K key, V value) {
		expire();
		put_(key, value);
	}

	public synchronized V remove(K key) {
		return remove_(key);
	}

	public synchronized V expireAndRemove(K key) {
		expire();
		return remove_(key);
	}
}