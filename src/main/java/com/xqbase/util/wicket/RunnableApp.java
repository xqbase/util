package com.xqbase.util.wicket;

import java.nio.charset.Charset;

import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.SecondLevelCacheSessionStore;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.IRequestCycleProcessor;
import org.apache.wicket.session.ISessionStore;
import org.apache.wicket.session.pagemap.LeastRecentlyAccessedEvictionStrategy;

import com.xqbase.util.db.MapStore;

public abstract class RunnableApp extends WebApplication implements Runnable {
	public static class Page extends WebPage {
		{
			((Runnable) getApplication()).run();
		}

		@Override
		protected void onRender(MarkupStream markupStream) {/**/}
	}

	protected abstract MapStore getMapStore();

	@Override
	public Class<? extends WebPage> getHomePage() {
		return Page.class;
	}

	@Override
	protected void init() {
		getApplicationSettings().setInternalErrorPage(Page.class);
		getApplicationSettings().setPageExpiredErrorPage(Page.class);
		getMarkupSettings().setDefaultBeforeDisabledLink("");
		getMarkupSettings().setDefaultAfterDisabledLink("");
		getPageSettings().setAutomaticMultiWindowSupport(false);
		getPageSettings().setVersionPagesByDefault(false);
		getRequestCycleSettings().setResponseRequestEncoding(Charset.
				defaultCharset().name());
		getSessionSettings().setMaxPageMaps(1);
		getSessionSettings().setPageMapEvictionStrategy(new
				LeastRecentlyAccessedEvictionStrategy(1));
	}

	@Override
	protected IRequestCycleProcessor newRequestCycleProcessor() {
		return new ShortProcessor();
	}

	@Override
	protected ISessionStore newSessionStore() {
		return new SecondLevelCacheSessionStore(this, new MapPageStore(getMapStore()));
	}
}