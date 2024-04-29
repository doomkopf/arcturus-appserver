package com.arcturus.appserver.system.internalapp.maintainer.dto;

public class MaintainerAppDto
{
	String appId;
	String name;

	public MaintainerAppDto(String appId, String name)
	{
		this.appId = appId;
		this.name = name;
	}
}