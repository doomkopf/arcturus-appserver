package com.arcturus.appserver.system.app.inject;

/**
 * Container for some primitives that are singletons/global in the app context.
 * 
 * @author doomkopf
 */
public class AppSingletons
{
	private final String appId;

	public AppSingletons(String appId)
	{
		this.appId = appId;
	}

	public String getAppId()
	{
		return appId;
	}
}