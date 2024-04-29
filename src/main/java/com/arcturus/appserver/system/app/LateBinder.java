package com.arcturus.appserver.system.app;

import com.arcturus.api.ArcturusAppException;
import com.arcturus.appserver.system.app.logmessage.AppLoggerEntityService;
import com.arcturus.appserver.system.app.logmessage.ArcturusAppLogger;
import com.arcturus.appserver.system.app.service.UseCaseProvider.LateBindingUseCaseProvider;
import com.arcturus.appserver.system.app.service.UserEntityServiceProvider;
import com.arcturus.appserver.system.app.service.entity.EntityServiceFactory;

import java.net.UnknownHostException;

public class LateBinder
{
	private final CustomLateBinder customLateBinder;

	private final App app;
	private final UserEntityServiceProvider serviceProvider;
	private final LateBindingUseCaseProvider lateBindingUseCaseProvider;
	private final ArcturusAppLogger appLogger;

	private final EntityServiceFactory<?> entityServiceFactory;
	private final AppLoggerEntityService appLoggerEntityService;

	public LateBinder(
		CustomLateBinder customLateBinder,

		App app,
		UserEntityServiceProvider serviceProvider,
		LateBindingUseCaseProvider lateBindingUseCaseProvider,
		ArcturusAppLogger appLogger,

		EntityServiceFactory<?> entityServiceFactory,
		AppLoggerEntityService appLoggerEntityService
	)
	{
		this.customLateBinder = customLateBinder;

		this.app = app;
		this.serviceProvider = serviceProvider;
		this.lateBindingUseCaseProvider = lateBindingUseCaseProvider;
		this.appLogger = appLogger;

		this.entityServiceFactory = entityServiceFactory;
		this.appLoggerEntityService = appLoggerEntityService;
	}

	void lateBind() throws UnknownHostException, ArcturusAppException
	{
		customLateBinder.lateBind();

		app.init();
		serviceProvider.init(entityServiceFactory);
		lateBindingUseCaseProvider.init();
		appLogger.init(appLoggerEntityService);
	}
}