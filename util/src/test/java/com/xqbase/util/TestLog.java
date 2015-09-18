package com.xqbase.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class TestLog {
	public static void main(String[] args) {
		System.setProperty("java.util.logging.SimpleFormatter.format",
				"%1$tY-%1$tm-%1$td %1$tk:%1$tM:%1$tS.%1$tL %2$s%n%4$s: %5$s%6$s%n");
		Logger logger = Log.getAndSet(Conf.openLogger("Test", 1048576, 10));
		Log.suffix.set(" [Test Suffix]");
		Log.i("Started");
		ExecutorService executor = Executors.newCachedThreadPool();
		executor.execute(Runnables.wrap(() -> { // Line 15
			Time.sleep(2000);
			try {
				throw new Exception(); // Line 18
			} catch (Exception e) {
				Log.e("Exception Thrown in Branch Thread", e);
			}
		}));
		Time.sleep(1000);
		Log.i("Number of Branch Threads: " + Runnables.getThreadNum());
		Runnables.shutdown(executor);
		Log.i("Stopped");
		Conf.closeLogger(Log.getAndSet(logger));
	}
}