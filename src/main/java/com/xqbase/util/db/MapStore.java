package com.xqbase.util.db;

/** @see com.xqbase.util.db.mapstore.MapStore */
@Deprecated
public class MapStore extends com.xqbase.util.db.mapstore.MapStore {
	public MapStore(ConnectionPool db, String table) {
		super(db, table);
	}
}