package com.arcturus.appserver.system.app.type.java;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;

import com.arcturus.api.AppConfig;
import com.arcturus.appserver.reflect.ClassLookupTools;

public class AppClasses
{
	private final Collection<Class<?>> classes;

	public AppClasses(AppConfig appConfig)
			throws ClassNotFoundException, IOException, URISyntaxException
	{
		classes = ClassLookupTools.findClasses(appConfig.getRootPackage());
	}

	public Iterable<Class<?>> getClassesIterable()
	{
		return classes;
	}
}