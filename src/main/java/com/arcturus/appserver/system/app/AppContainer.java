package com.arcturus.appserver.system.app;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Wraps an {@link App} and its spring
 * {@link AnnotationConfigApplicationContext}.
 *
 * @author doomkopf
 */
public class AppContainer
{
	private final App app;
	private final AnnotationConfigApplicationContext springContext;

	public AppContainer(App app, AnnotationConfigApplicationContext springContext)
	{
		this.app = app;
		this.springContext = springContext;
	}

	public App getApp()
	{
		return app;
	}

	public void shutdown()
	{
		springContext.stop();
		springContext.close();
	}
}