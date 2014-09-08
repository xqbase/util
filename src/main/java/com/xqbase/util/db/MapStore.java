package com.xqbase.util.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.xqbase.util.LinkedEntry;
import com.xqbase.util.Log;

class Value extends LinkedEntry {
	static final long MAX_INACTIVE_INTERVAL = 60000;

	long timeout;
	String key;
	byte[] data;

	Value(String key, byte[] data) {
		timeout = System.currentTimeMillis() + MAX_INACTIVE_INTERVAL;
		this.key = key;
		this.data = data;
	}
}

public class MapStore implements AutoCloseable {
	private static String match(String prefix) {
		return prefix.replace("%", "\\%").replace("_", "\\_") + '%';
	}

	private ConnectionPool db;
	private String table;
	private TreeMap<String, Value> valueMap = new TreeMap<>();
	private Value header = new Value(null, null);
	private long accessed = System.currentTimeMillis();

	public MapStore(ConnectionPool db, String table) {
		this.db = db;
		this.table = table;
		updateIgnore("DROP TABLE IF EXISTS " + table);
		updateIgnore("CREATE TABLE " + table + " (name VARCHAR(255) " +
				"PRIMARY KEY NOT NULL, value LONGBLOB NOT NULL)");
	}

	@Override
	public void close() {
		updateIgnore("DROP TABLE " + table);
	}

	private void updateIgnore(String sql, Object... s) {
		try {
			db.updateEx(sql, s);
		} catch (SQLException e) {
			Log.e(e);
		}
	}

	private Set<String> keySet0(String prefix) {
		return valueMap.subMap(prefix, prefix + Character.MAX_VALUE).keySet();
	}

	public TreeSet<String> keySet() {
		return keySet(null);
	}

	public TreeSet<String> keySet(String prefix) {
		final TreeSet<String> keySet;
		synchronized (this) {
			if (prefix == null) {
				keySet = new TreeSet<>(valueMap.keySet());
			} else {
				keySet = new TreeSet<>(keySet0(prefix));
			}
		}
		RowCallback callback = new RowCallback() {
			@Override
			public void onRow(Row row) {
				keySet.add(row.getString(1));
			}
		};
		try {
			if (prefix == null) {
				String sql = "SELECT name FROM " + table;
				db.queryEx(callback, sql);
			} else {
				String sql = "SELECT name FROM " + table + " WHERE name LIKE ?";
				db.queryEx(callback, sql, match(prefix));
			}
		} catch (SQLException e) {
			Log.e(e);
		}
		return keySet;
	}

	private void delete(String key) {
		String sql = "DELETE FROM " + table + " WHERE name = ?";
		updateIgnore(sql, key);
	}

	public boolean containsKey(String key) {
		synchronized (this) {
			if (valueMap.containsKey(key)) {
				return true;
			}
		}
		try {
			String sql = "SELECT name FROM " + table + " WHERE name = ?";
			return db.queryEx(sql, key) != null;
		} catch (SQLException e) {
			Log.e(e);
			return false;
		}
	}

	public byte[] get(String key) {
		synchronized (this) {
			Value value = valueMap.get(key);
			if (value != null) {
				value.remove();
				value.timeout = System.currentTimeMillis() + Value.MAX_INACTIVE_INTERVAL;
				header.addPrev(value);
				return value.data;
			}
		}
		Row row;
		try {
			String sql = "SELECT value FROM " + table + " WHERE name = ?";
			row = db.queryEx(sql, key);
		} catch (SQLException e) {
			Log.e(e);
			return null;
		}
		if (row == null) {
			return null;
		}
		byte[] data = row.getBytes(1);
		delete(key);
		// Check whether a new value is put during DB execution
		synchronized (this) {
			Value value = valueMap.get(key);
			if (value != null) {
				// Unnecessary: value.timeout = ...
				return value.data;
			}
			value = new Value(key, data);
			valueMap.put(key, value);
			header.addPrev(value);
		}
		return data;
	}

	public void put(String key, byte[] data) {
		synchronized (this) {
			long now = System.currentTimeMillis();
			if (now > accessed + 60000) {
				accessed = now;
				Value value;
				while ((value = (Value) header.getNext()) != header && now > value.timeout) {
					valueMap.remove(value.key);
					value.remove();
					// Synchronized DB updating
					String sql = "INSERT INTO " + table + " (name, value) VALUES (?, ?)";
					updateIgnore(sql, value.key, value.data);
				}
			}

			Value newValue = new Value(key, data);
			header.addPrev(newValue);
			Value value = valueMap.put(key, newValue);
			if (value != null) {
				value.remove();
				return;
			}
		}
		delete(key);
	}

	public void remove(String key) {
		synchronized (this) {
			Value value = valueMap.remove(key);
			if (value != null) {
				value.remove();
				return;
			}
		}
		delete(key);
	}

	public void clear() {
		synchronized (this) {
			valueMap.clear();
			header.clear();
		}
		String sql = "DELETE FROM " + table;
		updateIgnore(sql);
	}

	public void clear(String prefix) {
		synchronized (this) {
			for (String key : new ArrayList<>(keySet0(prefix))) {
				valueMap.remove(key).remove();
			}
		}
		String sql = "DELETE FROM " + table + " WHERE name LIKE ?";
		updateIgnore(sql, match(prefix));
	}
}