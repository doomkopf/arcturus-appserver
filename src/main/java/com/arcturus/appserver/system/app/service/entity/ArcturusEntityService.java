package com.arcturus.appserver.system.app.service.entity;

import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;
import com.arcturus.api.service.entity.EntityService;
import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.system.SourceNodeIdProvider;
import com.arcturus.appserver.system.app.service.LocalService;
import com.arcturus.appserver.system.app.service.entity.transaction.Transaction;
import com.arcturus.appserver.system.message.DomainMessage;
import com.arcturus.appserver.system.message.DomainTransactionMessage;
import com.arcturus.appserver.system.message.ServiceMessage;

import java.util.UUID;

/**
 * @author doomkopf
 * @see EntityService
 */
public class ArcturusEntityService
{
	public static final String SERVICE_NAME_USER = "user";

	private final String appId;
	private final String name;
	private final Logger log;
	private final EntityIdToNodeBalancer entityIdToNodeBalancer;
	private final LocalEntityService<?> localEntityService;
	private final SourceNodeIdProvider sourceNodeIdProvider;
	private final JsonStringSerializer jsonStringSerializer;

	public ArcturusEntityService(
		String appId,
		String name,
		LoggerFactory loggerFactory,
		EntityIdToNodeBalancer entityIdToNodeBalancer,
		LocalEntityService<?> localEntityService,
		SourceNodeIdProvider sourceNodeIdProvider,
		JsonStringSerializer jsonStringSerializer
	)
	{
		this.appId = appId;
		this.name = name;
		log = loggerFactory.create(getClass());
		this.entityIdToNodeBalancer = entityIdToNodeBalancer;
		this.localEntityService = localEntityService;
		this.sourceNodeIdProvider = sourceNodeIdProvider;
		this.jsonStringSerializer = jsonStringSerializer;
	}

	public void send(String useCase, UUID id, long requestId, UUID requestingUserId, String payload)
	{
		UUID entityId;
		entityId = (id == null) ? UUID.randomUUID() : id;

		entityIdToNodeBalancer.determineNodeForEntityId(entityId, node ->
		{
			if (node == null)
			{
				log.log(LogLevel.info, "No node could be determined for entity id " + entityId);
				return;
			}

			if (node.isLocal())
			{
				if (log.isLogLevel(LogLevel.debug))
				{
					log.log(LogLevel.debug, "Determined local node for entityId " + entityId);
				}
				if (localEntityService == null)
				{
					log.log(LogLevel.info, "Determined local node but local service is null");
					return;
				}
				localEntityService.sendDomainMessage(useCase,
					entityId,
					requestId,
					requestingUserId,
					payload
				);
				return;
			}

			if (log.isLogLevel(LogLevel.debug))
			{
				log.log(LogLevel.debug, "Determined remote node for entityId " + entityId);
			}

			var msg = new ServiceMessage(appId,
				name,
				sourceNodeIdProvider.getSourceNodeId(requestId),
				new DomainMessage(useCase, entityId, requestId, requestingUserId, payload)
			);
			msg.sendToNode(node);
			if (log.isLogLevel(LogLevel.debug))
			{
				log.log(LogLevel.debug,
					"Sent "
						+ ServiceMessage.class.getSimpleName()
						+ " to remote node: "
						+ jsonStringSerializer.toJsonString(msg)
				);
			}
		});
	}

	public void sendTransaction(
		Transaction transaction,
		long requestId,
		UUID requestingUserId,
		String payload,
		int entityIndex
	)
	{
		var transactionEntity = transaction.getTransactionEntities()[entityIndex];

		UUID entityId;
		entityId = (transactionEntity.getId() == null) ?
			UUID.randomUUID() :
			transactionEntity.getId();

		entityIdToNodeBalancer.determineNodeForEntityId(entityId, node ->
		{
			if (node == null)
			{
				log.log(LogLevel.info, "No node could be determined for entity id " + entityId);
				return;
			}

			if (node.isLocal())
			{
				if (log.isLogLevel(LogLevel.debug))
				{
					log.log(LogLevel.debug, "Determined local node for entityId " + entityId);
				}
				if (localEntityService == null)
				{
					log.log(LogLevel.info, "Determined local node but local service is null");
					return;
				}
				localEntityService.sendDomainTransactionMessage(transaction,
					requestId,
					requestingUserId,
					payload,
					entityIndex
				);
				return;
			}

			if (log.isLogLevel(LogLevel.debug))
			{
				log.log(LogLevel.debug, "Determined remote node for entityId " + entityId);
			}

			new ServiceMessage(appId,
				name,
				sourceNodeIdProvider.getSourceNodeId(requestId),
				new DomainTransactionMessage(transaction,
					entityIndex,
					requestId,
					requestingUserId,
					payload
				)
			).sendToNode(node);
		});
	}

	public LocalService getLocalService()
	{
		return localEntityService;
	}

	public void shutdown() throws InterruptedException
	{
		if (localEntityService != null)
		{
			localEntityService.shutdown();
		}

		entityIdToNodeBalancer.shutdown();
	}
}