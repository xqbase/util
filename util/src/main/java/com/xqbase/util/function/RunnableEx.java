package com.xqbase.util.function;

@FunctionalInterface
public interface RunnableEx<E extends Exception> {
	public void run() throws E;
}