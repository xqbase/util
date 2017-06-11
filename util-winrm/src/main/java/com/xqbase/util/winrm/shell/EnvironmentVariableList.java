package com.xqbase.util.winrm.shell;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EnvironmentVariableList", propOrder = {"variable"})
public class EnvironmentVariableList {
	@XmlElement(name = "Variable", required = true)
	protected List<EnvironmentVariable> variable;

   public List<EnvironmentVariable> getVariable() {
		if (variable == null) {
			variable = new ArrayList<>();
		}
		return variable;
	}
}