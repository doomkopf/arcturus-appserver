package com.arcturus.appserver.net;

/**
 * A persistent (non HTTP, stays open, data can be pushed at any time), local
 * (connected to this machine, not another node in the cluster) session that
 * exists from connect till disconnect.
 * 
 * @author doomkopf
 */
public interface PersistentLocalSession extends RequestContext
{
	void send(String payload);

	void close();

	boolean isOpen();

	String getIp();

	PersistentLocalSessionStats getStats();

	PersistentLocalSessionInfo getInfo();

	void setInfo(PersistentLocalSessionInfo info);
}