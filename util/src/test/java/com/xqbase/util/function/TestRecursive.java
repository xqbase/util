package com.xqbase.util.function;

import java.util.function.Function;

public class TestRecursive {
	@SuppressWarnings("boxing")
	public static void main(String[] args) {
		Function<Integer, Integer> fibonacci = Recursive.wrap((self, n) ->
				n <= 1 ? 1 : self.apply(self, n - 1) + self.apply(self, n - 2));
		for (int i = 0; i < 16; i ++) {
			System.out.print(fibonacci.apply(i) + " ");
		}
		System.out.println();

		fibonacci = Recursive.generate((self, n) ->
				n <= 1 ? 1 : self.apply(n - 1) + self.apply(n - 2));
		for (int i = 0; i < 16; i ++) {
			System.out.print(fibonacci.apply(i) + " ");
		}
		System.out.println();
	}
}