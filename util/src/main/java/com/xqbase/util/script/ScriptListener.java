package com.xqbase.util.script;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.xqbase.util.Log;

public class ScriptListener extends ScriptHelper
		implements ServletContextListener, ServletConfig {
	private ServletContext application;

	@Override
	protected void doEval(File file, ScriptContext context)
			throws IOException, ScriptException {
		try (FileReader script = new FileReader(file)) {
			ScriptUtil.getEngine().eval(script, context);
		}
	}

	private void onEvent(String event) {
		String filename = application.getInitParameter(ScriptListener.
				class.getName() + event);
		if (filename == null) {
			return;
		}
		SimpleScriptContext context = new SimpleScriptContext();
		Bindings bindings = ScriptHelper.init(application, filename, context);
		bindings.put("config", this);
		try {
			doEval(context, bindings);
		} catch (IOException | ScriptException e) {
			Log.e(e);
		} finally {
			ScriptHelper.destroy();
		}
	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
		application = event.getServletContext();
		onEvent(".INIT");
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		onEvent(".DESTROY");
	}

	@Override
	public String getServletName() {
		return null;
	}

	@Override
	public ServletContext getServletContext() {
		return application;
	}

	@Override
	public String getInitParameter(String paramString) {
		return application.getInitParameter(paramString);
	}

	@Override
	public Enumeration<String> getInitParameterNames() {
		return application.getInitParameterNames();
	}
}