package com.xqbase.util.winrm.transfer;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

@XmlRegistry
public class ObjectFactory {
	private final static QName _ResourceCreated_QNAME = new QName("http://schemas.xmlsoap.org/ws/2004/09/transfer", "ResourceCreated");

	public ObjectFactory() {/**/}

	public ResourceCreated createResourceCreated() {
		return new ResourceCreated();
	}

	@XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2004/09/transfer", name = "ResourceCreated")
	public JAXBElement<ResourceCreated> createResourceCreated(ResourceCreated value) {
		return new JAXBElement<>(_ResourceCreated_QNAME, ResourceCreated.class, null, value);
	}
}