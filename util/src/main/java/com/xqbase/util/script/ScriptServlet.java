package com.xqbase.util.script;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicLong;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

class CompiledScriptEx {
	final AtomicLong accessed;
	volatile long modified;
	volatile FutureTask<CompiledScript> compileTask;

	CompiledScriptEx(long now, long modified, FutureTask<CompiledScript> compileTask) {
		accessed = new AtomicLong(now);
		this.modified = modified;
		this.compileTask = compileTask;
	}
}

public class ScriptServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private ScriptHelper helper = new ScriptHelper() {
		private ConcurrentHashMap<String, CompiledScriptEx>
				compiledMap = new ConcurrentHashMap<>();

		private FutureTask<CompiledScript>
				getCompileTask(File file, Compilable engine) {
			return new FutureTask<>(() -> {
				try (FileReader script = new FileReader(file)) {
					return doCompile(engine, script);
				}
			});
		}

		@Override
		protected void doEval(File file, ScriptContext context)
				throws IOException, ScriptException {
			ScriptEngine engine = ScriptUtil.getEngine();
			if (!(engine instanceof Compilable)) {
				// Run script if not compilable
				try (FileReader script = new FileReader(file)) {
					ScriptServlet.this.doEval(engine, script, context);
				}
				return;
			}

			String key = file.getCanonicalPath();
			long now = System.currentTimeMillis();
			CompiledScriptEx compiled = compiledMap.get(key);
			if (compiled == null) {
				CompiledScriptEx newCompiled = new CompiledScriptEx(now,
						file.lastModified(), getCompileTask(file, (Compilable) engine));
				compiled = compiledMap.putIfAbsent(key, newCompiled);
				if (compiled == null) {
					compiled = newCompiled;
					compiled.compileTask.run();
				}
			}
			long accessed = compiled.accessed.get();
			if (now > accessed + 1000 &&
					compiled.accessed.compareAndSet(accessed, now)) {
				long modified = file.lastModified();
				if (compiled.modified != modified) {
					compiled.modified = modified;
					compiled.compileTask = getCompileTask(file, (Compilable) engine);
					// set volatile compileTask, then block ...
					compiled.compileTask.run();
				}
			}

			// Run compiled script
			try {
				compiled.compileTask.get().eval(context);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} catch (ExecutionException e) {
				Throwable t = e.getCause();
				if (t instanceof IOException) {
					throw (IOException) t;
				}
				if (t instanceof ScriptException) {
					throw (ScriptException) t;
				}
				throw new RuntimeException(t);
			}
		}
	};

	public void include(String filename) throws IOException, ScriptException {
		helper.include(filename);
	}

	/** @throws IOException */
	protected void doEval(ScriptEngine engine, Reader script,
			ScriptContext context) throws IOException, ScriptException {
		engine.eval(script, context);
	}

	/** @throws IOException */
	protected CompiledScript doCompile(Compilable compilable, Reader script)
			throws IOException, ScriptException {
		return compilable.compile(script);
	}

	@Override
	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
		Object servletPath = request.
				getAttribute(RequestDispatcher.INCLUDE_SERVLET_PATH);
		String filename = servletPath instanceof String ?
				(String) servletPath : request.getServletPath();
		SimpleScriptContext context = new SimpleScriptContext();
		PrintWriter out = response.getWriter();
		context.setWriter(out);
		Bindings bindings = ScriptHelper.
				init(getServletContext(), filename, context);
		bindings.put("config", this);
		bindings.put("request", request);
		bindings.put("session", request.getSession());
		bindings.put("response", response);
		bindings.put("out", out);
		Enumeration<String> en = request.getAttributeNames();
		while (en.hasMoreElements()) {
			String name = en.nextElement();
			bindings.put(name, request.getAttribute(name));
		}
		try {
			helper.doEval(context, bindings);
		} catch (ScriptException e) {
			throw new ServletException(e);
		} finally {
			ScriptHelper.destroy();
		}
	}
}