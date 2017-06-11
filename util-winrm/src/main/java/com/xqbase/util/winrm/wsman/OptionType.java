package com.xqbase.util.winrm.wsman;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OptionType", propOrder = {
	"value"
})
public class OptionType {
	@XmlValue
	protected String value;
	@XmlAttribute(name = "Name", required = true)
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	@XmlSchemaType(name = "token")
	protected String name;
	@XmlAttribute(name = "MustComply")
	protected Boolean mustComply;
	@XmlAttribute(name = "Type")
	protected QName type;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean isMustComply() {
		return mustComply;
	}

	public void setMustComply(Boolean mustComply) {
		this.mustComply = mustComply;
	}

	public QName getType() {
		return type;
	}

	public void setType(QName type) {
		this.type = type;
	}
}