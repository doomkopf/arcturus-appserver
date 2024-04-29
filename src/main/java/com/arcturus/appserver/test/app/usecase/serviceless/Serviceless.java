package com.arcturus.appserver.test.app.usecase.serviceless;

import com.arcturus.api.ResponseSender;
import com.arcturus.api.service.PojoPayloadUseCaseHandler;
import com.arcturus.api.service.RequestInfo;
import com.arcturus.api.service.UseCase;
import com.arcturus.api.tool.JsonStringSerializer;

import java.util.UUID;

@UseCase(id = "serviceless", isPublic = true)
public class Serviceless extends PojoPayloadUseCaseHandler<Request>
{
	private final ResponseSender responseSender;

	public Serviceless(JsonStringSerializer jsonStringSerializer, ResponseSender responseSender)
	{
		super(jsonStringSerializer);
		this.responseSender = responseSender;
	}

	@Override
	protected Class<Request> getPayloadType()
	{
		return Request.class;
	}

	@Override
	protected void handle(
		long requestId, UUID requestingUserId, Request payload, RequestInfo requestInfo
	)
	{
		responseSender.sendObject(requestId, new Response());
	}
}
