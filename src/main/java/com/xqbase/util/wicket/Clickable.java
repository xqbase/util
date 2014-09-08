package com.xqbase.util.wicket;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.link.ILinkListener;

public abstract class Clickable extends WebComponent implements ILinkListener {
	private static final long serialVersionUID = 1L;

	protected abstract void onClick();

	@Override
	public void onLinkClicked() {
		onClick();
	}

	public Clickable(String id) {
		super(id);
	}

	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		tag.put("onclick", "javascript:location.href='" + urlFor(INTERFACE) + "'");
	}
}