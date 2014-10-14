package com.xqbase.util.db;

import java.io.PrintStream;

import com.xqbase.util.Bytes;

public class DumpCallback implements RowCallback, AutoCloseable {
	private static final int ROWS_PER_LINE = 256;

	private int rows = 0, columns;
	private PrintStream out;
	private String sql;

	public DumpCallback(PrintStream out, String table, String... columns) {
		this.out = out;
		this.columns = columns.length;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < columns.length; i ++) {
			sb.append(", " + columns[i]);
		}
		sql = "INSERT INTO " + table + " (" + sb.substring(2) + ") VALUES ";
		out.println("TRUNCATE TABLE " + table + ";");
	}

	@Override
	public void onRow(Row row) {
		if (rows % ROWS_PER_LINE == 0) {
			if (rows > 0) {
				out.println(";");
			}
			out.print(sql);
		} else {
			out.print(", ");
		}
		rows ++;

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < columns; i ++) {
			Object o = row.get(i + 1);
			if (o == null) {
				sb.append(", NULL");
			} else if (o instanceof String) {
				sb.append(", '" + ((String) o).replace("\\", "\\\\").
						replace("\'", "\\\'") + "'");
			} else if (o instanceof byte[]) {
				sb.append(", x'" + Bytes.toHexUpper((byte[]) o) + "'");
			} else { // Number
				sb.append(", " + o);
			}
		}
		out.print("(" + sb.substring(2) + ")");
	}

	@Override
	public void close() {
		out.println(";");
	}
}