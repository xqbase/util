package com.xqbase.util.winrm.shell;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CommandLine", propOrder = {"command", "arguments"})
public class CommandLine {
	@XmlElement(name = "Command")
	protected String command;
	@XmlElement(name = "Arguments")
	protected List<String> arguments;

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public List<String> getArguments() {
		if (arguments == null) {
			arguments = new ArrayList<>();
		}
		return arguments;
	}
}