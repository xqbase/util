package com.xqbase.util.winrm.wsman;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CommandResponse", propOrder = {"commandId"})
public class CommandResponse {
	@XmlElement(name = "CommandId", namespace = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell")
	protected String commandId;

	public String getCommandId() {
		return commandId;
	}

	public void setCommandId(String commandId) {
		this.commandId = commandId;
	}
}