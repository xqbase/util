package com.xqbase.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Log {
	public static ThreadLocal<String> suffix = new ThreadLocal<>();
	public static ThreadLocal<Throwable> throwable = new ThreadLocal<>();

	private static AtomicReference<Logger> logger_ =
			new AtomicReference<>(Logger.getAnonymousLogger());

	public static Logger getAndSet(Logger logger) {
		return logger_.getAndSet(logger);
	}

	private static final StackTraceElement[] EMPTY_STACK_TRACE = {};
	private static final HashSet<String> THREAD_CLASSES = new HashSet<>(Arrays.
			asList("java.lang.Thread",
			"java.util.concurrent.ThreadPoolExecutor",
			"com.xqbase.util.Runnables"));

	private static void concat(ArrayList<StackTraceElement> stes, Throwable t) {
		for (StackTraceElement ste : t.getStackTrace()) {
			String className = ste.getClassName();
			int dollar = className.indexOf('$');
			if (dollar >= 0) {
				className = className.substring(0, dollar);
			}
			if (!THREAD_CLASSES.contains(className)) {
				stes.add(ste);
			}
		}
	}

	private static Throwable concat(Throwable t) {
		Throwable atop = throwable.get();
		if (atop == null) {
			return t;
		}
		// clone t
		Throwable cloned;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(t);
			ObjectInputStream ois = new ObjectInputStream(new
					ByteArrayInputStream(baos.toByteArray()));
			cloned = (Throwable) ois.readObject();
		} catch (IOException | ReflectiveOperationException e) {
			return t;
		}
		// concatenate t with atop
		ArrayList<StackTraceElement> stes = new ArrayList<>();
		concat(stes, cloned);
		do {
			concat(stes, atop);
			// t_.getCause() is atop t_, see Runnables for more details
			atop = atop.getCause();
		} while (atop != null);
		cloned.setStackTrace(stes.toArray(EMPTY_STACK_TRACE));
		return cloned;
	}

	private static void log(Level l, String s, Throwable t) {
		StackTraceElement ste = new Throwable().getStackTrace()[2];
		String x = suffix.get();
		logger_.get().logp(l, ste.getClassName(), ste.getMethodName(),
				x == null ? s : s + x, t == null ? null : concat(t));
	}

	public static void v(String s) {
		log(Level.FINE, s, null);
	}

	public static void v(Throwable t) {
		log(Level.FINE, "", t);
	}

	public static void v(String s, Throwable t) {
		log(Level.FINE, s, t);
	}

	public static void d(String s) {
		log(Level.CONFIG, s, null);
	}

	public static void d(Throwable t) {
		log(Level.CONFIG, "", t);
	}

	public static void d(String s, Throwable t) {
		log(Level.CONFIG, s, t);
	}

	public static void i(String s) {
		log(Level.INFO, s, null);
	}

	public static void i(Throwable t) {
		log(Level.INFO, "", t);
	}

	public static void i(String s, Throwable t) {
		log(Level.INFO, s, t);
	}

	public static void w(String s) {
		log(Level.WARNING, s, null);
	}

	public static void w(Throwable t) {
		log(Level.WARNING, "", t);
	}

	public static void w(String s, Throwable t) {
		log(Level.WARNING, s, t);
	}

	public static void e(String s) {
		log(Level.SEVERE, s, null);
	}

	public static void e(Throwable t) {
		log(Level.SEVERE, "", t);
	}

	public static void e(String s, Throwable t) {
		log(Level.SEVERE, s, t);
	}
}