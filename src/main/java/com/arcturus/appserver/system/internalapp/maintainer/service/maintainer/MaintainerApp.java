package com.arcturus.appserver.system.internalapp.maintainer.service.maintainer;

public class MaintainerApp
{
	private String id;
	private String name;

	@SuppressWarnings("unused")
	private MaintainerApp()
	{
	}

	public MaintainerApp(String id, String name)
	{
		this.id = id;
		this.name = name;
	}

	public String getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}
}