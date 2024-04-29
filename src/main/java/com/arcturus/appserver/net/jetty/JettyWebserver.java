package com.arcturus.appserver.net.jetty;

import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;
import com.arcturus.appserver.config.Config;
import com.arcturus.appserver.config.ServerConfigPropery;
import com.arcturus.appserver.net.HttpSessionListener;
import com.arcturus.appserver.net.HttpSessionService;
import com.arcturus.appserver.net.PersistentLocalSessionListener;
import com.arcturus.appserver.net.PersistentLocalSessionService;
import com.arcturus.appserver.system.Constants;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.log.Log;

public class JettyWebserver implements HttpSessionService, PersistentLocalSessionService
{
	private final Logger log;
	private final Server server;

	private final JettyHttpServlet jettyHttpServlet;
	private final JettyWebSocketServlet jettyWebSocketServlet;

	public JettyWebserver(
		LoggerFactory loggerFactory,
		Config config,
		JettyHttpServlet jettyHttpServlet,
		JettyWebSocketServlet jettyWebSocketServlet
	) throws Exception
	{
		this.jettyHttpServlet = jettyHttpServlet;
		this.jettyWebSocketServlet = jettyWebSocketServlet;

		log = loggerFactory.create(getClass());

		Log.setLog(new JettyLogger(loggerFactory, "JettyRootLogger"));

		server = new Server(config.getInt(ServerConfigPropery.httpPort));

		var context = new ServletContextHandler();
		context.setResourceBase("./");
		context.setContextPath("/");
		server.setHandler(context);

		context.addServlet(new ServletHolder(jettyHttpServlet),
			'/' + Constants.HTTP_PATH_HTTP_API + "/*"
		);
		context.addServlet(new ServletHolder(jettyWebSocketServlet),
			'/' + Constants.HTTP_PATH_WEBSOCKET_API + "/*"
		);

		/*var maintenanceWebHome = new ServletHolder(DefaultServlet.class);
		maintenanceWebHome.setInitParameter("resourceBase", "./maintenance-web");
		maintenanceWebHome.setInitParameter("dirAllowed", "true");
		maintenanceWebHome.setInitParameter("pathInfoOnly", "true");
		context.addServlet(maintenanceWebHome, "/arcmanage/*");*/

		// TODO Temp
		var webHome = new ServletHolder(DefaultServlet.class);
		webHome.setInitParameter("resourceBase", "./log");
		webHome.setInitParameter("dirAllowed", "true");
		webHome.setInitParameter("pathInfoOnly", "true");
		context.addServlet(webHome, "/caracara/*");

		server.start();
	}

	@Override
	public void registerSessionListener(HttpSessionListener sessionListener)
	{
		jettyHttpServlet.sessionListener = sessionListener;
	}

	@Override
	public void registerSessionListener(PersistentLocalSessionListener sessionListener)
	{
		jettyWebSocketServlet.sessionListener = sessionListener;
	}

	@Override
	public void shutdown()
	{
		try
		{
			server.stop();
		}
		catch (Throwable e)
		{
			log.log(LogLevel.error, e);
		}
	}
}