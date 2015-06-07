package com.xqbase.util.function;

public interface Supplier<T> extends SupplierEx<T, RuntimeException> {
	@Override
	public T get();
}