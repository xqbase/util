package com.xqbase.util.db;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Properties;

import com.xqbase.util.ByteArrayQueue;
import com.xqbase.util.Log;
import com.xqbase.util.Pool;
import com.xqbase.util.Streams;

class SingleRowException extends Exception {
	private static final long serialVersionUID = 1L;
}

public class ConnectionPool extends Pool<Connection, SQLException> {
	private static Object[] valueOf(int... values) {
		Object[] objs = new Object[values.length];
		for (int i = 0; i < values.length; i ++) {
			objs[i] = Integer.valueOf(values[i]);
		}
		return objs;
	}

	private Driver driver;
	private String url;
	private Properties info = new Properties();

	public ConnectionPool(Driver driver, String url) {
		this(driver, url, null, null);
	}

	public ConnectionPool(Driver driver, String url, String user, String password) {
		super(60000);
		this.driver = driver;
		this.url = url;
		if (user != null) {
			info.put("user", user);
		}
		if (password != null) {
			info.put("password", password);
		}
	}

	@Override
	protected Connection makeObject() throws SQLException {
		return driver.connect(url, info);
	}

	@Override
	protected void destroyObject(Connection conn) {
		try {
			conn.close();
		} catch (SQLException e) {/**/}
	}

	public int update(String sql, int... in) throws SQLException {
		return updateEx(sql, valueOf(in));
	}

	public int update(int[] insertId, String sql,
			int... in) throws SQLException {
		return updateEx(insertId, sql, valueOf(in));
	}

	public int update(long[] insertId, String sql,
			int... in) throws SQLException {
		return updateEx(insertId, sql, valueOf(in));
	}

	public int updateEx(String sql, Object... in) throws SQLException {
		return updateEx((long[]) null, sql, in);
	}

	public int updateEx(int[] insertId, String sql,
			Object... in) throws SQLException {
		if (insertId == null) {
			return updateEx((long[]) null, sql, in);
		}
		long[] insertId_ = new long[insertId.length];
		int rows = updateEx(insertId_, sql, in);
		for (int i = 0; i < insertId.length; i ++) {
			insertId[i] = (int) insertId_[i];
		}
		return rows;
	}

	public int updateEx(long[] insertId, String sql,
			Object... in) throws SQLException {
		try (Entry entry = borrow()) {
			int numRows;
			try (PreparedStatement ps = entry.getObject().prepareStatement(sql,
					insertId == null ? Statement.NO_GENERATED_KEYS :
					Statement.RETURN_GENERATED_KEYS)) {
				for (int i = 0; i < in.length; i ++) {
					ps.setObject(i + 1, in[i]);
				}
				try {
					numRows = ps.executeUpdate();
					if (insertId != null) {
						try (ResultSet rs = ps.getGeneratedKeys()) {
							int i = 0;
							while (i < insertId.length && rs.next()) {
								insertId[i] = rs.getLong(1);
								i ++;
							}
						}
					}
				} catch (SQLWarning e) {
					Log.i(e.getMessage());
					numRows = 0;
				} catch (SQLIntegrityConstraintViolationException e) {
					// Log.i(e.getMessage());
					numRows = -1;
				}
			}
			entry.setValid(true);
			return numRows;
		}
	}

	public Row query(String sql, int... in) throws SQLException {
		return queryEx(sql, valueOf(in));
	}

	public <E extends Exception> void query(RowCallbackEx<E> callback,
			String sql, int... in) throws E, SQLException {
		queryEx(callback, sql, valueOf(in));
	}

	public Row queryEx(String sql, Object... in) throws SQLException {
		final Row[] row_ = {null};
		try {
			queryEx(new RowCallbackEx<SingleRowException>() {
				@Override
				public void onRow(Row row) throws SingleRowException {
					row_[0] = row;
					throw new SingleRowException();
				}
			}, sql, in);
		} catch (SingleRowException e) {/**/}
		return row_[0];
	}

	public <E extends Exception> void queryEx(RowCallbackEx<E> callback,
			String sql, Object... in) throws E, SQLException {
		try (Entry entry = borrow()) {
			try (PreparedStatement ps = entry.getObject().prepareStatement(sql)) {
				for (int i = 0; i < in.length; i ++) {
					ps.setObject(i + 1, in[i]);
				}
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						callback.onRow(new Row(rs));
					}
				}
			}
			entry.setValid(true);
		}
	}

	public void source(String sqlFile) throws IOException, SQLException {
		ByteArrayQueue baq = new ByteArrayQueue();
		try (FileInputStream inSql = new FileInputStream(sqlFile)) {
			Streams.copy(inSql, baq.getOutputStream());
		}
		String[] sqls = baq.toString().split(";");
		for (String sql : sqls) {
			update(sql);
		}
	}
}