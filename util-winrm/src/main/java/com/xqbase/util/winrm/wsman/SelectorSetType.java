package com.xqbase.util.winrm.wsman;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SelectorSetType", propOrder = {"selector"})
public class SelectorSetType {
	@XmlElement(name = "Selector")
	protected List<SelectorType> selector;
	@XmlAnyAttribute
	private Map<QName, String> otherAttributes = new HashMap<>();

	public List<SelectorType> getSelector() {
		if (selector == null) {
			selector = new ArrayList<>();
		}
		return selector;
	}

	public Map<QName, String> getOtherAttributes() {
		return otherAttributes;
	}
}