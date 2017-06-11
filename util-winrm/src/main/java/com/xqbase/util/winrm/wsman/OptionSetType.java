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
@XmlType(name = "OptionSetType", propOrder = {"option"})
public class OptionSetType {
	@XmlElement(name = "Option")
	protected List<OptionType> option;
	@XmlAnyAttribute
	private Map<QName, String> otherAttributes = new HashMap<>();

	public List<OptionType> getOption() {
		if (option == null) {
			option = new ArrayList<>();
		}
		return option;
	}

	public Map<QName, String> getOtherAttributes() {
		return otherAttributes;
	}
}