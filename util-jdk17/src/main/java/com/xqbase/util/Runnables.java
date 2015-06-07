package com.xqbase.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Runnables {
	static AtomicInteger threadNum = new AtomicInteger(0);

	public static int getThreadNum() {
		return threadNum.get();
	}

	public static Runnable wrap(final Runnable runnable) {
		final String suffix = Log.suffix.get();
		// t.getCause() is atop t, see Log.concat() for more details
		final Throwable t = new Throwable(Log.throwable.get());
		return new Runnable() {
			@Override
			public void run() {
				threadNum.incrementAndGet();
				Log.suffix.set(suffix);
				Log.throwable.set(t);
				try {
					runnable.run();
				} catch (Error | RuntimeException e) {
					Log.e(e);
				} finally {
					Log.throwable.remove();
					Log.suffix.remove();
					threadNum.decrementAndGet();
				}
			}
		};
	}

	public static <V> Callable<V> wrap(final Callable<V> callable) {
		final String suffix = Log.suffix.get();
		// t.getCause() is atop t, see Log.concat() for more details
		final Throwable t = new Throwable(Log.throwable.get());
		return new Callable<V>() {
			@Override
			public V call() throws Exception {
				threadNum.incrementAndGet();
				Log.suffix.set(suffix);
				Log.throwable.set(t);
				try {
					return callable.call();
				} catch (Error e) {
					throw new Exception(e);
				} finally {
					Log.throwable.remove();
					Log.suffix.remove();
					threadNum.decrementAndGet();
				}
			}
		};
	}

	private static void awaitTermination(ExecutorService service) {
		boolean interrupted = Thread.interrupted();
		boolean terminated = false;
		while (!terminated) {
			try {
				terminated = service.awaitTermination(1, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				interrupted = true;
			}
		}
		if (interrupted) {
			Thread.currentThread().interrupt();
		}
	}

	public static void shutdown(ExecutorService service) {
		service.shutdown();
		awaitTermination(service);
	}

	public static void shutdownNow(ExecutorService service) {
		service.shutdownNow();
		awaitTermination(service);
	}
}