package com.xqbase.util.winrm.wsman;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Signal", propOrder = {"code"})
public class Signal {
	@XmlElement(name = "Code", namespace = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell", required = true)
	protected String code;
	@XmlAttribute(name = "CommandId")
	protected String commandId;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getCommandId() {
		return commandId;
	}

	public void setCommandId(String commandId) {
		this.commandId = commandId;
	}
}