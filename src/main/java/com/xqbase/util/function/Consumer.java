package com.xqbase.util.function;

@FunctionalInterface
public interface Consumer<T> extends ConsumerEx<T, RuntimeException> {
	@Override
	public void accept(T t);
}