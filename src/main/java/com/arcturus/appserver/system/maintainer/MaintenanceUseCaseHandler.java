package com.arcturus.appserver.system.maintainer;

import com.arcturus.appserver.net.PersistentLocalSession;
import com.arcturus.appserver.net.RequestContext;

import java.util.UUID;

public interface MaintenanceUseCaseHandler<T>
{
	Class<T> getRequestType();

	void handle(
		RequestContext requestContext,
		PersistentLocalSession persistentLocalSession,
		String appId,
		UUID userId,
		T request
	) throws InterruptedException;
}