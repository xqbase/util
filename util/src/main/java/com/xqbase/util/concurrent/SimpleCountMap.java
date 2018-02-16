package com.xqbase.util.concurrent;

public class SimpleCountMap<K> extends CountMap<K, Count> {
	private static final long serialVersionUID = 1L;

	public SimpleCountMap() {
		super(Count::new);
	}
}