package com.xqbase.util.winrm.transfer;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlType;
import org.w3c.dom.Element;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResourceCreated", propOrder = {"any"})
public class ResourceCreated {
	@XmlAnyElement
	protected List<Element> any;

	public List<Element> getAny() {
		if (any == null) {
			any = new ArrayList<>();
		}
		return any;
	}
}