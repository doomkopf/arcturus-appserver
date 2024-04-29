package com.arcturus.appserver.system.app.type.java;

import com.arcturus.api.service.UseCase;
import com.arcturus.api.service.UseCaseHandler;
import com.arcturus.appserver.inject.Injector;
import com.arcturus.appserver.system.app.service.UseCaseProvider;
import com.arcturus.appserver.system.app.service.UseCaseProvider.LateBindingUseCaseProvider;

import java.util.HashMap;
import java.util.Map;

public class JavaUseCaseProvider implements UseCaseProvider, LateBindingUseCaseProvider
{
	private final JavaServiceInfos javaServiceInfos;
	private final Injector appInjector;

	private final Map<String, UseCaseHandler> idToUseCaseHandlerMap = new HashMap<>();

	public JavaUseCaseProvider(
		JavaServiceInfos javaServiceInfos, Injector appInjector
	)
	{
		this.javaServiceInfos = javaServiceInfos;
		this.appInjector = appInjector;
	}

	@Override
	public void init()
	{
		for (var clazz : javaServiceInfos.getUseCaseHandlerClassesIterable())
		{
			var useCase = clazz.getAnnotation(UseCase.class);
			if (useCase == null)
			{
				// TODO log
				continue;
			}

			idToUseCaseHandlerMap.put(useCase.id(), appInjector.getInstance(clazz));
		}
	}

	@Override
	public UseCaseHandler getUseCaseHandler(String useCaseId)
	{
		return idToUseCaseHandlerMap.get(useCaseId);
	}
}
