package com.arcturus.appserver.test.app.usecase.setname;

import com.arcturus.api.ResponseSender;
import com.arcturus.api.service.entity.EntityUseCase;
import com.arcturus.api.service.entity.PojoPayloadEntityUseCaseHandler;
import com.arcturus.api.service.entity.UseCaseContext;
import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.net.NetStatusCode;
import com.arcturus.appserver.test.app.service.user.User;

import java.util.UUID;

@EntityUseCase(id = "setName", service = "user", isCreateEntity = true, isPublic = true)
public class SetName extends PojoPayloadEntityUseCaseHandler<User, Request>
{
	private static final int MAX_NAME_LENGTH = 8;

	private final ResponseSender responseSender;

	public SetName(JsonStringSerializer jsonStringSerializer, ResponseSender responseSender)
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
		User entity,
		UUID id,
		long requestId,
		UUID requestingUserId,
		Request payload,
		UseCaseContext context
	)
	{
		if (payload.name.length() > MAX_NAME_LENGTH)
		{
			responseSender.sendObject(requestId, new Response("nameTooLong", null));
			return;
		}

		entity.setName(payload.name);
		context.dirty();

		responseSender.sendObject(requestId,
			new Response(NetStatusCode.ok.name(), entity.getName())
		);
	}
}
