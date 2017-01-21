package com.xqbase.util.function;

import java.util.function.BiFunction;
import java.util.function.Function;

@FunctionalInterface
interface Generator<T> extends Function<Generator<T>, Function<T, T>> {/**/}

@FunctionalInterface
public interface Recursive<T> extends BiFunction<Recursive<T>, T, T> {
	public static <T> Function<T, T> wrap(Recursive<T> func) {
		return t -> func.apply(func, t);
	}

	public static <T> Function<T, T> generate(BiFunction<Function<T, T>, T, T> func) {
		Generator<T> gen = self -> t -> func.apply(self.apply(self), t);
		return gen.apply(gen);
	}
}