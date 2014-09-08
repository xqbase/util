package com.xqbase.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Executors {
	static class Command implements Runnable {
		private String suffix = Log.suffix.get();
		// t.getCause() is atop t, see Log.concat() for more details
		private Throwable t = new Throwable(Log.throwable.get());
		private Runnable command;

		Command(Runnable command) {
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
	}

	static int poolSize = 100;
	static int timerPoolSize = 10;
	static String classLoader = Executors.class.getClassLoader().toString();
	static AtomicInteger nextId = new AtomicInteger(0);
	static AtomicInteger threadNum = new AtomicInteger(0);
	static LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();

	public static void setPoolSize(int poolSize) {
		Executors.poolSize = poolSize;
	}

	public static void setTimerPoolSize(int timerPoolSize) {
		Executors.timerPoolSize = timerPoolSize;
	}

	public static int getThreadNum() {
		return threadNum.get();
	}

	public static int getQueuedThreadNum() {
		return queue.size();
	}

	private static Lazy<ThreadPoolExecutor> executor = new Lazy<ThreadPoolExecutor>() {
		@Override
		protected ThreadPoolExecutor makeObject() {
			ThreadPoolExecutor service = new ThreadPoolExecutor(poolSize,
					poolSize, 1, TimeUnit.MINUTES, queue, new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
					return new Thread(r, classLoader + "-" + nextId.incrementAndGet());
				}
			});
			service.allowCoreThreadTimeOut(true);
			return service;
		}

		@Override
		protected void destroyObject(ThreadPoolExecutor executor_) {
			shutdown(executor_);
		}
	};

	private static Lazy<ScheduledThreadPoolExecutor> timer = new Lazy<ScheduledThreadPoolExecutor>() {
		@Override
		protected ScheduledThreadPoolExecutor makeObject() {
			return new ScheduledThreadPoolExecutor(timerPoolSize, new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
					return new Thread(r, "timer-" + classLoader + "-" + nextId.incrementAndGet());
				}
			});
		}

		@Override
		protected void destroyObject(ScheduledThreadPoolExecutor executor_) {
			shutdown(executor_);
		}
	};

	static void shutdown(ExecutorService service) {
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

	public static void execute(Runnable command) {
		executor.get().execute(new Command(command));
	}

	public static void schedule(Runnable command, long delay) {
		timer.get().schedule(new Command(command), delay, TimeUnit.MILLISECONDS);
	}

	public static void schedule(Runnable command, long delay, long peroid) {
		timer.get().scheduleAtFixedRate(new Command(command),
				delay, peroid, TimeUnit.MILLISECONDS);
	}

	public static void shutdown() {
		executor.close();
		timer.close();
	}
}