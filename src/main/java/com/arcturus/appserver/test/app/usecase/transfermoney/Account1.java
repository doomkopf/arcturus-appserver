package com.arcturus.appserver.test.app.usecase.transfermoney;

import com.arcturus.api.service.ServiceAssignment;
import com.arcturus.api.service.entity.transaction.PojoPayloadEntityTransactionUseCaseHandler;
import com.arcturus.api.service.entity.transaction.ValidationResult;
import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.test.app.service.bankaccount.BankAccount;

import java.util.UUID;

@ServiceAssignment(service = "bankAccount")
public class Account1 extends PojoPayloadEntityTransactionUseCaseHandler<BankAccount, Request>
{
	public Account1(JsonStringSerializer jsonStringSerializer)
	{
		super(jsonStringSerializer);
	}

	@Override
	protected Class<Request> getPayloadType()
	{
		return Request.class;
	}

	@Override
	protected ValidationResult validate(
		BankAccount entity, UUID id, long requestId, UUID requestingUserId, Request payload
	)
	{
		if (entity.getMoney() != 100)
		{
			return new ValidationResult(false);
		}

		try
		{
			Thread.sleep(500);
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
			e.printStackTrace();
		}

		return new ValidationResult(true, "{}");
	}

	@Override
	protected void commit(
		BankAccount entity, UUID id, long requestId, UUID requestingUserId, Request payload
	)
	{
		entity.setMoney(50);
	}
}
