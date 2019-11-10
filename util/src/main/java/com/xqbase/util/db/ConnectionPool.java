package com.xqbase.util.db;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.SQLNonTransientException;
import java.sql.Wrapper;
import java.util.Properties;
import java.util.logging.Logger;

import javax.sql.DataSource;

import com.xqbase.util.Pool;
import com.xqbase.util.function.ConsumerEx;

interface Wrapped {/**/}

@FunctionalInterface
interface NewPredicate {
	public boolean isNew();
}

public class ConnectionPool extends Pool<Connection, SQLException> 
		implements DataSource {
	private static Properties getInfo(String user, String password) {
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

	public ConnectionPool(Driver driver, String url, String user, String password) {
		super(() -> driver.connect(url, getInfo(user, password)), Connection::close, 60000);
	}

	@Deprecated
	public int update(String sql, long... in) throws SQLException {
		return DSUtil.update(this, sql, in);
	}

	@Deprecated
	public int update(long[] insertId, String sql,
			long... in) throws SQLException {
		return DSUtil.update(this, insertId, sql, in);
	}

	@Deprecated
	public int updateEx(String sql, Object... in) throws SQLException {
		return DSUtil.updateEx(this, sql, in);
	}

	@Deprecated
	public int updateEx(long[] insertId, String sql,
			Object... in) throws SQLException {
		return DSUtil.updateEx(this, insertId, sql, in);
	}

	@Deprecated
	public Row query(String sql, long... in) throws SQLException {
		return DSUtil.query(this, sql, in);
	}

	@Deprecated
	public <E extends Exception> void query(ConsumerEx<Row, E> consumer,
			String sql, long... in) throws E, SQLException {
		DSUtil.query(this, consumer, sql, in);
	}

	@Deprecated
	public Row queryEx(String sql, Object... in) throws SQLException {
		return DSUtil.queryEx(this, sql, in);
	}

	@Deprecated
	public <E extends Exception> void queryEx(ConsumerEx<Row, E> consumer,
			String sql, Object... in) throws E, SQLException {
		DSUtil.queryEx(this, consumer, sql, in);
	}

	@Deprecated
	public void source(String sqlFile) throws IOException, SQLException {
		DSUtil.source(this, sqlFile);
	}

	private static Object invoke(Entry connEntry, Object delegate,
			Method method, Object[] args) throws Throwable {
		if (args != null && args.length == 1 && args[0] instanceof Class) {
			switch (method.getName()) {
			case "unwrap":
				return delegate;
			case "isWrapperFor":
				return Boolean.valueOf(((Class<?>) args[0]).
						isAssignableFrom(delegate.getClass()));
			default:
			}
		}
		Object value;
		try {
			value = method.invoke(delegate, args);
		} catch (InvocationTargetException e) {
			Throwable t = e.getTargetException();
			if (!(t instanceof SQLNonTransientException) ||
					t instanceof SQLNonTransientConnectionException) {
				connEntry.setValid(false);
			}
			throw t;
		}
		if (!(value instanceof Wrapper) || value instanceof Wrapped) {
			return value;
		}
		Class<?>[] fromIfaces = value.getClass().getInterfaces();
		Class<?>[] toIfaces = new Class[fromIfaces.length + 1];
		System.arraycopy(fromIfaces, 0, toIfaces, 0, fromIfaces.length);
		toIfaces[fromIfaces.length] = Wrapped.class;
		return Proxy.newProxyInstance(ConnectionPool.class.getClassLoader(),
				toIfaces, (InvocationHandler) (proxy, method_, args_) ->
				invoke(connEntry, value, method_, args_));
	}

	@Override
	public Connection getConnection() throws SQLException {
		Entry entry = borrow();
		entry.setValid(true);
		return (Connection) Proxy.
				newProxyInstance(ConnectionPool.class.getClassLoader(),
				new Class[] {Connection.class, NewPredicate.class, Wrapped.class},
				(InvocationHandler) (proxy, method, args) -> {
			if (args == null || args.length == 0) {
				switch (method.getName()) {
				case "close":
					entry.close();
					return null;
				case "isNew":
					return Boolean.valueOf(entry.getBorrows() == 0);
				default:
				}
			}
			return invoke(entry, entry.getObject(), method, args);
		});
	}

	@Override
	public Connection getConnection(String username,
			String password) throws SQLException {
		return getConnection();
	}

	@Override
	public PrintWriter getLogWriter() {
		return null;
	}

	@Override
	public void setLogWriter(PrintWriter out) {/**/}

	@Override
	public int getLoginTimeout() {
		return 0;
	}

	@Override
	public void setLoginTimeout(int seconds) {/**/}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}
}