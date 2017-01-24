package com.xqbase.util.function;

import java.util.function.Function;

public class TestRecursive {
	private static int fibonacci(int n) {
		return n <= 1 ? 1 : fibonacci(n - 1) + fibonacci(n - 2);
	}

	private static final int TEST_INPUT = 45;

	@SuppressWarnings("boxing")
	public static void main(String[] args) {
		long t0 = System.currentTimeMillis();

		int result = fibonacci(TEST_INPUT);
		long t1 = System.currentTimeMillis();
		System.out.println("Fibonacci(" + TEST_INPUT + ")=" + result +
				", with Recursive: " + (t1 - t0) + "ms");

		Function<Integer, Integer> fibonacci = Recursive.wrap((self, n) ->
				n <= 1 ? 1 : self.apply(self, n - 1) + self.apply(self, n - 2));
		result = fibonacci.apply(TEST_INPUT);
		long t2 = System.currentTimeMillis();
		System.out.println("Fibonacci(" + TEST_INPUT + ")=" + result +
				", with Recursive Lambda: " + (t2 - t1) + "ms");

		fibonacci = Recursive.generate((self, n) ->
				n <= 1 ? 1 : self.apply(n - 1) + self.apply(n - 2));
		result = fibonacci.apply(TEST_INPUT);
		long t3 = System.currentTimeMillis();
		System.out.println("Fibonacci(" + TEST_INPUT + ")=" + result +
				", with Recursive Lambda and Y-Combinator: " + (t3 - t2) + "ms");
	}
}