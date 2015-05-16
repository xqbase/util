package com.xqbase.util.function;

import java.util.Map;

@FunctionalInterface
public interface BiConsumerEx<T, U, E extends Exception> {
	public void accept(T t, U u) throws E;

	public default BiConsumerEx<T, U, E>
			andThen(BiConsumerEx<? super T, ? super U, ? extends E> after) {
		return (t, u) -> {
			accept(t, u);
			after.accept(t, u);
		};
	}

	public static <K, V, E extends Exception> void forEach(Map<K, V> map,
			BiConsumerEx<? super K, ? super V, ? extends E> consumer) throws E {
		for (Map.Entry<K, V> entry : map.entrySet()) {
			consumer.accept(entry.getKey(), entry.getValue());
		}
	}
}