package com.arcturus.appserver.system.maintainer.usecase.getswaggerinfo;

import com.arcturus.appserver.net.PersistentLocalSession;
import com.arcturus.appserver.net.RequestContext;
import com.arcturus.appserver.system.AppManager;
import com.arcturus.appserver.system.maintainer.MaintenanceUseCaseHandler;

import java.util.UUID;

public class GetSwaggerInfo implements MaintenanceUseCaseHandler<GetSwaggerInfoRequest>
{
	private final AppManager appManager;

	public GetSwaggerInfo(AppManager appManager)
	{
		this.appManager = appManager;
	}

	@Override
	public Class<GetSwaggerInfoRequest> getRequestType()
	{
		return GetSwaggerInfoRequest.class;
	}

	@Override
	public void handle(
		RequestContext requestContext,
		PersistentLocalSession persistentLocalSession,
		String appId,
		UUID userId,
		GetSwaggerInfoRequest request
	)
	{
		var app = appManager.getApp(appId);
		if (app == null)
		{
			return;
		}

		requestContext.respond(app.getSwaggerInfo().getSwaggerInfoJson());
	}
}