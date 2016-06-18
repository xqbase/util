import javax.servlet.ServletException;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Server;
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
		sslConnector.setProperty("sslEnabledProtocols", "SSLv2Hello,TLSv1,TLSv1.1,TLSv1.2");

		Tomcat tomcat = new Tomcat();
		tomcat.setPort(80);
		tomcat.getService().addConnector(connector);
		tomcat.getService().addConnector(sslConnector);
		tomcat.setConnector(connector);
		try {
			Context ctx = tomcat.addWebapp("", Conf.getAbsolutePath(".."));
			WebResourceRoot resources = new StandardRoot(ctx);
			resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes",
					Conf.getAbsolutePath("../../../../target/test-classes"), "/"));
			ctx.setResources(resources);

			Server server = tomcat.getServer();
			server.start();
			server.setPort(8005);
			server.await();
			server.stop();
		} catch (ServletException | LifecycleException e) {
			Log.e(e);
		}
	}
/*
	public static void main(String[] args) throws Exception {
		Conf.chdir("../src/test/webapp/WEB-INF");

		Server server = new Server();
		ServerConnector connector = new ServerConnector(server);
		connector.setPort(80);
		server.addConnector(connector);

		SslContextFactory ssl = new SslContextFactory();
		ssl.setKeyStoreType("PKCS12");
		ssl.setKeyStorePath(Conf.getAbsolutePath("conf/localhost.pfx"));
		ssl.setKeyStorePassword("changeit");
		ssl.setTrustStoreType("JKS");
		ssl.setTrustStorePath(Conf.getAbsolutePath("conf/localhost.jks"));
		ssl.setTrustStorePassword("changeit");
		ssl.setWantClientAuth(true);
		connector = new ServerConnector(server, ssl);
		connector.setPort(443);
		server.addConnector(connector);

		WebAppContext context = new WebAppContext(Conf.getAbsolutePath(".."), "/");
		context.getMetaData().setWebInfClassesDirs(Collections.
				singletonList(Resource.newResource(Conf.
				getAbsolutePath("../../../../target/test-classes"))));
		context.setConfigurations(new Configuration[] {
			new AnnotationConfiguration(),
			new WebInfConfiguration(),
			new WebXmlConfiguration(),
		});
        server.setHandler(context);
		server.start();
		server.join();
	}
*/
}