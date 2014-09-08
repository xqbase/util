package com.xqbase.util.wicket;

import java.util.TreeSet;

import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.pagestore.AbstractPageStore;

import com.xqbase.util.Bytes;
import com.xqbase.util.db.MapStore;

public class MapPageStore extends AbstractPageStore {
	private static String hex(int n) {
		return Bytes.toHexLower(Bytes.fromInt(n));
	}

	private MapStore store;
	private WebApplication app;

	public MapPageStore(MapStore store) {
		this.store = store;
		app = (WebApplication) Application.get();
	}

	private String getPrefix(String sessionId, String pageMapName, int pageId) {
		String prefix = sessionId + "_" + app.getApplicationKey() + "_" + pageMapName;
		if (pageId == -1) {
			return prefix;
		}
		return prefix + "_" + hex(pageId);
	}

	private String getKey(String sessionId, String pageMapName, int pageId,
			int pageVersion, int ajaxVersion) {
		return getKey(getPrefix(sessionId, pageMapName, pageId),
				pageVersion, ajaxVersion);
	}

	private String getKey(String prefix, int pageVersion, int ajaxVersion) {
		if (pageVersion != -1 && ajaxVersion != -1) {
			return prefix + "_" + hex(pageVersion) + "_" + hex(ajaxVersion);
		}
		// if versionNumber is -1, we need the last touched (saved) file
		if (pageVersion == -1) {
			byte[] b = store.get(prefix);
			return b == null ? null : new String(b);
		}
		// if versionNumber is specified and ajaxVersionNumber is -1,
		// we need page with the highest ajax number
		TreeSet<String> keySet = store.keySet(prefix + "_" + hex(pageVersion));
		if (keySet.isEmpty()) {
			return null;
		}
		return keySet.last();
	}

	@Override
	public boolean containsPage(String sessionId, String pageMapName,
			int pageId, int pageVersion) {
		String key = getKey(sessionId, pageMapName, pageId, pageVersion, -1);
		return key == null ? false : store.containsKey(key);
	}

	@Override
	public Page getPage(String sessionId, String pageMapName, int id,
			int pageVersion, int ajaxVersion) {
		String key = getKey(sessionId, pageMapName, id, pageVersion, ajaxVersion);
		if (key == null) {
			return null;
		}
		byte[] data = store.get(key);
		if (data == null) {
			return null;
		}
		return deserializePage(data, pageVersion);
	}

	@Override
	public void storePage(String sessionId, Page page) {
		for (SerializedPage serialized : serializePage(page)) {
			String prefix = getPrefix(sessionId,
					serialized.getPageMapName(), serialized.getPageId());
			String key = getKey(prefix, serialized.getVersionNumber(),
					serialized.getAjaxVersionNumber());
			store.put(key, serialized.getData());
			store.put(prefix, key.getBytes());
		}
	}

	@Override
	public void removePage(String sessionId, String pageMapName, int pageId) {
		store.clear(getPrefix(sessionId, pageMapName, pageId));
	}

	@Override
	public void pageAccessed(String sessionId, Page page) {/**/}

	@Override
	public void unbind(String sessionId) {
		store.clear(sessionId);
	}

	@Override
	public void destroy() {
		store.clear();
	}
}