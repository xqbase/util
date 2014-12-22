package com.xqbase.util;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class Service implements Executor {
	private CountDownLatch latch = new CountDownLatch(1);
	private ExecutorService executor = Executors.newCachedThreadPool();
	private AtomicBoolean interrupted = new AtomicBoolean(false);
	private ConcurrentLinkedQueue<Runnable> shutdownHooks = new ConcurrentLinkedQueue<>();
	private ConcurrentLinkedQueue<AutoCloseable> closeables = new ConcurrentLinkedQueue<>();
	private Thread shutdownHook = new Thread(() -> {
		interrupted.set(true);
		Runnable runnable;
		while ((runnable = shutdownHooks.poll()) != null) {
			runnable.run();
		}
		AutoCloseable closeable;
		while ((closeable = closeables.poll()) != null) {
			try {
				closeable.close();
			} catch (Exception e) {/**/}
		}
		Runnables.shutdownNow(executor);
		try {
			latch.await();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	});

	@Override
	public void execute(Runnable command) {
		executor.execute(command);
	}

	public boolean startup(String[] args) {
		if (args != null && args.length > 0 && args[0].equals("stop")) {
			shutdownHook.run();
			return false;
		}
		// Do not call addShutdownHook inside Apache Commons Daemon Service Runner
		if (new Throwable().getStackTrace().length == 2) {
			Runtime.getRuntime().addShutdownHook(shutdownHook);
		}
		return true;
	}

	public void shutdown() {
		latch.countDown();
	}

	public void shutdownNow() {
		if (!isInterrupted()) {
			Runtime.getRuntime().removeShutdownHook(shutdownHook);
			shutdownHook.start();
		}
	}

	public boolean isInterrupted() {
		return interrupted.get();
	}

	public void addShutdownHook(Runnable runnable) {
		shutdownHooks.offer(runnable);
	}

	public void register(AutoCloseable closeable) {
		closeables.offer(closeable);
	}
}