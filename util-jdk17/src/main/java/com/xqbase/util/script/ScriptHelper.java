package com.xqbase.util.script;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayDeque;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.servlet.ServletContext;

class Stack extends ArrayDeque<String> {
	private static final long serialVersionUID = 1L;
}

public abstract class ScriptHelper {
	private static final String STACK = ScriptEngine.FILENAME + ".stack";
	private static final String APPLICATION = "application";

	private static ThreadLocal<ScriptContext> __context__ = new ThreadLocal<>();

	public static Bindings init(ServletContext application,
			String filename, ScriptContext context) {
		__context__.set(context);
		Bindings bindings = context.getBindings(ScriptContext.ENGINE_SCOPE);
		bindings.put(ScriptEngine.FILENAME, filename);
		bindings.put(STACK, new Stack());
		bindings.put(APPLICATION, application);
		return bindings;
	}

	public static void destroy() {
		__context__.remove();
	}

	protected abstract void doEval(File file, ScriptContext context)
			throws IOException, ScriptException;

	public void include(String filename) throws IOException, ScriptException {
		ScriptContext context = __context__.get();
		Bindings bindings = context.getBindings(ScriptContext.ENGINE_SCOPE);
		Stack stack = (Stack) bindings.get(STACK);
		stack.push((String) bindings.get(ScriptEngine.FILENAME));
		String filename_;
		if (filename.length() > 0 && filename.charAt(0) == '/') {
			filename_ = filename;
		} else {
			String parent = stack.peek();
			int index = parent.lastIndexOf('/');
			filename_ = index < 0 ? filename :
					parent.substring(0, index + 1) + filename;
		}
		bindings.put(ScriptEngine.FILENAME, filename_);
		doEval(context, bindings);
		bindings.put(ScriptEngine.FILENAME, stack.pop());
	}

	public void doEval(ScriptContext context, Bindings bindings)
			throws IOException, ScriptException {
		String filename = (String) bindings.get(ScriptEngine.FILENAME);
		File file = new File(((ServletContext) bindings.
				get(APPLICATION)).getRealPath(filename));
		if (!file.exists()) {
			throw new FileNotFoundException(filename);
		}
		try {
			doEval(file, context);
		} catch (ScriptException e) {
			Throwable t = e.getCause();
			if (t == null) {
				throw e;
			}
			throw new ScriptException(t.getMessage(), filename,
					e.getLineNumber(), e.getColumnNumber());
		}
	}
}