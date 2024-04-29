package com.arcturus.appserver.system;

import com.arcturus.api.AppConfig;
import com.arcturus.appserver.system.app.App;

import java.util.List;

/**
 * Containing {@link AppConfig}s for each {@link App}.
 *
 * @author doomkopf
 */
public class AppConfigs
{
	private final List<AppConfig> appConfigList;

	public AppConfigs(List<AppConfig> appConfigList)
	{
		this.appConfigList = appConfigList;
	}

	Iterable<AppConfig> getAppConfigsIterable()
	{
		return appConfigList;
	}
}