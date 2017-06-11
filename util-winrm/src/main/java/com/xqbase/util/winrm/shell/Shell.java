package com.xqbase.util.winrm.shell;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.Duration;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Shell", propOrder = {
	"shellId",
	"environment",
	"workingDirectory",
	"lifetime",
	"idleTimeout",
	"inputStreams",
	"outputStreams",
	"any",
})
public class Shell {
	@XmlElement(name = "ShellId")
	@XmlSchemaType(name = "anyURI")
	protected String shellId;
	@XmlElement(name = "Environment")
	protected EnvironmentVariableList environment;
	@XmlElement(name = "WorkingDirectory")
	protected String workingDirectory;
	@XmlElement(name = "Lifetime")
	protected Duration lifetime;
	@XmlElement(name = "IdleTimeout")
	protected Duration idleTimeout;
	@XmlList
	@XmlElement(name = "InputStreams")
	protected List<String> inputStreams;
	@XmlList
	@XmlElement(name = "OutputStreams")
	protected List<String> outputStreams;
	@XmlAnyElement(lax = true)
	protected List<Object> any;

	public String getShellId() {
		return shellId;
	}

	public void setShellId(String shellId) {
		this.shellId = shellId;
	}

	public EnvironmentVariableList getEnvironment() {
		return environment;
	}

	public void setEnvironment(EnvironmentVariableList environment) {
		this.environment = environment;
	}

	public String getWorkingDirectory() {
		return workingDirectory;
	}

	public void setWorkingDirectory(String workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	public Duration getLifetime() {
		return lifetime;
	}

	public void setLifetime(Duration lifetime) {
		this.lifetime = lifetime;
	}

	public Duration getIdleTimeout() {
		return idleTimeout;
	}

	public void setIdleTimeout(Duration idleTimeout) {
		this.idleTimeout = idleTimeout;
	}

	public List<String> getInputStreams() {
		if (inputStreams == null) {
			inputStreams = new ArrayList<>();
		}
		return inputStreams;
	}

	public List<String> getOutputStreams() {
		if (outputStreams == null) {
			outputStreams = new ArrayList<>();
		}
		return outputStreams;
	}

	public List<Object> getAny() {
		if (any == null) {
			any = new ArrayList<>();
		}
		return any;
	}
}