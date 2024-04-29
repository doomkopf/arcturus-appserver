package com.arcturus.appserver.test.app.usecase.internalerrortest;

import com.arcturus.api.service.entity.EntityUseCase;
import com.arcturus.api.service.entity.PojoPayloadEntityUseCaseHandler;
import com.arcturus.api.service.entity.UseCaseContext;
import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.test.app.service.user.User;

import java.util.UUID;

@EntityUseCase(id = "internalErrorTest", service = "user", isPublic = true)
public class InternalErrorTest extends PojoPayloadEntityUseCaseHandler<User, Request>
{
	public InternalErrorTest(JsonStringSerializer jsonStringSerializer)
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
		User entity,
		UUID id,
		long requestId,
		UUID requestingUserId,
		Request payload,
		UseCaseContext context
	)
	{
		throw new RuntimeException("!!!!TEST!!!!");
	}
}