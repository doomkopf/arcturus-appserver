package com.arcturus.appserver.system.app.type.js.script;

import com.arcturus.api.LoggerFactory;

public class InfoAppScriptFactory
{
	private final LoggerFactory loggerFactory;
	private final ScriptEnhancer scriptEnhancer;
	private final String appScriptCode;

	public InfoAppScriptFactory(
		LoggerFactory loggerFactory, ScriptEnhancer scriptEnhancer, String appScriptCode
	)
	{
		this.loggerFactory = loggerFactory;
		this.scriptEnhancer = scriptEnhancer;
		this.appScriptCode = appScriptCode;
	}

	public InfoAppScript create()
	{
		return new InfoAppScript(loggerFactory,
			new AppScript(loggerFactory, scriptEnhancer, appScriptCode)
		);
	}
}