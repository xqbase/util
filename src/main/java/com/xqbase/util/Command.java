package com.xqbase.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Command implements Runnable {
	private static AtomicInteger threadNum = new AtomicInteger(0);

	public static int getThreadNum() {
		return threadNum.get();
	}

	private String suffix = Log.suffix.get();
	// t.getCause() is atop t, see Log.concat() for more details
	private Throwable t = new Throwable(Log.throwable.get());
	private Runnable command;

	public Command(Runnable command) {
		this.command = command;
	}

	@Override
	public void run() {
		threadNum.incrementAndGet();
		Log.suffix.set(suffix);
		Log.throwable.set(t);
		try {
			command.run();
		} catch (Error | RuntimeException e) {
			Log.e(e);
		} finally {
			Log.throwable.remove();
			Log.suffix.remove();
			threadNum.decrementAndGet();
		}
	}

	public static void shutdown(ExecutorService service) {
		service.shutdown();
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
}