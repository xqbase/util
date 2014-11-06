package com.xqbase.util;

@FunctionalInterface
public interface SupplierEx<T, E extends Exception> {
	public T get() throws E;

	public default void close(T obj) {
		if (obj instanceof AutoCloseable) {
			try {
				((AutoCloseable) obj).close();
			} catch (Exception e) {/**/}
		}
	}
}