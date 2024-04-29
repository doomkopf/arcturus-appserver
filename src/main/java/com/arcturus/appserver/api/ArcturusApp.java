package com.arcturus.appserver.api;

/**
 * An application of the arcturus-appserver.
 *
 * @author doomkopf
 */
public class ArcturusApp
{
	private final ArcturusUseCaseHandler useCaseHandler;

	ArcturusApp(ArcturusUseCaseHandler useCaseHandler)
	{
		this.useCaseHandler = useCaseHandler;
	}

	public ArcturusUseCaseHandler getUseCaseHandler()
	{
		return useCaseHandler;
	}
}