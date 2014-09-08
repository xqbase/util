package com.xqbase.util.wicket;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.target.basic.RedirectRequestTarget;

class AttributeMap extends AbstractBehavior {
	private static final long serialVersionUID = 1L;

	private LinkedHashMap<String, String> map = new LinkedHashMap<>();

	@Override
	public void onComponentTag(final Component component, final ComponentTag tag) {
		if (!isEnabled(component)) {
			return;
		}
		for (Map.Entry<String, String> entry : map.entrySet()) {
			tag.getAttributes().put(entry.getKey(), entry.getValue());
		}
	}

	String get(String key) {
		return map.get(key);
	}

	void put(String key, String value) {
		map.put(key, value);
	}

	void remove(String key) {
		map.remove(key);
	}
}

public class WicketUtil {
	public static void close() {
		RequestCycle requestCycle = RequestCycle.get();
		if (requestCycle != null) {
			requestCycle.setRequestTarget(null);
		}
	}

	private static AttributeMap getAttributeMap(Component component) {
		for (IBehavior behavior : component.getBehaviors()) {
			if ((behavior instanceof AttributeMap)) {
				return (AttributeMap) behavior;
			}
		}
		return null;
	}

	public static String getAttribute(Component component, String attribute) {
		AttributeMap attributeMap = getAttributeMap(component);
		return attributeMap == null ? null : attributeMap.get(attribute);
	}

	public static void setAttribute(Component component,
			String attribute, String value) {
		AttributeMap attributeMap = getAttributeMap(component);
		if (attributeMap == null) {
			attributeMap = new AttributeMap();
			component.add(attributeMap);
		}
		attributeMap.put(attribute, value);
	}

	public static void removeAttribute(Component component, String attribute) {
		AttributeMap attributeMap = getAttributeMap(component);
		if (attributeMap != null) {
			attributeMap.remove(attribute);
		}
	}

	public static void addSubmitLink(Form<Void> frm, String id) {
		WebMarkupContainer lnkSubmit = new WebMarkupContainer(id);
		WicketUtil.setAttribute(lnkSubmit, "href",
				"javascript:" + frm.getMarkupId() + ".submit()");
		frm.add(lnkSubmit);
		frm.setOutputMarkupId(true);
	}

	@SafeVarargs
	public static <T extends Enum<T>> RadioGroup<T> newRadioGroup(String id, T... values) {
		RadioGroup<T> radioGroup = new RadioGroup<>(id, Model.<T>of());
		for (T value : values) {
			radioGroup.add(new Radio<>(value.name(), Model.of(value)).
					setMarkupId(value.name()));
		}
		return radioGroup;
	}

	public static void redirect(String url) {
		RequestCycle.get().setRequestTarget(new RedirectRequestTarget(url));
	}

	public static String setVisible(WebMarkupContainer container, boolean visible) {
		return container.getMarkupId() + ".style.display = \"" +
				(visible ? "block" : "none") + "\";";
	}
}