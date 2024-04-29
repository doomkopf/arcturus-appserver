package com.arcturus.appserver.system.internalapp.maintainer;

import com.arcturus.api.AppConfig;
import com.arcturus.api.service.entity.list.ListServiceConfig;
import com.arcturus.appserver.system.app.service.entity.ArcturusEntityService;

public class MaintainerAppConfig implements AppConfig
{
	@Override
	public String getAppId()
	{
		return "_m";
	}

	@Override
	public String getRootPackage()
	{
		return MaintainerAppConfig.class.getPackage().getName();
	}

	@Override
	public String[] entityServiceNames()
	{
		return new String[] {ArcturusEntityService.SERVICE_NAME_USER};
	}

	@Override
	public ListServiceConfig[] listServiceConfigs()
	{
		return new ListServiceConfig[0];
	}
}