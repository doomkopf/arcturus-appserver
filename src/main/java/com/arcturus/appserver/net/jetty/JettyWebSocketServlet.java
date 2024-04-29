package com.arcturus.appserver.net.jetty;

import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;
import com.arcturus.appserver.config.Config;
import com.arcturus.appserver.config.ServerConfigPropery;
import com.arcturus.appserver.net.PersistentLocalSessionListener;
import com.arcturus.appserver.system.Constants;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import java.nio.ByteBuffer;

public class JettyWebSocketServlet extends WebSocketServlet
{
	private static final long serialVersionUID = 1L;

	class InternalWebSocket implements WebSocketListener
	{
		private JettyWebSocketPersistentLocalSession jettyWebSocketPersistentLocalSession;

		@Override
		public void onWebSocketConnect(Session session)
		{
			if (log.isLogLevel(LogLevel.debug))
			{
				log.log(LogLevel.debug, "Websocket opened: " + session);
			}

			jettyWebSocketPersistentLocalSession = new JettyWebSocketPersistentLocalSession(loggerFactory,
				session
			);

			sessionListener.onConnected(jettyWebSocketPersistentLocalSession);
		}

		@Override
		public void onWebSocketClose(int statusCode, String reason)
		{
			if (log.isLogLevel(LogLevel.debug))
			{
				log.log(LogLevel.debug, "Websocket closed: " + statusCode + " " + reason);
			}

			sessionListener.onDisconnected(jettyWebSocketPersistentLocalSession);
		}

		@Override
		public void onWebSocketError(Throwable cause)
		{
			if (log.isLogLevel(LogLevel.debug))
			{
				log.log(LogLevel.debug, cause);
			}
		}

		@Override
		public void onWebSocketBinary(byte[] payload, int offset, int len)
		{
			if (log.isLogLevel(LogLevel.debug))
			{
				log.log(LogLevel.debug, "Received websocket binary message with length: " + len);
			}

			var frame = ByteBuffer.wrap(payload, offset, len);
			var data = new byte[frame.remaining()];
			frame.get(data);
			onWebSocketText(new String(data, Constants.CHARSET_UTF8));
		}

		@Override
		public void onWebSocketText(String message)
		{
			if (log.isLogLevel(LogLevel.debug))
			{
				log.log(LogLevel.debug, "Received websocket text message: " + message);
			}

			sessionListener.onReceived(jettyWebSocketPersistentLocalSession, message);
		}
	}

	final LoggerFactory loggerFactory;
	final Logger log;
	final Config config;

	PersistentLocalSessionListener sessionListener;

	public JettyWebSocketServlet(Config config, LoggerFactory loggerFactory)
	{
		this.loggerFactory = loggerFactory;
		this.log = loggerFactory.create(getClass());
		this.config = config;
	}

	@Override
	public void configure(WebSocketServletFactory factory)
	{
		var policy = factory.getPolicy();

		policy.setIdleTimeout(config.getInt(ServerConfigPropery.webSocketIdleTimeoutMillis));
		policy.setMaxTextMessageSize(262144);

		factory.setCreator((req, resp) -> new InternalWebSocket());
	}
}