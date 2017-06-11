package com.xqbase.util.winrm.shell;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Receive", propOrder = {"desiredStream"})
public class Receive {
	@XmlElement(name = "DesiredStream", required = true)
	protected DesiredStreamType desiredStream;

	public DesiredStreamType getDesiredStream() {
		return desiredStream;
	}

	public void setDesiredStream(DesiredStreamType desiredStream) {
		this.desiredStream = desiredStream;
	}
}