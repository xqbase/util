package com.xqbase.util.winrm.shell;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReceiveResponse", propOrder = {"stream", "commandState"})
public class ReceiveResponse {
	@XmlElement(name = "Stream", required = true)
	protected List<StreamType> stream;
	@XmlElement(name = "CommandState")
	protected CommandStateType commandState;
	@XmlAttribute(name = "SequenceID", namespace = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell")
	@XmlSchemaType(name = "unsignedLong")
	protected BigInteger sequenceID;

	public List<StreamType> getStream() {
		if (stream == null) {
			stream = new ArrayList<>();
		}
		return stream;
	}

	public CommandStateType getCommandState() {
		return commandState;
	}

	public void setCommandState(CommandStateType commandState) {
		this.commandState = commandState;
	}

	public BigInteger getSequenceID() {
		return sequenceID;
	}

	public void setSequenceID(BigInteger sequenceID) {
		this.sequenceID = sequenceID;
	}
}