package com.xqbase.util.db;

public interface RowCallback extends RowCallbackEx<RuntimeException> {
	@Override
	public void onRow(Row row);
}