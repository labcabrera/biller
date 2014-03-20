package com.luckia.biller.web;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Utilidad que levanta la aplicacion a traves de un Jetty en el puerto 9090.
 */
public class JettyServer {

	public static final int JETTY_PORT = 9090;
	public static String JETTY_CONTEXT_PATH = "/";

	public static void main(String[] args) throws Exception {
		Server server = new Server(JETTY_PORT);
		WebAppContext context = new WebAppContext();
		context.setResourceBase("src/main/webapp");
		context.setDescriptor("src/main/webapp/WEB-INF/web.xml");
		context.setContextPath(JETTY_CONTEXT_PATH);
		context.setParentLoaderPriority(true);
		server.setHandler(context);
		System.out.println("Starting server on port " + JETTY_PORT);
		server.start();
		server.join();
	}
}
