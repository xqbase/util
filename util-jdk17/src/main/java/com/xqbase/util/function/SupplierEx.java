package com.xqbase.util.function;

public interface SupplierEx<T, E extends Exception> {
	public T get() throws E;
}