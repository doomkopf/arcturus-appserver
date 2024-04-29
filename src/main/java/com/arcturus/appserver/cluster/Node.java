package com.arcturus.appserver.cluster;

import java.util.UUID;

/**
 * A node of the current {@link Cluster}.
 * 
 * @author doomkopf
 */
public interface Node
{
	UUID getId();

	NodeIdentity getPhysicalIdentity();

	boolean isLocal();

	void send(byte[] byteData);
}