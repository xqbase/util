package com.xqbase.util.winrm;

import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service;

@WebServiceClient(name = "WinRmService", 
		wsdlLocation = "classpath:wsdl/WinRmService.wsdl",
		targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd") 
public class WinRmService extends Service {
	public final static URL WSDL_LOCATION;

	public final static QName SERVICE = new QName("http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", "WinRmService");
	public final static QName WinRmPort = new QName("http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", "WinRmPort");

	static {
		URL url = WinRmService.class.getClassLoader().getResource("wsdl/WinRmService.wsdl");
		if (url == null) {
			java.util.logging.Logger.getLogger(WinRmService.class.getName())
				.log(java.util.logging.Level.INFO, 
					 "Can not initialize the default wsdl from {0}", "classpath:wsdl/WinRmService.wsdl");
		}	   
		WSDL_LOCATION = url;   
	}

	public WinRmService(URL wsdlLocation) {
		super(wsdlLocation, SERVICE);
	}

	public WinRmService(URL wsdlLocation, QName serviceName) {
		super(wsdlLocation, serviceName);
	}

	public WinRmService() {
		super(WSDL_LOCATION, SERVICE);
	}
	
	public WinRmService(WebServiceFeature ... features) {
		super(WSDL_LOCATION, SERVICE, features);
	}

	public WinRmService(URL wsdlLocation, WebServiceFeature ... features) {
		super(wsdlLocation, SERVICE, features);
	}

	public WinRmService(URL wsdlLocation, QName serviceName, WebServiceFeature ... features) {
		super(wsdlLocation, serviceName, features);
	}	

	@WebEndpoint(name = "WinRmPort")
	public WinRmPort getWinRmPort() {
		return super.getPort(WinRmPort, WinRmPort.class);
	}

	@WebEndpoint(name = "WinRmPort")
	public WinRmPort getWinRmPort(WebServiceFeature... features) {
		return super.getPort(WinRmPort, WinRmPort.class, features);
	}
}