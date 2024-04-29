package com.arcturus.appserver.system.internalapp.maintainer.service.maintainer;

import java.util.List;
import java.util.UUID;

public class Maintainer
{
	private List<MaintainerApp> apps;

	@SuppressWarnings("unused")
	private Maintainer()
	{
	}

	public Maintainer(List<MaintainerApp> apps)
	{
		this.apps = apps;
	}

	public List<MaintainerApp> getApps()
	{
		return apps;
	}

	public MaintainerApp createApp(String name)
	{
		var app = new MaintainerApp(UUID.randomUUID().toString(), name);
		apps.add(app);

		return app;
	}
}