package com.arcturus.appserver.net;

/**
 * For listening to persistent connections e.g. websockets.
 * 
 * @author doomkopf
 */
public interface PersistentLocalSessionListener
{
	void onReceived(PersistentLocalSession localSession, String payload);

	void onConnected(PersistentLocalSession localSession);

	void onDisconnected(PersistentLocalSession localSession);
}