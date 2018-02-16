package com.xqbase.util.concurrent;

import com.xqbase.util.function.Supplier;

public class SimpleCountMap<K> extends CountMap<K, Count> {
	private static final long serialVersionUID = 1L;

	public SimpleCountMap() {
		super(new Supplier<Count>() {
			@Override
			public Count get() {
				return new Count();
			}
		});
	}
}