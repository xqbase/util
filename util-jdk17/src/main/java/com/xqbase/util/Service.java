package com.xqbase.util;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Create a Windows or Linux service, which can be stopped gracefully.<p>
 *
 * A Java program running as a service, whether in Windows or Linux, can be written as:
 *
 * <pre><code>
 *	private static Service service = new Service();
 *
 *	public static void main(String[] args) {
 *		if (!service.startup(args)) {
 *			return;
 *		}
 *		// initialize
 *		...
 *		while (!Thread.interrupted()) {
 *			// keep service running
 *			...
 *		}
 *		// close resources
 *		...
 *		service.shutdown();
 *	}
 * </code></pre>
 *
 * A Windows service is usually made with the <i>service runner</i> of
 * <a href="http://commons.apache.org/proper/commons-daemon/">Apache Commons Daemon</a>.
 * The <i>service runner</i> will call main method with an argument "stop"
 * in another thread to notify shutdown, which can be caught by {@link #startup(String[])}.
 * So the <b>Service</b> object must be a singleton.<p>
 *
 * A Linux service may receive SIGTERM (kill) and start the JVM's <i>shutdown hook</i>.
 */
public class Service implements Executor {
	CountDownLatch latch = new CountDownLatch(1);
	ExecutorService executor = Executors.newCachedThreadPool();
	AtomicBoolean interrupted = new AtomicBoolean(false);
	Queue<Runnable> shutdownHooks = new ConcurrentLinkedQueue<>();
	Queue<AutoCloseable> closeables = new ConcurrentLinkedQueue<>();

	private Thread shutdownHook = new Thread() {
		@Override
		public void run() {
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
		}
	};

	/**
	 * Execute a {@link Runnable} in the thread pool (created by the service).<p>
	 *
	 * This runnable will be interrupted when the service is stopping.
	 */
	@Override
	public void execute(Runnable command) {
		executor.execute(command);
	}

	/**
	 * Start or stop the service according to:
	 * <dl>
	 * <dd><b>A</b>. If the argument is "stop", it may be a stop notification
	 *		by the <i>service runner</i> and the <i>shutdown hook</i> will be started.</dd>
	 * <dd><b>B</b>. If the main method is not called by the <i>service runner</i>
	 *		(the main method is on the top of stack trace),
	 *		it will add a <i>shutdown hook</i> to catch SIGTERM.</dd>
	 * <dd><b>C</b>. Otherwise (may be called by the <i>service runner</i>) it will do nothing.</dd>
	 * </dl>
	 *
	 * @param args arguments in the main method
	 * @return <code>false</code> for case <b>A</b> and <code>true</code> for case <b>B</b> or <b>C</b> 
	 */
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

	/**
	 * The <i>shutdown hook</i> will be suspended until <code>shutdown()</code> is called.
	 * This can prevent the main thread being killed before closing resources.
	 */
	public void shutdown() {
		latch.countDown();
	}

	/**
	 * Enforce to stop the service.
	 */
	public void shutdownNow() {
		if (!isInterrupted()) {
			Runtime.getRuntime().removeShutdownHook(shutdownHook);
			shutdownHook.start();
		}
	}

	/**
	 * Check whether the service is stopping.<p>
	 *
	 * In the main thread, this can be replaced with <code>Thread.interrupted()</code> or
	 * <code>Thread.currentThread().isInterrupted()</code>.
	 * 
	 * @return <code>true</code> when the service is stopping
	 */
	public boolean isInterrupted() {
		return interrupted.get();
	}

	/**
	 * Add a {@link Runnable} (NOT the JVM's <i>shutdown hook</i>) into the queue
	 * which will be run when the service is stopping. 
	 */
	public void addShutdownHook(Runnable runnable) {
		shutdownHooks.offer(runnable);
	}

	/**
	 * Add an {@link AutoCloseable} into the queue
	 * which will be closed when the service is stopping.
	 */
	public void register(AutoCloseable closeable) {
		closeables.offer(closeable);
	}
}