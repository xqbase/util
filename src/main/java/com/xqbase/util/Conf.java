package com.xqbase.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Conf {
	public static boolean DEBUG = false;

	private static String confDir = null, logDir = null;

	static {
		for (String argument : ManagementFactory.
				getRuntimeMXBean().getInputArguments()) {
			if (argument.startsWith("-agentlib:jdwp")) {
				DEBUG = true;
				break;
			}
		}

		Properties p = load("Conf");
		confDir = p.getProperty("conf_dir");
		logDir = p.getProperty("log_dir");
	}

	private static void load(Properties p, String path) {
		try (FileInputStream in = new FileInputStream(path)) {
			p.load(in);
		} catch (FileNotFoundException e) {
			// Ignored
		} catch (IOException e) {
			Log.w(e.getMessage());
		}
	}

	private static String getConfPath(String name, String confDir_) {
		return confDir_ == null ? locate("conf/" + name + ".properties") :
				(confDir_.endsWith("/") ? confDir_ : confDir_ + "/") +
				name + ".properties";
	}

	private static Class<?> getParentClass() {
		for (StackTraceElement ste : new Throwable().getStackTrace()) {
			String className = ste.getClassName();
			if (!className.equals(Conf.class.getName())) {
				try {
					return Class.forName(className);
				} catch (ReflectiveOperationException e) {
					Log.e(e);
					return Conf.class;
				}
			}
		}
		return Conf.class;
	}

	public static String locate(String path) {
		Class<?> parentClass = getParentClass();
		String classFile = parentClass.getName().replace('.', '/') + ".class";
		URL url = parentClass.getResource("/" + classFile);
		if (url == null) {
			return path;
		}
		try {
			String root;
			if (url.getProtocol().equals("jar")) {
				root = url.getPath();
				int i = root.lastIndexOf('!');
				if (i >= 0) {
					root = root.substring(0, i);
				}
				root = new File(new URL(root).toURI()).getParent();
			} else {
				root = new File(url.toURI()).getPath();
				root = root.substring(0, root.length() - classFile.length());
			}
			return new File(root + File.separator + ".." +
					File.separator + path).getCanonicalPath();
		} catch (IOException | URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	public static Logger openLogger(String name, int limit, int count) {
		Logger logger = Logger.getAnonymousLogger();
		logger.setLevel(Level.ALL);
		logger.setUseParentHandlers(false);
		if (DEBUG) {
			ConsoleHandler handler = new ConsoleHandler();
			handler.setLevel(Level.ALL);
			logger.addHandler(handler);
		}
		FileHandler handler;
		try {
			String logDir_ = logDir == null ? locate("logs") : logDir;
			new File(logDir_).mkdirs();
			String pattern = (logDir_.endsWith("/") ? logDir_ : logDir_ + "/") +
					name + "%g.log";
			handler = new FileHandler(pattern, limit, count, true);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		handler.setFormatter(new SimpleFormatter());
		handler.setLevel(Level.ALL);
		logger.addHandler(handler);
		return logger;
	}

	public static void closeLogger(Logger logger) {
		for (Handler handler : logger.getHandlers()) {
			logger.removeHandler(handler);
			handler.close();
		}
	}

	public static Properties load(String name) {
		Properties p = new Properties();
		InputStream in = getParentClass().getResourceAsStream("/" + name + ".properties");
		if (in != null) {
			try {
				p.load(in);
			} catch (IOException e) {
				Log.w(e.getMessage());
			}
		}
		load(p, getConfPath(name, null));
		if (confDir != null) {
			load(p, getConfPath(name, confDir));
		}
		return p;
	}

	public static void store(String name, Properties p) {
		try (FileOutputStream out = new FileOutputStream(getConfPath(name, confDir))) {
			p.store(out, null);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static final HashSet<String> TRUE_VALUES =
			new HashSet<>(Arrays.asList("true", "yes", "on", "enable", "enabled"));
	private static final HashSet<String> FALSE_VALUES =
			new HashSet<>(Arrays.asList("false", "no", "off", "disable", "disabled"));

	public static boolean getBoolean(String value, boolean defaultValue) {
		if (value == null) {
			return defaultValue;
		}
		String value_ = value.trim().toLowerCase();
		return defaultValue ? !FALSE_VALUES.contains(value_) : TRUE_VALUES.contains(value_);
	}

	/** @see #getBoolean(String, boolean) */
	@Deprecated
	public static boolean getBoolean(Properties p, String key, boolean defaultValue) {
		String value = p.getProperty(key);
		if (value == null) {
			return defaultValue;
		}
		value = value.trim().toLowerCase();
		return defaultValue ? !FALSE_VALUES.contains(value) : TRUE_VALUES.contains(value);
	}

	private static void search(ArrayList<String> classes, String root, String path) {
		File folder = new File(root + path);
		String[] list = folder.list();
		if (list == null) {
			return;
		}
		for (String file : folder.list()) {
			if (new File(root + path + "/" + file).isDirectory()) {
				search(classes, root, path + "/" + file);
			} else if (file.endsWith(".class")) {
				classes.add(path.replace('/', '.') + "." +
						file.substring(0, file.length() - 6));
			}
		}
	}

	public static ArrayList<String> getClasses(String... packageNames) {
		ArrayList<String> classes = new ArrayList<>();
		for (String packageName : packageNames) {
			String packagePath = packageName.replace('.', '/');
			URL url = Conf.class.getResource("/" + packagePath);
			if (url == null) {
				return classes;
			}
			if (url.getProtocol().equals("jar")) {
				String path = url.getPath();
				int i = path.lastIndexOf('!');
				if (i >= 0) {
					path = path.substring(0, i);
				}
				try (JarFile jar = new JarFile(new File(new URL(path).toURI()))) {
					Enumeration<JarEntry> e = jar.entries();
					while (e.hasMoreElements()) {
						path = e.nextElement().getName();
						if (path.startsWith(packagePath) && path.endsWith(".class")) {
							classes.add(path.substring(0, path.length() - 6).replace('/', '.'));
						}
					}
				} catch (IOException | URISyntaxException e) {
					throw new RuntimeException(e);
				}
			} else {
				try {
					String root = new File(url.toURI()).getPath();
					root = root.substring(0, root.length() - packagePath.length());
					search(classes, root, packagePath);
				} catch (URISyntaxException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return classes;
	}
}