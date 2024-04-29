package com.arcturus.appserver.net.jetty;

import java.io.IOException;

import org.eclipse.jetty.websocket.api.Session;

import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;
import com.arcturus.appserver.net.PersistentLocalSession;
import com.arcturus.appserver.net.PersistentLocalSessionInfo;
import com.arcturus.appserver.net.PersistentLocalSessionStats;

/**
 * Jetty based implementation of {@link PersistentLocalSession}.
 * 
 * @author doomkopf
 */
public class JettyWebSocketPersistentLocalSession implements PersistentLocalSession
{
	private final Logger log;
	private final Session session;
	private final PersistentLocalSessionStats stats = new PersistentLocalSessionStats();
	private volatile PersistentLocalSessionInfo info = null;

	public JettyWebSocketPersistentLocalSession(LoggerFactory loggerFactory, Session session)
	{
		log = loggerFactory.create(getClass());
		this.session = session;
	}

	@Override
	public void send(String payload)
	{
		if (isOpen())
		{
			session.getRemote().sendStringByFuture(payload);
			if (log.isLogLevel(LogLevel.debug))
			{
				log.log(LogLevel.debug, "Sent payload: " + payload);
			}
		}
	}

	@Override
	public void close()
	{
		try
		{
			session.disconnect();
		}
		catch (IOException e)
		{
			if (log.isLogLevel(LogLevel.debug))
			{
				log.log(LogLevel.debug, e);
			}
		}

		session.close();
	}

	@Override
	public void respond(String jsonString)
	{
		send(jsonString);
	}

	@Override
	public PersistentLocalSessionStats getStats()
	{
		return stats;
	}

	@Override
	public boolean isOpen()
	{
		return session.isOpen();
	}

	@Override
	public String getIp()
	{
		return session.getRemoteAddress().getHostString();
	}

	@Override
	public PersistentLocalSessionInfo getInfo()
	{
		return info;
	}

	@Override
	public void setInfo(PersistentLocalSessionInfo info)
	{
		this.info = info;
	}

	@Override
	public int hashCode()
	{
		return session.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		return session.equals(obj);
	}

	@Override
	public String toString()
	{
		return session.toString();
	}
}