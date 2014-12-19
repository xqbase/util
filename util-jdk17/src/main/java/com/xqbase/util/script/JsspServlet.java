package com.xqbase.util.script;

import java.io.IOException;
import java.io.Reader;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class JsspServlet extends ScriptServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
		response.setContentType("text/html");
		super.service(request, response);
	}

	@Override
	protected void doEval(ScriptEngine engine, Reader reader,
			ScriptContext context) throws IOException, ScriptException {
		engine.eval(ScriptUtil.parseTemplate(reader), context);
	}

	@Override
	protected CompiledScript doCompile(Compilable compilable,
			Reader script) throws IOException, ScriptException {
		return compilable.compile(ScriptUtil.parseTemplate(script));
	}
}