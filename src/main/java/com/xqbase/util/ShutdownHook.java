package com.xqbase.util;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import sun.misc.Signal;
import sun.misc.SignalHandler;

public class ShutdownHook implements SignalHandler {
	private static AtomicReference<Thread> mainThread = new AtomicReference<>();

	private AtomicBoolean interrupted = new AtomicBoolean(false);
	private ConcurrentLinkedQueue<Thread> threads = new ConcurrentLinkedQueue<>();
	private ConcurrentLinkedQueue<Runnable> runnables = new ConcurrentLinkedQueue<>();
	private ConcurrentLinkedQueue<Closeable> closeables = new ConcurrentLinkedQueue<>();

	public boolean isShutdown(String[] args) {
		if (args != null && args.length > 0 && args[0].equals("stop")) {
			handle(null);
			return true;
		}
		mainThread.set(Thread.currentThread());
		if (new Throwable().getStackTrace().length == 2) {
			Signal.handle(new Signal("INT"), this);
			Signal.handle(new Signal("TERM"), this);
		}
		return false;
	}

	public boolean isInterrupted() {
		return interrupted.get();
	}

	public void execute(Runnable command) {
		Thread thread = new Thread(command);
		thread.start();
		threads.offer(thread);
	}

	public void postExecute(Runnable runnable) {
		runnables.offer(runnable);
	}

	public void register(Closeable closeable) {
		closeables.offer(closeable);
	}

	@Override
	public void handle(Signal signal) {
		Thread thread = mainThread.get();
		if (thread != null) {
			thread.interrupt();
		}
		interrupted.set(true);
		while ((thread = threads.poll()) != null) {
			thread.interrupt();
		}
		Runnable runnable;
		while ((runnable = runnables.poll()) != null) {
			runnable.run();
		}
		// In case blocked by non-interruptible operations, Main Thread should be:
		//   hook.register(...);
		//   if (Thread.interrupted()) ...
		Closeable closeable;
		while ((closeable = closeables.poll()) != null) {
			try {
				closeable.close();
			} catch (IOException e) {/**/}
		}
	}
}