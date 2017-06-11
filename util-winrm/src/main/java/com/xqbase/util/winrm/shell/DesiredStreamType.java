package com.xqbase.util.winrm.shell;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DesiredStreamType", propOrder = {"value"})
public class DesiredStreamType {
	@XmlValue
	protected String value;
	@XmlAttribute(name = "CommandId", namespace = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell")
	protected String commandId;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getCommandId() {
		return commandId;
	}

	public void setCommandId(String commandId) {
		this.commandId = commandId;
	}
}