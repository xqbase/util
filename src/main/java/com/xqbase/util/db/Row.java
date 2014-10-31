package com.xqbase.util.db;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Row {
	private Object[] data;

	Row(ResultSet rs) throws SQLException {
		data = new Object[rs.getMetaData().getColumnCount()];
		for (int i = 0; i < data.length; i ++) {
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
	}

	public Object get(int column) {
		return data[column - 1];
	}

	public int getInt(int column) {
		Number n = (Number) get(column);
		return n == null ? 0 : n.intValue();
	}

	public String getString(int column) {
		return (String) get(column);
	}

	public byte[] getBytes(int column) {
		return (byte[]) get(column);
	}

	/** Available until Year 2106 */
	public long getTime(int column) {
		return (getInt(column) & 0xFFFFFFFFL) * 1000;
	}

	/** Available until Year 2106 */
	public static int setTime(long time) {
		return (int) (time / 1000);
	}

	/** Available until Year 2106 */
	public static Integer setTimeEx(long time) {
		return Integer.valueOf(setTime(time));
	}

	/** Available until Year 2106 */
	public static int now() {
		return setTime(System.currentTimeMillis());
	}

	/** Available until Year 2106 */
	public static Integer nowEx() {
		return Integer.valueOf(now());
	}
}