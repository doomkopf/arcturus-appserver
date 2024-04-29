package com.arcturus.appserver.test.app.usecase.transfermoney;

import com.arcturus.api.service.entity.EntityUseCase;
import com.arcturus.api.service.entity.PojoPayloadEntityUseCaseHandler;
import com.arcturus.api.service.entity.UseCaseContext;
import com.arcturus.api.service.entity.transaction.TransactionBuilder;
import com.arcturus.api.service.entity.transaction.TransactionEntity;
import com.arcturus.api.service.entity.transaction.TransactionManager;
import com.arcturus.api.tool.ClassToStringHasher;
import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.test.app.TestService;
import com.arcturus.appserver.test.app.service.user.User;

import java.util.UUID;

@EntityUseCase(id = "transferMoney", service = "user", isPublic = true)
public class StartTransaction extends PojoPayloadEntityUseCaseHandler<User, Request>
{
	private final TransactionManager transactionManager;
	private final ClassToStringHasher classToStringHasher;

	public StartTransaction(
		JsonStringSerializer jsonStringSerializer,
		TransactionManager transactionManager,
		ClassToStringHasher classToStringHasher
	)
	{
		super(jsonStringSerializer);
		this.transactionManager = transactionManager;
		this.classToStringHasher = classToStringHasher;
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
		transactionManager.startTransactionObject(TransactionBuilder.create()
			.addEntity(new TransactionEntity(
				entity.getBankAccount1(),
				TestService.bankAccount.name(),
				classToStringHasher.classToString(Account1.class)
			))
			.addEntity(new TransactionEntity(
				entity.getBankAccount2(),
				TestService.bankAccount.name(),
				classToStringHasher.classToString(Account2.class)
			)), requestId, requestingUserId, payload);
	}
}
