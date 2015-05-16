package com.xqbase.util.function;

public interface BiConsumer<T, U> extends BiConsumerEx<T, U, RuntimeException>{
	@Override
	public void accept(T t, U u);
}