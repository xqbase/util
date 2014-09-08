package com.xqbase.util.script;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class ScriptUtil {
	private static final int TAG_NONE = 0;
	private static final int TAG_SCRIPT = 1;
	private static final int TAG_OUTPUT = 2;
	private static final int TAG_COMMENTS = 3;

	private static ScriptEngine engine = new ScriptEngineManager().
			getEngineByMimeType("text/javascript");

	public static ScriptEngine getEngine() {
		return engine;
	}

	public static String parseTemplate(Reader reader) throws IOException {
		StringBuilder sb = new StringBuilder();
		int status = TAG_NONE;
		BufferedReader in = new BufferedReader(reader);
		String line;
		while ((line = in.readLine()) != null) {
			int offset = 0;
			if (status == TAG_NONE) {
				sb.append("print(\"");
			}

			while (true) {
				if (status == TAG_NONE) {
					// Out of Tag
					int index = line.indexOf("<%", offset);
					// "<%" not found: Write whole line
					if (index < 0) {
						sb.append(line.substring(offset).
								replace("\\", "\\\\").replace("\"", "\\\""));
						sb.append("\\n\");");
						break;
					}
					// "<%" found: Write to open tag
					sb.append(line.substring(offset, index).
							replace("\\", "\\\\").replace("\"", "\\\""));
					sb.append("\");");
					if (index + 2 == line.length()) {
						status = TAG_SCRIPT;
						break;
					}
					char c = line.charAt(index + 2);
					if (c == '=') {
						sb.append("print(");
						status = TAG_OUTPUT;
						offset = index + 3;
					} else if (c == '-') {
						status = TAG_COMMENTS;
						offset = index + 3;
					} else {
						status = TAG_SCRIPT;
						offset = index + 2;
					}
				} else if (status == TAG_COMMENTS) {
					// In Remark
					int index = line.indexOf("-%>", offset);
					// "-%>" not found: Continue write script
					if (index < 0) {
						break;
					}
					// "-%>" found: Exit tag
					sb.append("print(\"");
					status = TAG_NONE;
					offset = index + 3;
				} else {
					// In Tag
					int index = line.indexOf("%>", offset);
					// "%>" not found: Continue write script
					if (index < 0) {
						sb.append(line.substring(offset));
						break;
					}
					// "%>" found: Exit tag
					sb.append(line.substring(offset, index));
					if (status == TAG_OUTPUT) {
						sb.append(");");
					}
					sb.append("print(\"");
					status = TAG_NONE;
					offset = index + 2;
				}
			}
			sb.append("\n");
		}
		return sb.toString();
	}
}