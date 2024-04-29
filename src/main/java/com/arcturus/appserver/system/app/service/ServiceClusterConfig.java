package com.arcturus.appserver.system.app.service;

/**
 * Holding the config of a {@link Service} inside the cluster (e.g. on which
 * node/s a service is supposed to be located on).
 * 
 * @author doomkopf
 */
public class ServiceClusterConfig
{
	private final boolean isIncludingNodes;
	private final String[] nodes;

	public ServiceClusterConfig(boolean isIncludingNodes, String[] nodes)
	{
		this.isIncludingNodes = isIncludingNodes;
		this.nodes = nodes;
	}

	public boolean isIncludingNodes()
	{
		return isIncludingNodes;
	}

	public String[] getNodes()
	{
		return nodes;
	}
}