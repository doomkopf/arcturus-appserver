package com.arcturus.appserver.system.app.service.entity;

import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;
import com.arcturus.api.service.entity.EntityFactory;
import com.arcturus.api.service.entity.EntityUpdater;
import com.arcturus.appserver.concurrent.nanoprocess.NanoProcessSystem;
import com.arcturus.appserver.database.DocumentKeys;
import com.arcturus.appserver.database.keyvaluestore.StringKeyValueStore;
import com.arcturus.appserver.system.ArcturusResponseSender;
import com.arcturus.appserver.system.ArcturusUserSender;
import com.arcturus.appserver.system.Message;
import com.arcturus.appserver.system.app.logmessage.ArcturusAppLogger;
import com.arcturus.appserver.system.app.service.LocalService;
import com.arcturus.appserver.system.app.service.entity.EntityNanoProcess.DependencyContainer;
import com.arcturus.appserver.system.app.service.entity.transaction.ArcturusTransactionManager;
import com.arcturus.appserver.system.app.service.entity.transaction.Transaction;
import com.arcturus.appserver.system.app.service.info.EntityServiceInfo;
import com.arcturus.appserver.system.message.DomainMessage;
import com.arcturus.appserver.system.message.DomainTransactionMessage;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The local (real) version of an {@link ArcturusEntityService}.
 *
 * @author doomkopf
 */
public class LocalEntityService<E> implements LocalService
{
	private static final int SHUTDOWN_LOOP_CYCLE_SLEEP_MILLIS = 100;
	private static final int SHUTDOWN_MAX_LOOP_CYCLES = 10;

	private final Logger log;
	private final String appId;
	private final String serviceName;
	private final LoggerFactory loggerFactory;
	private final NanoProcessSystem<Message> nanoProcessSystem;

	private final DependencyContainer<E> dependencyContainer;

	private final Map<UUID, EntityNanoProcess<E>> idToNanoProcessMap = new ConcurrentHashMap<>();

	public LocalEntityService( // NOSONAR
		String appId,
		String serviceName,
		int entityVersion,
		LoggerFactory loggerFactory,
		NanoProcessSystem<Message> nanoProcessSystem,
		EntityUseCaseProvider<E> useCaseProvider,
		EntityUpdater<E> entityUpdater,
		EntityFactory<E> entityFactory,
		EntitySerializer<E> entitySerializer,
		EntityInitializer<E> entityInitializer,
		EntityMigrator entityMigrator,
		StringKeyValueStore db,
		ArcturusTransactionManager transactionManager,
		EntityServiceInfo entityServiceInfo,
		ArcturusResponseSender responseSender,
		ArcturusUserSender userSender,
		ArcturusAppLogger appLogger
	)
	{
		log = loggerFactory.create(getClass());
		this.appId = appId;
		this.serviceName = serviceName;
		this.loggerFactory = loggerFactory;
		this.nanoProcessSystem = nanoProcessSystem;

		dependencyContainer = new DependencyContainer<>(useCaseProvider,
			entityServiceInfo,
			entityUpdater,
			entityFactory,
			entitySerializer,
			entityInitializer,
			entityMigrator,
			db,
			transactionManager,
			this,
			responseSender,
			userSender,
			appLogger,
			serviceName,
			entityVersion
		);
	}

	@Override
	public void send(Message msg)
	{
		var domainMessage = msg.getDomainMessage();
		if (domainMessage != null)
		{
			sendDomainMessage(domainMessage.getUseCase(),
				domainMessage.getId(),
				domainMessage.getRequestId(),
				domainMessage.getRequestingUserId(),
				domainMessage.getPayload()
			);
			if (log.isLogLevel(LogLevel.debug))
			{
				log.log(LogLevel.debug,
					"Received "
						+ DomainMessage.class.getSimpleName()
						+ " with payload: "
						+ domainMessage.getPayload()
				);
			}
			return;
		}

		var managementMessage = msg.getManagementMessage();
		if (managementMessage != null)
		{
			var nanoProcess = idToNanoProcessMap.get(managementMessage.getEntityId());
			if (nanoProcess != null)
			{
				nanoProcess.queueMessage(managementMessage);
			}
			return;
		}

		var domainTransactionMessage = msg.getDomainTransactionMessage();
		if (domainTransactionMessage != null)
		{
			sendDomainTransactionMessage(domainTransactionMessage.getTransaction(),
				domainTransactionMessage.getRequestId(),
				domainTransactionMessage.getRequestingUserId(),
				domainTransactionMessage.getPayload(),
				domainTransactionMessage.getCurrentEntityIndex()
			);
			if (log.isLogLevel(LogLevel.debug))
			{
				log.log(LogLevel.debug,
					"Received "
						+ DomainTransactionMessage.class.getSimpleName()
						+ " with payload: "
						+ domainTransactionMessage.getPayload()
				);
			}
			return;
		}
	}

	private EntityNanoProcess<E> getOrCreateEntityNanoProcess(UUID id)
	{
		var nanoProcess = idToNanoProcessMap.get(id);
		if (nanoProcess == null)
		{
			nanoProcess = idToNanoProcessMap.computeIfAbsent(id, i ->
			{
				var newNanoProcess = (EntityNanoProcess<E>) nanoProcessSystem.addProc(thread -> new EntityNanoProcess<>(
					thread,
					loggerFactory,
					dependencyContainer,
					id,
					DocumentKeys.entity(appId, serviceName, id.toString())
				));
				newNanoProcess.startInit();
				return newNanoProcess;
			});
		}

		return nanoProcess;
	}

	public void sendDomainMessage(
		String useCase, UUID id, long requestId, UUID requestingUserId, String payload
	)
	{
		getOrCreateEntityNanoProcess(id).queueMessage(new DomainMessage(useCase,
			id,
			requestId,
			requestingUserId,
			payload
		));
	}

	public void sendDomainTransactionMessage(
		Transaction transaction,
		long requestId,
		UUID requestingUserId,
		String payload,
		int entityIndex
	)
	{
		getOrCreateEntityNanoProcess(transaction.getTransactionEntities()[entityIndex].getId()).queueMessage(
			new DomainTransactionMessage(transaction,
				entityIndex,
				requestId,
				requestingUserId,
				payload
			));
	}

	public void removeNanoProcessForEntityId(UUID id)
	{
		idToNanoProcessMap.remove(id);
	}

	public void shutdown() throws InterruptedException
	{
		var c = 0;
		while (!idToNanoProcessMap.isEmpty())
		{
			for (var proc : idToNanoProcessMap.values())
			{
				proc.startShutdown();
			}

			Thread.sleep(SHUTDOWN_LOOP_CYCLE_SLEEP_MILLIS);

			if (++c >= SHUTDOWN_MAX_LOOP_CYCLES)
			{
				break;
			}
		}

		if (c >= SHUTDOWN_MAX_LOOP_CYCLES)
		{
			log.log(LogLevel.warn,
				"Shutting down service " + appId + ":" + serviceName + " timed out"
			);
		}
	}
}