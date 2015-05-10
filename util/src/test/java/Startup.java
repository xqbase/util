import javax.servlet.ServletException;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import com.xqbase.util.Conf;
import com.xqbase.util.Log;

public class Startup {
	public static void main(String[] args) {
		Conf.chdir("../src/test/webapp/WEB-INF");
		Connector connector = new Connector();
		connector.setPort(80);
		Connector sslConnector = new Connector();
		sslConnector.setPort(443);
		sslConnector.setScheme("https");
		sslConnector.setSecure(true);
		sslConnector.setProperty("SSLEnabled", "true");
		sslConnector.setProperty("sslProtocol", "TLS");
		sslConnector.setAttribute("clientAuth", "want");
		sslConnector.setProperty("keystoreType", "PKCS12");
		sslConnector.setProperty("keystoreFile",
				Conf.getAbsolutePath("conf/localhost.pfx"));
		sslConnector.setProperty("truststoreType", "JKS");
		sslConnector.setProperty("truststoreFile",
				Conf.getAbsolutePath("conf/localhost.jks"));
		Tomcat tomcat = new Tomcat();
		tomcat.setPort(80);
		tomcat.getService().addConnector(connector);
		tomcat.getService().addConnector(sslConnector);
		tomcat.setConnector(connector);
		try {
			Context ctx = tomcat.addWebapp("", Conf.getAbsolutePath(".."));
			WebResourceRoot resources = new StandardRoot(ctx);
			resources.addPreResources(new DirResourceSet(resources,
					"/WEB-INF/classes", Conf.getAbsolutePath("../../../../target/test-classes"), "/"));
			ctx.setResources(resources);
			tomcat.start();
			Thread.currentThread().join();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (ServletException | LifecycleException e) {
			Log.e(e);
		}
	}
}