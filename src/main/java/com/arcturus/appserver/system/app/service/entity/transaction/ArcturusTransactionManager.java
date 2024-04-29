package com.arcturus.appserver.system.app.service.entity.transaction;

import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;
import com.arcturus.api.service.entity.transaction.TransactionBuilder;
import com.arcturus.api.service.entity.transaction.TransactionEntity;
import com.arcturus.appserver.system.app.service.UserEntityServiceProvider;
import com.arcturus.appserver.system.app.service.entity.ArcturusEntityService;

import java.util.UUID;

public class ArcturusTransactionManager
{
	private static final TransactionEntity[] TRANSACTION_ENTITIES = new TransactionEntity[0];

	private final Logger log;
	private final UserEntityServiceProvider serviceProvider;

	public ArcturusTransactionManager(
		LoggerFactory loggerFactory, UserEntityServiceProvider serviceProvider
	)
	{
		log = loggerFactory.create(getClass());
		this.serviceProvider = serviceProvider;
	}

	void handleTransaction(
		Transaction transaction,
		long requestId,
		UUID requestingUserId,
		String payload,
		int entityIndex
	)
	{
		var transactionEntity = transaction.getTransactionEntities()[entityIndex];
		var entityService = serviceProvider.getServiceByName(transactionEntity.getService());
		if (entityService == null)
		{
			log.log(LogLevel.info,
				transactionEntity.getService()
					+ " is not an "
					+ ArcturusEntityService.class.getSimpleName()
			);
			return;
		}

		entityService.sendTransaction(transaction,
			requestId,
			requestingUserId,
			payload,
			entityIndex
		);
	}

	public void startTransaction(
		TransactionBuilder transactionBuilder, long requestId, UUID requestingUserId, String payload
	)
	{
		handleTransaction(new Transaction(UUID.randomUUID(),
			transactionBuilder.entities.toArray(TRANSACTION_ENTITIES),
			false,
			false
		), requestId, requestingUserId, payload, 0);
	}
}