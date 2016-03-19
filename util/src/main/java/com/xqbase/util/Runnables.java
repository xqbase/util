package com.xqbase.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.xqbase.util.function.RunnableEx;

public class Runnables {
	private static AtomicInteger threadNum = new AtomicInteger(0);

	/**
	 * @return Number of wrapped and active <b>branch thread</b>
	 * @see #wrap(Runnable)
	 */
	public static int getThreadNum() {
		return threadNum.get();
	}

	/**
	 * Wrap a {@link Runnable} in order to:
	 * <ul>
	 * <li>Make the logging suffix in <b>branch thread</b> (callee thread)
	 *		the same as <b>trunk thread</b> (caller thread)</li>
	 * <li>Make the logging stack trace in <b>branch thread</b>
	 *		concatenating with <b>trunk thread</b></li>
 	 * <li>Count number of <b>branch thread</b>s</li>
 	 * </ul>
 	 *
 	 * @see Log#suffix
 	 * @see Log#throwable
	 */
	public static Runnable wrap(Runnable runnable) {
		String suffix = Log.suffix.get();
		// t.getCause() is atop t, see Log.concat() for more details
		Throwable t = new Throwable(Log.throwable.get());
		return () -> {
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
		};
	}

	/**
	 * Wrap a {@link Callable}
	 *
 	 * @see #wrap(Runnable)
	 */
	public static <V> Callable<V> wrap(Callable<V> callable) {
		String suffix = Log.suffix.get();
		// t.getCause() is atop t, see Log.concat() for more details
		Throwable t = new Throwable(Log.throwable.get());
		return () -> {
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
		};
	}

	public static <E extends Exception> void retry(RunnableEx<E> runnable,
			int count, int interval) throws E {
		for (int i = 0; i < count; i ++) {
			try {
				runnable.run();
				return;
			} catch (Exception e) {
				if (e instanceof RuntimeException) {
					throw e;
				}
				Time.sleep(interval);
			}
		}
		runnable.run();
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

	/**
	 * Shutdown and wait for a {@link ExecutorService} like {@link ExecutorService#shutdown()}
	 * and {@link ExecutorService#awaitTermination(long, TimeUnit)} but ignore interruption<p>
	 * The <i>interrupted status</i> will not be cleared if current thread is interrupted during shutdown
	 */
	public static void shutdown(ExecutorService service) {
		service.shutdown();
		awaitTermination(service);
	}

	/**
	 * Shutdown immediately and wait for a {@link ExecutorService} like {@link ExecutorService#shutdownNow()}
	 * and {@link ExecutorService#awaitTermination(long, TimeUnit)} but ignore interruption<p>
	 * The <i>interrupted status</i> will not be cleared if current thread is interrupted during shutdown
	 */
	public static void shutdownNow(ExecutorService service) {
		service.shutdownNow();
		awaitTermination(service);
	}
}