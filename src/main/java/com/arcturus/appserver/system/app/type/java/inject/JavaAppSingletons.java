package com.arcturus.appserver.system.app.type.java.inject;

import com.arcturus.api.AppConfig;

/**
 * Containing some app scoped singletons. Used by spring.
 * 
 * @author doomkopf
 */
public class JavaAppSingletons
{
	private final AppConfig appConfig;

	public JavaAppSingletons(AppConfig appConfig)
	{
		this.appConfig = appConfig;
	}

	public AppConfig getAppConfig()
	{
		return appConfig;
	}
}