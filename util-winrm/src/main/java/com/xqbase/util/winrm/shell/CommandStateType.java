package com.xqbase.util.winrm.shell;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CommandStateType", propOrder = {"exitCode"})
public class CommandStateType {
	@XmlElement(name = "ExitCode")
	protected BigInteger exitCode;
	@XmlAttribute(name = "CommandId", required = true)
	protected String commandId;
	@XmlAttribute(name = "State")
	protected String state;

	public BigInteger getExitCode() {
		return exitCode;
	}

	public void setExitCode(BigInteger exitCode) {
		this.exitCode = exitCode;
	}

	public String getCommandId() {
		return commandId;
	}

	public void setCommandId(String commandId) {
		this.commandId = commandId;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
}