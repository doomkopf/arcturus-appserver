package com.arcturus.appserver.system.app.type.js.script;

public enum EntityUseCaseResultAction
{
	NOOP,
	STORE,
	REMOVE;

	public final char charValue;

	EntityUseCaseResultAction()
	{
		// TODO only works until 9...
		charValue = String.valueOf(ordinal()).charAt(0);
	}
}