package com.xqbase.util.winrm.wsman;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SignalResponse", propOrder = {"any"})
public class SignalResponse {
	@XmlAnyElement(lax = true)
	protected List<Object> any;

	public List<Object> getAny() {
		if (any == null) {
			any = new ArrayList<>();
		}
		return any;
	}
}