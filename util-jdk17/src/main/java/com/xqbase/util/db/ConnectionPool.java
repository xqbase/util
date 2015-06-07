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
import com.xqbase.util.function.ConsumerEx;
import com.xqbase.util.function.SupplierEx;

class OneRowException extends Exception {
	private static final long serialVersionUID = 1L;
}

public class ConnectionPool extends Pool<Connection, SQLException> {
	private static Object[] valueOf(long... values) {
		Object[] objs = new Object[values.length];
		for (int i = 0; i < values.length; i ++) {
			objs[i] = Long.valueOf(values[i]);
		}
		return objs;
	}

	static Properties getInfo(String user, String password) {
		Properties info = new Properties();
		if (user != null) {
			info.put("user", user);
		}
		if (password != null) {
			info.put("password", password);
		}
		return info;
	}

	public ConnectionPool(Driver driver, String url) {
		this(driver, url, null, null);
	}

	public ConnectionPool(final Driver driver, final String url,
			final String user, final String password) {
		super(new SupplierEx<Connection, SQLException>() {
			@Override
			public Connection get() throws SQLException {
				return driver.connect(url, getInfo(user, password));
			}
		}, new ConsumerEx<Connection, SQLException>() {
			@Override
			public void accept(Connection connection) throws SQLException {
				connection.close();
			}
		}, 60000);
	}

	public int update(String sql, long... in) throws SQLException {
		return updateEx(sql, valueOf(in));
	}

	public int update(long[] insertId, String sql,
			long... in) throws SQLException {
		return updateEx(insertId, sql, valueOf(in));
	}

	public int updateEx(String sql, Object... in) throws SQLException {
		return updateEx((long[]) null, sql, in);
	}

	public int updateEx(long[] insertId, String sql,
			Object... in) throws SQLException {
		try (
			Entry entry = borrow();
			PreparedStatement ps = entry.getObject().prepareStatement(sql,
					insertId == null ? Statement.NO_GENERATED_KEYS :
					Statement.RETURN_GENERATED_KEYS);
		) {
			for (int i = 0; i < in.length; i ++) {
				ps.setObject(i + 1, in[i]);
			}
			int numRows;
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
			entry.setValid(true);
			return numRows;
		}
	}

	public Row query(String sql, long... in) throws SQLException {
		return queryEx(sql, valueOf(in));
	}

	public <E extends Exception> void query(ConsumerEx<Row, E> consumer,
			String sql, long... in) throws E, SQLException {
		queryEx(consumer, sql, valueOf(in));
	}

	public Row queryEx(String sql, Object... in) throws SQLException {
		final Row[] row_ = {null};
		try {
			queryEx(new ConsumerEx<Row, OneRowException>() {
				@Override
				public void accept(Row row) throws OneRowException {
					row_[0] = row;
					throw new OneRowException();
				}
			}, sql, in);
		} catch (OneRowException e) {/**/}
		return row_[0];
	}

	public <E extends Exception> void queryEx(ConsumerEx<Row, E> consumer,
			String sql, Object... in) throws E, SQLException {
		try (
			Entry entry = borrow();
			PreparedStatement ps = entry.getObject().prepareStatement(sql);
		) {
			for (int i = 0; i < in.length; i ++) {
				ps.setObject(i + 1, in[i]);
			}
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					consumer.accept(new Row(rs));
				}
			} catch (RuntimeException | SQLException e) {
				throw e;
			} catch (Exception e) { // must be E
				entry.setValid(true);
				throw e;
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