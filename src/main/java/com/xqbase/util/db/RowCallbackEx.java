package com.xqbase.util.db;

@FunctionalInterface
public interface RowCallbackEx<E extends Exception> {
	public void onRow(Row row) throws E;
}