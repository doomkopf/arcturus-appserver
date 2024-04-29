package com.arcturus.appserver.test.app.usecase.createbankaccount;

import com.arcturus.api.ResponseSender;
import com.arcturus.api.service.entity.EntityUseCase;
import com.arcturus.api.service.entity.PojoPayloadEntityUseCaseHandler;
import com.arcturus.api.service.entity.UseCaseContext;
import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.test.app.service.user.User;

import java.util.UUID;

@EntityUseCase(service = "user")
public class WriteToUser extends PojoPayloadEntityUseCaseHandler<User, WriteToUser.Request>
{
	static class Request
	{
		UUID bankAccountId;

		Request(UUID bankAccountId)
		{
			this.bankAccountId = bankAccountId;
		}
	}

	private final ResponseSender responseSender;

	public WriteToUser(
		JsonStringSerializer jsonStringSerializer, ResponseSender responseSender
	)
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
		if ((entity.getBankAccount1() != null) && (entity.getBankAccount2() != null))
		{
			entity.setBankAccount1(null);
			entity.setBankAccount2(null);
		}

		if (entity.getBankAccount1() == null)
		{
			entity.setBankAccount1(payload.bankAccountId);
		}
		else
		{
			entity.setBankAccount2(payload.bankAccountId);
		}

		context.dirty();

		responseSender.sendObject(requestId, new Response("ok", payload.bankAccountId));
	}
}
