package com.xqbase.util.function;

public interface BiConsumerEx<T, U, E extends Exception> {
	public void accept(T t, U u) throws E;
}