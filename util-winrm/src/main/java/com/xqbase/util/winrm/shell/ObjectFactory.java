package com.xqbase.util.winrm.shell;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

import com.xqbase.util.winrm.wsman.Signal;

@XmlRegistry
public class ObjectFactory {
	private final static QName _Code_QNAME = new QName("http://schemas.microsoft.com/wbem/wsman/1/windows/shell", "Code");
	private final static QName _CommandId_QNAME = new QName("http://schemas.microsoft.com/wbem/wsman/1/windows/shell", "CommandId");
	private final static QName _CommandLine_QNAME = new QName("http://schemas.microsoft.com/wbem/wsman/1/windows/shell", "CommandLine");
	private final static QName _Receive_QNAME = new QName("http://schemas.microsoft.com/wbem/wsman/1/windows/shell", "Receive");
	private final static QName _ReceiveResponse_QNAME = new QName("http://schemas.microsoft.com/wbem/wsman/1/windows/shell", "ReceiveResponse");
	private final static QName _Shell_QNAME = new QName("http://schemas.microsoft.com/wbem/wsman/1/windows/shell", "Shell");
	private final static QName _Signal_QNAME = new QName("http://schemas.microsoft.com/wbem/wsman/1/windows/shell", "Signal");

	public ObjectFactory() {/**/}

	public CommandLine createCommandLine() {
		return new CommandLine();
	}

	public Receive createReceive() {
		return new Receive();
	}

	public ReceiveResponse createReceiveResponse() {
		return new ReceiveResponse();
	}

	public Shell createShell() {
		return new Shell();
	}

	public DesiredStreamType createDesiredStreamType() {
		return new DesiredStreamType();
	}

	public StreamType createStreamType() {
		return new StreamType();
	}

	public CommandStateType createCommandStateType() {
		return new CommandStateType();
	}

	public EnvironmentVariableList createEnvironmentVariableList() {
		return new EnvironmentVariableList();
	}

	public EnvironmentVariable createEnvironmentVariable() {
		return new EnvironmentVariable();
	}

	@XmlElementDecl(namespace = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell", name = "Code")
	public JAXBElement<String> createCode(String value) {
		return new JAXBElement<>(_Code_QNAME, String.class, null, value);
	}

	@XmlElementDecl(namespace = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell", name = "CommandId")
	public JAXBElement<String> createCommandId(String value) {
		return new JAXBElement<>(_CommandId_QNAME, String.class, null, value);
	}

	@XmlElementDecl(namespace = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell", name = "CommandLine")
	public JAXBElement<CommandLine> createCommandLine(CommandLine value) {
		return new JAXBElement<>(_CommandLine_QNAME, CommandLine.class, null, value);
	}

	@XmlElementDecl(namespace = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell", name = "Receive")
	public JAXBElement<Receive> createReceive(Receive value) {
		return new JAXBElement<>(_Receive_QNAME, Receive.class, null, value);
	}

	@XmlElementDecl(namespace = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell", name = "ReceiveResponse")
	public JAXBElement<ReceiveResponse> createReceiveResponse(ReceiveResponse value) {
		return new JAXBElement<>(_ReceiveResponse_QNAME, ReceiveResponse.class, null, value);
	}

	@XmlElementDecl(namespace = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell", name = "Shell")
	public JAXBElement<Shell> createShell(Shell value) {
		return new JAXBElement<>(_Shell_QNAME, Shell.class, null, value);
	}

	@XmlElementDecl(namespace = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell", name = "Signal")
	public JAXBElement<Signal> createSignal(Signal value) {
		return new JAXBElement<>(_Signal_QNAME, Signal.class, null, value);
	}
}