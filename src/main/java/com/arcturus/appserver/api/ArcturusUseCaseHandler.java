package com.arcturus.appserver.api;

import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.net.RequestContext;
import com.arcturus.appserver.system.app.App;

import java.util.UUID;

/**
 * Processes the use cases. Use cases are usually directly passed from the
 * network layer to an app. This can be used if you need to pass in use cases
 * manually e.g. when using your own network layer or even with another protocol
 * (e.g. REST with spring boot) or whatever.
 *
 * @author doomkopf
 */
public class ArcturusUseCaseHandler
{
	private final App app;
	private final JsonStringSerializer jsonStringSerializer;

	ArcturusUseCaseHandler(App app, JsonStringSerializer jsonStringSerializer)
	{
		this.app = app;
		this.jsonStringSerializer = jsonStringSerializer;
	}

	private void handleStringPayload(
		RequestContext requestContext,
		String useCaseId,
		String service,
		UUID entityId,
		Long sessionId,
		String jsonPayload)
	{
		app.handleUseCase(requestContext,
			null,
			useCaseId,
			service,
			entityId,
			sessionId,
			jsonPayload);
	}

	public void handle(
		RequestContext requestContext,
		String useCaseId,
		String service,
		UUID entityId,
		Long sessionId,
		Object payload)
	{
		handleStringPayload(requestContext,
			useCaseId,
			service,
			entityId,
			sessionId,
			jsonStringSerializer.toJsonString(payload));
	}
}