package com.arcturus.appserver.system.app.type.java;

import java.util.UUID;

import com.arcturus.api.service.entity.transaction.TransactionBuilder;
import com.arcturus.api.service.entity.transaction.TransactionManager;
import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.system.app.service.entity.transaction.ArcturusTransactionManager;

public class JavaTransactionManager implements TransactionManager
{
	private final ArcturusTransactionManager transactionManager;
	private final JsonStringSerializer jsonStringSerializer;

	public JavaTransactionManager(
			ArcturusTransactionManager transactionManager,
			JsonStringSerializer jsonStringSerializer)
	{
		this.transactionManager = transactionManager;
		this.jsonStringSerializer = jsonStringSerializer;
	}

	@Override
	public void startTransaction(
			TransactionBuilder transactionBuilder,
			long requestId,
			UUID requestingUserId,
			String payload)
	{
		transactionManager
				.startTransaction(transactionBuilder, requestId, requestingUserId, payload);
	}

	@Override
	public void startTransactionObject(
			TransactionBuilder transactionBuilder,
			long requestId,
			UUID requestingUserId,
			Object payload)
	{
		startTransaction(
				transactionBuilder,
				requestId,
				requestingUserId,
				jsonStringSerializer.toJsonString(payload));
	}
}