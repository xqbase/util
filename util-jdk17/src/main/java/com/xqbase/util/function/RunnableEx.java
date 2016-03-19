package com.xqbase.util.function;

public interface RunnableEx<E extends Exception> {
	public void run() throws E;
}