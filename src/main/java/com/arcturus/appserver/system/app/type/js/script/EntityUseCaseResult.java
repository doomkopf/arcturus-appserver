package com.arcturus.appserver.system.app.type.js.script;

public class EntityUseCaseResult
{
	public final EntityUseCaseResultAction action;
	public final String newEntity;

	EntityUseCaseResult(EntityUseCaseResultAction action, String newEntity)
	{
		this.action = action;
		this.newEntity = newEntity;
	}
}