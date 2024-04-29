package com.arcturus.appserver.system.app.service;

public interface ServiceClusterConfigProvider // NOSONAR
{
	ServiceClusterConfig DEFAULT_CONFIG = new ServiceClusterConfig(false, new String[] {});

	ServiceClusterConfig getByName(String serviceName);
}