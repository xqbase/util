package com.xqbase.util.winrm.wsman;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

@XmlRegistry
public class ObjectFactory {
	private final static QName _CommandResponse_QNAME = new QName("http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", "CommandResponse");
	private final static QName _Delete_QNAME = new QName("http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", "Delete");
	private final static QName _DeleteResponse_QNAME = new QName("http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", "DeleteResponse");
	private final static QName _ResourceURI_QNAME = new QName("http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", "ResourceURI");
	private final static QName _MaxEnvelopeSize_QNAME = new QName("http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", "MaxEnvelopeSize");
	private final static QName _OperationTimeout_QNAME = new QName("http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", "OperationTimeout");
	private final static QName _Locale_QNAME = new QName("http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", "Locale");
	private final static QName _SelectorSet_QNAME = new QName("http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", "SelectorSet");
	private final static QName _OptionSet_QNAME = new QName("http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", "OptionSet");
	private final static QName _SignalResponse_QNAME = new QName("http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", "SignalResponse");

	public ObjectFactory() {/**/}

	public Signal createSignal() {
		return new Signal();
	}

	public CommandResponse createCommandResponse() {
		return new CommandResponse();
	}

	public Delete createDelete() {
		return new Delete();
	}

	public DeleteResponse createDeleteResponse() {
		return new DeleteResponse();
	}

	public Locale createLocale() {
		return new Locale();
	}

	public SelectorSetType createSelectorSetType() {
		return new SelectorSetType();
	}

	public OptionSetType createOptionSetType() {
		return new OptionSetType();
	}

	public SignalResponse createSignalResponse() {
		return new SignalResponse();
	}

	public SelectorType createSelectorType() {
		return new SelectorType();
	}

	public OptionType createOptionType() {
		return new OptionType();
	}

	@XmlElementDecl(namespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", name = "CommandResponse")
	public JAXBElement<CommandResponse> createCommandResponse(CommandResponse value) {
		return new JAXBElement<>(_CommandResponse_QNAME, CommandResponse.class, null, value);
	}

	@XmlElementDecl(namespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", name = "Delete")
	public JAXBElement<Delete> createDelete(Delete value) {
		return new JAXBElement<>(_Delete_QNAME, Delete.class, null, value);
	}

	@XmlElementDecl(namespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", name = "DeleteResponse")
	public JAXBElement<DeleteResponse> createDeleteResponse(DeleteResponse value) {
		return new JAXBElement<>(_DeleteResponse_QNAME, DeleteResponse.class, null, value);
	}

	@XmlElementDecl(namespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", name = "ResourceURI")
	public JAXBElement<String> createResourceURI(String value) {
		return new JAXBElement<>(_ResourceURI_QNAME, String.class, null, value);
	}

	@XmlElementDecl(namespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", name = "MaxEnvelopeSize")
	public JAXBElement<Integer> createMaxEnvelopeSize(Integer value) {
		return new JAXBElement<>(_MaxEnvelopeSize_QNAME, Integer.class, null, value);
	}

	@XmlElementDecl(namespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", name = "OperationTimeout")
	public JAXBElement<String> createOperationTimeout(String value) {
		return new JAXBElement<>(_OperationTimeout_QNAME, String.class, null, value);
	}

	@XmlElementDecl(namespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", name = "Locale")
	public JAXBElement<Locale> createLocale(Locale value) {
		return new JAXBElement<>(_Locale_QNAME, Locale.class, null, value);
	}

	@XmlElementDecl(namespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", name = "SelectorSet")
	public JAXBElement<SelectorSetType> createSelectorSet(SelectorSetType value) {
		return new JAXBElement<>(_SelectorSet_QNAME, SelectorSetType.class, null, value);
	}

	@XmlElementDecl(namespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", name = "OptionSet")
	public JAXBElement<OptionSetType> createOptionSet(OptionSetType value) {
		return new JAXBElement<>(_OptionSet_QNAME, OptionSetType.class, null, value);
	}

	@XmlElementDecl(namespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", name = "SignalResponse")
	public JAXBElement<SignalResponse> createSignalResponse(SignalResponse value) {
		return new JAXBElement<>(_SignalResponse_QNAME, SignalResponse.class, null, value);
	}
}