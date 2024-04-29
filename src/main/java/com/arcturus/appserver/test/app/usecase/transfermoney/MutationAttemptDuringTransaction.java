package com.arcturus.appserver.test.app.usecase.transfermoney;

import com.arcturus.api.ResponseSender;
import com.arcturus.api.service.entity.EntityUseCase;
import com.arcturus.api.service.entity.PojoPayloadEntityUseCaseHandler;
import com.arcturus.api.service.entity.UseCaseContext;
import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.test.app.service.bankaccount.BankAccount;

import java.util.UUID;

@EntityUseCase(id = "mutationAttemptDuringTransaction", service = "bankAccount", isPublic = true)
public class MutationAttemptDuringTransaction
	extends PojoPayloadEntityUseCaseHandler<BankAccount, Request>
{
	private final ResponseSender responseSender;

	public MutationAttemptDuringTransaction(
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
		BankAccount entity,
		UUID id,
		long requestId,
		UUID requestingUserId,
		Request payload,
		UseCaseContext context
	)
	{
		entity.setMoney(entity.getMoney() / 2);
		context.dirty();

		responseSender.sendObject(requestId, new Response(entity.getMoney()));
	}
}
