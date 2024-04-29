package com.arcturus.appserver.net;

/**
 * Service for {@link PersistentLocalSession}s.
 * 
 * @author doomkopf
 */
public interface PersistentLocalSessionService
{
	void registerSessionListener(PersistentLocalSessionListener sessionListener);

	void shutdown();
}