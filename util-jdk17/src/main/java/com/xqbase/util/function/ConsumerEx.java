package com.xqbase.util.function;

public interface ConsumerEx<T, E extends Exception> {
	public void accept(T t) throws E;
}