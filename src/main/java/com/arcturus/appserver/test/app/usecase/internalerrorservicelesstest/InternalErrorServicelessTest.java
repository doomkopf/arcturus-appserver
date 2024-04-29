package com.arcturus.appserver.test.app.usecase.internalerrorservicelesstest;

import com.arcturus.api.service.PojoPayloadUseCaseHandler;
import com.arcturus.api.service.RequestInfo;
import com.arcturus.api.service.UseCase;
import com.arcturus.api.tool.JsonStringSerializer;

import java.util.UUID;

@UseCase(id = "internalErrorServicelessTest", isPublic = true)
public class InternalErrorServicelessTest extends PojoPayloadUseCaseHandler<Request>
{
	public InternalErrorServicelessTest(JsonStringSerializer jsonStringSerializer)
	{
		super(jsonStringSerializer);
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
		throw new RuntimeException("!!!!TEST!!!!");
	}
}
