package com.xqbase.util.db;

import java.sql.Connection;

import com.mysql.cj.jdbc.NonRegisteringDriver;

public class TestConnectionPool {
	public static void main(String[] args) throws Exception {
		try (ConnectionPool pool = new ConnectionPool(new NonRegisteringDriver(), "jdbc:mysql://localhost:33306/test?&serverTimezone=UTC", "root", "****")) {
			try (Connection conn = pool.getConnection()) {
				conn.createStatement().execute("SELECT 0");
				System.out.println(conn);
			}
			Thread.sleep(10000);
			try (Connection conn = pool.getConnection()) {
				conn.createStatement().execute("SELECT 0 FROM not_exist");
				System.out.println(conn);
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
			Thread.sleep(10000);
			try (Connection conn = pool.getConnection()) {
				conn.createStatement().execute("SELECT 0");
				System.out.println(conn);
			}
		}
	}
}