package com.xqbase.util.concurrent;

import java.util.concurrent.atomic.AtomicInteger;

public class Count extends AtomicInteger {
	private static final long serialVersionUID = 1L;

	public static final Count ZERO = new Count(0);

	private Count(int initialValue) {
		super(initialValue);
	}

	protected Count() {
		this(1);
	}

	@Override
	public int hashCode() {
		return (get() == 0 ? 0 : super.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == ZERO) {
			return get() == 0;
		}
		if (this == ZERO) {
			return ((Count) obj).get() == 0;
		}
		return super.equals(obj);
	}
}