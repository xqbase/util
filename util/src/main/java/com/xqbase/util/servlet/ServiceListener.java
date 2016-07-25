package com.xqbase.util.servlet;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.xqbase.util.Runnables;

public class ServiceListener implements ServletContextListener {
	public static final String SERVICES =
			ServiceListener.class.getName() + ".services";

	private static final String[] START_ARGS = {};
	private static final String[] STOP_ARGS = {"stop"};

	private static AtomicInteger count = new AtomicInteger(0);
	private static ArrayList<Class<?>> classes;
	private static ExecutorService executor;

	@Override
	public void contextInitialized(ServletContextEvent event) {
		if (count.getAndIncrement() > 0) {
			return;
		}

		classes = new ArrayList<>();
		executor = Executors.newCachedThreadPool();
		ServletContext context = event.getServletContext();
		String services = context.getInitParameter(SERVICES);
		if (services == null) {
			return;
		}
		for (String s : services.split(",")) {
			try {
				Class<?> clazz = Class.forName(s);
				Method method = clazz.getMethod("main", String[].class);
				classes.add(clazz);
				executor.execute(() -> {
					try {
						method.invoke(null, (Object) START_ARGS);
					} catch (InvocationTargetException e) {
						context.log(s, e.getTargetException());
					} catch (ReflectiveOperationException e) {
						context.log(s + ": " + e.getMessage());
					}
				});
			} catch (ReflectiveOperationException e) {
				context.log(s + ": " + e.getMessage());
			}
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		if (count.decrementAndGet() > 0) {
			return;
		}

		ServletContext context = event.getServletContext();
		for (int i = classes.size() - 1; i >= 0; i --) {
			try {
				classes.get(i).getMethod("main", String[].class).
						invoke(null, (Object) STOP_ARGS);
			} catch (InvocationTargetException e) {
				context.log(classes.get(i).getName(), e.getTargetException());
			} catch (ReflectiveOperationException e) {
				context.log(classes.get(i) + ": " + e.getMessage());
			}
		}
		Runnables.shutdown(executor);
	}
}