package com.arcturus.appserver.system.app.type.java;

import com.arcturus.api.UserSessionHandler;
import com.arcturus.appserver.inject.Injector;
import com.arcturus.appserver.system.app.UserSessionHandlerFactory;

/**
 * Creates {@link UserSessionHandler}.
 *
 * @author doomkopf
 */
public class JavaUserSessionHandlerFactory implements UserSessionHandlerFactory
{
	private final AppClasses appClasses;
	private final Injector appInjector;

	public JavaUserSessionHandlerFactory(AppClasses appClasses, Injector appInjector)
	{
		this.appClasses = appClasses;
		this.appInjector = appInjector;
	}

	@Override
	public UserSessionHandler create()
	{
		for (var clazz : appClasses.getClassesIterable())
		{
			if (UserSessionHandler.class.isAssignableFrom(clazz))
			{
				return (UserSessionHandler) appInjector.getInstance(clazz);
			}
		}

		return null;
	}
}