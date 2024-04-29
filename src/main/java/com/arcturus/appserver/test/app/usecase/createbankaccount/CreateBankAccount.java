package com.arcturus.appserver.test.app.usecase.createbankaccount;

import com.arcturus.api.service.entity.EntityServiceProvider;
import com.arcturus.api.service.entity.EntityUseCase;
import com.arcturus.api.service.entity.PojoPayloadEntityUseCaseHandler;
import com.arcturus.api.service.entity.UseCaseContext;
import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.test.app.TestService;
import com.arcturus.appserver.test.app.service.bankaccount.BankAccount;

import java.util.UUID;

@EntityUseCase(id = "create", service = "bankAccount", isCreateEntity = true, isPublic = true)
public class CreateBankAccount extends PojoPayloadEntityUseCaseHandler<BankAccount, Request>
{
	private final EntityServiceProvider entityServiceProvider;

	public CreateBankAccount(
		JsonStringSerializer jsonStringSerializer, EntityServiceProvider entityServiceProvider
	)
	{
		super(jsonStringSerializer);
		this.entityServiceProvider = entityServiceProvider;
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
		entity.setMoney(payload.money);
		context.dirty();

		entityServiceProvider.getEntityServiceByType(TestService.user).sendObject(
			WriteToUser.class,
			requestingUserId,
			requestId,
			requestingUserId,
			new WriteToUser.Request(id)
		);
	}
}
