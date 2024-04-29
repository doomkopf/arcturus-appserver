package com.arcturus.appserver.net;

/**
 * Service for HTTP sessions.
 * 
 * @author doomkopf
 */
public interface HttpSessionService
{
	void registerSessionListener(HttpSessionListener sessionListener);

	void shutdown();
}