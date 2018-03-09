package com.xqbase.util.db;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class Row {
	private Object[] data;
	private HashMap<String, Integer> columnMap;

	Row(ResultSet rs, int columnCount,
			HashMap<String, Integer> columnMap) throws SQLException {
		data = new Object[columnCount];
		for (int i = 0; i < columnCount; i ++) {
			Object o = rs.getObject(i + 1);
			if (o instanceof Blob) {
				Blob b = (Blob) o;
				o = b.getBytes(1, (int) b.length());
			} else if (o instanceof Clob) {
				Clob c = (Clob) o;
				o = c.getSubString(1, (int) c.length());
			}
			data[i] = o;
		}
		this.columnMap = columnMap;
	}

	public Object get(int column) {
		return data[column - 1];
	}

	public Object get(String column) {
		Integer column_ = columnMap.get(column.toLowerCase());
		if (column_ == null) {
			throw new IllegalArgumentException("Unknown column \"" +
					column + "\"");
		}
		return data[column_.intValue()];
	}

	public int getInt(int column) {
		Number n = (Number) get(column);
		return n == null ? 0 : n.intValue();
	}

	public int getInt(String column) {
		Number n = (Number) get(column);
		return n == null ? 0 : n.intValue();
	}

	public long getLong(int column) {
		Number n = (Number) get(column);
		return n == null ? 0 : n.longValue();
	}

	public long getLong(String column) {
		Number n = (Number) get(column);
		return n == null ? 0 : n.longValue();
	}

	public String getString(int column) {
		return (String) get(column);
	}

	public String getString(String column) {
		return (String) get(column);
	}

	public byte[] getBytes(int column) {
		return (byte[]) get(column);
	}

	public byte[] getBytes(String column) {
		return (byte[]) get(column);
	}

	public static Long now() {
		return Long.valueOf(System.currentTimeMillis());
	}
}