package com.xqbase.util.winrm;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.Action;

import com.xqbase.util.winrm.shell.CommandLine;
import com.xqbase.util.winrm.shell.Receive;
import com.xqbase.util.winrm.shell.ReceiveResponse;
import com.xqbase.util.winrm.shell.Shell;
import com.xqbase.util.winrm.transfer.ResourceCreated;
import com.xqbase.util.winrm.wsman.CommandResponse;
import com.xqbase.util.winrm.wsman.Delete;
import com.xqbase.util.winrm.wsman.DeleteResponse;
import com.xqbase.util.winrm.wsman.Locale;
import com.xqbase.util.winrm.wsman.OptionSetType;
import com.xqbase.util.winrm.wsman.SelectorSetType;
import com.xqbase.util.winrm.wsman.Signal;
import com.xqbase.util.winrm.wsman.SignalResponse;

@WebService(targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", name = "WinRm")
@XmlSeeAlso({com.xqbase.util.winrm.transfer.ObjectFactory.class, com.xqbase.util.winrm.wsman.ObjectFactory.class, com.xqbase.util.winrm.shell.ObjectFactory.class})
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public interface WinRmPort {
	@WebResult(name = "CommandResponse", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", partName = "parameters")
	@Action(input = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell/Command", output = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell/CommandResponse")
	@WebMethod(operationName = "Command", action = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell/Command")
	public CommandResponse command(
		@WebParam(partName = "body", name = "CommandLine", targetNamespace = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell")
		CommandLine body,
		@WebParam(partName = "ResourceURI", name = "ResourceURI", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
		String resourceURI,
		@WebParam(partName = "MaxEnvelopeSize", name = "MaxEnvelopeSize", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
		int maxEnvelopeSize,
		@WebParam(partName = "OperationTimeout", name = "OperationTimeout", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
		String operationTimeout,
		@WebParam(partName = "Locale", name = "Locale", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
		Locale locale,
		@WebParam(partName = "SelectorSet", name = "SelectorSet", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
		SelectorSetType selectorSet,
		@WebParam(partName = "OptionSet", name = "OptionSet", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
		OptionSetType optionSet
	);

	@WebResult(name = "ReceiveResponse", targetNamespace = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell", partName = "ReceiveResponse")
	@Action(input = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell/Receive", output = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell/ReceiveResponse")
	@WebMethod(operationName = "Receive", action = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell/Receive")
	public ReceiveResponse receive(
		@WebParam(partName = "Receive", name = "Receive", targetNamespace = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell")
		Receive receive,
		@WebParam(partName = "ResourceURI", name = "ResourceURI", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
		String resourceURI,
		@WebParam(partName = "MaxEnvelopeSize", name = "MaxEnvelopeSize", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
		int maxEnvelopeSize,
		@WebParam(partName = "OperationTimeout", name = "OperationTimeout", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
		String operationTimeout,
		@WebParam(partName = "Locale", name = "Locale", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
		Locale locale,
		@WebParam(partName = "SelectorSet", name = "SelectorSet", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
		SelectorSetType selectorSet
	);

	@WebResult(name = "ResourceCreated", targetNamespace = "http://schemas.xmlsoap.org/ws/2004/09/transfer", partName = "ResourceCreated")
	@Action(input = "http://schemas.xmlsoap.org/ws/2004/09/transfer/Create", output = "http://schemas.xmlsoap.org/ws/2004/09/transfer/CreateResponse")
	@WebMethod(operationName = "Create", action = "http://schemas.xmlsoap.org/ws/2004/09/transfer/Create")
	public ResourceCreated create(
		@WebParam(partName = "Shell", name = "Shell", targetNamespace = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell")
		Shell shell,
		@WebParam(partName = "ResourceURI", name = "ResourceURI", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
		String resourceURI,
		@WebParam(partName = "MaxEnvelopeSize", name = "MaxEnvelopeSize", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
		int maxEnvelopeSize,
		@WebParam(partName = "OperationTimeout", name = "OperationTimeout", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
		String operationTimeout,
		@WebParam(partName = "Locale", name = "Locale", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
		Locale locale,
		@WebParam(partName = "OptionSet", name = "OptionSet", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
		OptionSetType optionSet
	);

	@WebResult(name = "SignalResponse", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", partName = "SignalResponse")
	@Action(input = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell/Signal", output = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell/SignalResponse")
	@WebMethod(operationName = "Signal", action = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell/Signal")
	public SignalResponse signal(
		@WebParam(partName = "Signal", name = "Signal", targetNamespace = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell")
		Signal signal,
		@WebParam(partName = "ResourceURI", name = "ResourceURI", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
		String resourceURI,
		@WebParam(partName = "MaxEnvelopeSize", name = "MaxEnvelopeSize", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
		int maxEnvelopeSize,
		@WebParam(partName = "OperationTimeout", name = "OperationTimeout", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
		String operationTimeout,
		@WebParam(partName = "Locale", name = "Locale", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
		Locale locale,
		@WebParam(partName = "SelectorSet", name = "SelectorSet", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
		SelectorSetType selectorSet
	);

	@WebResult(name = "DeleteResponse", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", partName = "result")
	@Action(input = "http://schemas.xmlsoap.org/ws/2004/09/transfer/Delete", output = "http://schemas.xmlsoap.org/ws/2004/09/transfer/DeleteResponse")
	@WebMethod(operationName = "Delete", action = "http://schemas.xmlsoap.org/ws/2004/09/transfer/Delete")
	public DeleteResponse delete(
		@WebParam(partName = "parameters", name = "Delete", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd")
		Delete parameters,
		@WebParam(partName = "ResourceURI", name = "ResourceURI", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
		String resourceURI,
		@WebParam(partName = "MaxEnvelopeSize", name = "MaxEnvelopeSize", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
		int maxEnvelopeSize,
		@WebParam(partName = "OperationTimeout", name = "OperationTimeout", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
		String operationTimeout,
		@WebParam(partName = "Locale", name = "Locale", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
		Locale locale,
		@WebParam(partName = "SelectorSet", name = "SelectorSet", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
		SelectorSetType selectorSet
	);
}