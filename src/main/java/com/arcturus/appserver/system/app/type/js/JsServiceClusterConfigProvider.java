package com.arcturus.appserver.system.app.type.js;

import com.arcturus.appserver.system.app.service.ServiceClusterConfig;
import com.arcturus.appserver.system.app.service.ServiceClusterConfigProvider;

public class JsServiceClusterConfigProvider implements ServiceClusterConfigProvider
{
	@Override
	public ServiceClusterConfig getByName(String serviceName)
	{
		return DEFAULT_CONFIG;
	}
}