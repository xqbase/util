package com.xqbase.util.db;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLRecoverableException;
import java.sql.Statement;
import java.util.HashMap;

import javax.sql.DataSource;

import com.xqbase.util.ByteArrayQueue;
import com.xqbase.util.function.ConsumerEx;

class OneRowException extends Exception {
	private static final long serialVersionUID = 1L;
}

public class DSUtil {
	private static Object[] valueOf(long... values) {
		Object[] objs = new Object[values.length];
		for (int i = 0; i < values.length; i ++) {
			objs[i] = Long.valueOf(values[i]);
		}
		return objs;
	}

	public static int update(DataSource ds,
			String sql, long... in) throws SQLException {
		return updateEx(ds, sql, valueOf(in));
	}

	public static int update(DataSource ds,
			long[] insertId, String sql, long... in) throws SQLException {
		return updateEx(ds, insertId, sql, valueOf(in));
	}

	public static int updateEx(DataSource ds,
			String sql, Object... in) throws SQLException {
		return updateEx(ds, (long[]) null, sql, in);
	}

	public static int updateEx(DataSource ds,
			long[] insertId, String sql, Object... in) throws SQLException {
		while (true) {
			try (Connection conn = ds.getConnection()) {
				try (PreparedStatement ps = conn.prepareStatement(sql,
						insertId == null ? Statement.NO_GENERATED_KEYS :
						Statement.RETURN_GENERATED_KEYS)) {
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
					} catch (SQLIntegrityConstraintViolationException e) {
						// Log.i(e.getMessage());
						numRows = -1;
					}
					return numRows;
				} catch (SQLRecoverableException e) {
					if (conn instanceof NewPredicate &&
							((NewPredicate) conn).isNew()) {
						throw e;
					}
					continue;
				}
			}
		}
	}

	public static Row query(DataSource ds,
			String sql, long... in) throws SQLException {
		return queryEx(ds, sql, valueOf(in));
	}

	public static <E extends Exception> void query(DataSource ds,
			ConsumerEx<Row, E> consumer, String sql,
			long... in) throws E, SQLException {
		queryEx(ds, consumer, sql, valueOf(in));
	}

	public static Row queryEx(DataSource ds,
			String sql, Object... in) throws SQLException {
		Row[] row_ = {null};
		try {
			queryEx(ds, row -> {
				row_[0] = row;
				throw new OneRowException();
			}, sql, in);
		} catch (OneRowException e) {/**/}
		return row_[0];
	}

	public static <E extends Exception> void queryEx(DataSource ds,
			ConsumerEx<Row, E> consumer, String sql,
			Object... in) throws E, SQLException {
		while (true) {
			try (Connection conn = ds.getConnection()) {
				try (PreparedStatement ps = conn.prepareStatement(sql)) {
					for (int i = 0; i < in.length; i ++) {
						ps.setObject(i + 1, in[i]);
					}
					try (ResultSet rs = ps.executeQuery()) {
						HashMap<String, Integer> columnMap = new HashMap<>();
						ResultSetMetaData rsmd = rs.getMetaData();
						int columnCount = rsmd.getColumnCount();
						for (int i = 0; i < columnCount; i ++) {
							columnMap.put(rsmd.getColumnLabel(i + 1).
									toLowerCase(), Integer.valueOf(i));
						}
						while (rs.next()) {
							consumer.accept(new Row(rs, columnCount, columnMap));
						}
					} catch (RuntimeException | SQLException e) {
						throw e;
					} catch (Exception e) { // must be E
						throw e;
					}
					return;
				} catch (SQLRecoverableException e) {
					if (conn instanceof NewPredicate &&
							((NewPredicate) conn).isNew()) {
						throw e;
					}
					continue;
				}
			}
		}
	}

	public static void source(DataSource ds,
			String sqlFile) throws IOException, SQLException {
		ByteArrayQueue baq = new ByteArrayQueue();
		try (FileInputStream inSql = new FileInputStream(sqlFile)) {
			baq.readFrom(inSql);
		}
		String[] sqls = baq.toString().split(";");
		for (String sql : sqls) {
			update(ds, sql);
		}
	}
}