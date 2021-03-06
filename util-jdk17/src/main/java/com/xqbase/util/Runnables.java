package com.xqbase.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.xqbase.util.function.Consumer;
import com.xqbase.util.function.RunnableEx;
import com.xqbase.util.function.SupplierEx;

public class Runnables {
	static AtomicInteger threadNum = new AtomicInteger(0);

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

	/**
	 * Wrap a {@link Callable}
	 *
 	 * @see #wrap(Runnable)
	 */
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

	@SuppressWarnings("unchecked")
	private static <T, U extends T> U cast(T o) {
		return (U) o;
	}

	public static <E extends Exception> void retry(final RunnableEx<E>
			runnable, Consumer<E> handler, int count, int interval) throws E {
		retry(new SupplierEx<Void, E>() {
			@Override
			public Void get() throws E {
				runnable.run();
				return null;
			}
		}, handler, count, interval);
	}

	public static <T, E extends Exception> T retry(SupplierEx<T, E> supplier,
			Consumer<E> handler, int count, int interval) throws E {
		for (int i = 0; i < count; i ++) {
			try {
				return supplier.get();
			} catch (Exception e) {
				if (e instanceof RuntimeException) {
					throw e;
				}
				handler.accept(Runnables.<Exception, E>cast(e));
				if (interval > 0) {
					Time.sleep(interval);
				}
			}
		}
		return supplier.get();
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