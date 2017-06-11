package com.xqbase.util.winrm.shell;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StreamType", propOrder = {"value"})
public class StreamType {
	@XmlValue
	protected byte[] value;
	@XmlAttribute(name = "Name", required = true)
	protected String name;
	@XmlAttribute(name = "CommandId")
	protected String commandId;
	@XmlAttribute(name = "End")
	protected Boolean end;
	@XmlAttribute(name = "Unit")
	@XmlSchemaType(name = "anyURI")
	protected String unit;
	@XmlAttribute(name = "EndUnit")
	protected Boolean endUnit;

	public byte[] getValue() {
		return value;
	}

	public void setValue(byte[] value) {
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCommandId() {
		return commandId;
	}

	public void setCommandId(String commandId) {
		this.commandId = commandId;
	}

	public Boolean isEnd() {
		return end;
	}

	public void setEnd(Boolean end) {
		this.end = end;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public Boolean isEndUnit() {
		return endUnit;
	}

	public void setEndUnit(Boolean endUnit) {
		this.endUnit = endUnit;
	}
}