package com.xqbase.util.db;

@FunctionalInterface
public interface RowCallback extends RowCallbackEx<RuntimeException> {
	@Override
	public void onRow(Row row);
}