package com.xqbase.util.function;

@FunctionalInterface
public interface SupplierEx<T, E extends Exception> {
	public T get() throws E;
}