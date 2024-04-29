package com.arcturus.appserver.system.app.service.entity;

import com.arcturus.api.ArcturusAppException;
import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;
import com.arcturus.api.log.AppLogLevel;
import com.arcturus.api.service.entity.EntityFactory;
import com.arcturus.api.service.entity.EntityUpdater;
import com.arcturus.appserver.concurrent.nanoprocess.NanoProcess;
import com.arcturus.appserver.concurrent.nanoprocess.NanoProcessThread;
import com.arcturus.appserver.database.keyvaluestore.KeyValueStore;
import com.arcturus.appserver.database.keyvaluestore.StringKeyValueStore;
import com.arcturus.appserver.net.NetCodes;
import com.arcturus.appserver.system.ArcturusResponseSender;
import com.arcturus.appserver.system.ArcturusUserSender;
import com.arcturus.appserver.system.Constants;
import com.arcturus.appserver.system.Message;
import com.arcturus.appserver.system.app.logmessage.ArcturusAppLogger;
import com.arcturus.appserver.system.app.service.entity.transaction.ArcturusTransactionManager;
import com.arcturus.appserver.system.app.service.entity.transaction.DomainTypeMessageHandler;
import com.arcturus.appserver.system.app.service.entity.transaction.EntityNanoProcessTransactionComponent;
import com.arcturus.appserver.system.app.service.entity.transaction.EntityPersistencyContext;
import com.arcturus.appserver.system.message.DomainMessage;
import com.arcturus.appserver.system.message.EntityManagementMessage;
import com.arcturus.appserver.system.message.LocalManagementMessage;
import com.arcturus.appserver.system.message.management.EntityManagementMessageBehavior;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A {@link NanoProcess} isolating one single entity.
 *
 * @author doomkopf
 */
public class EntityNanoProcess<E> extends NanoProcess<Message>
	implements EntityPersistencyContext, DomainTypeMessageHandler
{
	static class DependencyContainer<E>
	{
		final EntityUseCaseProvider<E> useCaseProvider;
		final EntityUseCaseInfoProvider useCaseInfoProvider;
		final EntityUpdater<E> entityUpdater;
		final EntityFactory<E> entityFactory;
		final EntitySerializer<E> entitySerializer;
		final EntityInitializer<E> entityInitializer;
		final EntityMigrator entityMigrator;
		final StringKeyValueStore db;
		final ArcturusTransactionManager transactionManager;
		final LocalEntityService<E> localEntityService;
		final ArcturusResponseSender responseSender;
		final ArcturusUserSender userSender;
		final ArcturusAppLogger appLogger;
		final String service;
		final int entityVersion;

		DependencyContainer( // NOSONAR
			EntityUseCaseProvider<E> useCaseProvider,
			EntityUseCaseInfoProvider useCaseInfoProvider,
			EntityUpdater<E> entityUpdater,
			EntityFactory<E> entityFactory,
			EntitySerializer<E> entitySerializer,
			EntityInitializer<E> entityInitializer,
			EntityMigrator entityMigrator,
			StringKeyValueStore db,
			ArcturusTransactionManager transactionManager,
			LocalEntityService<E> localEntityService,
			ArcturusResponseSender responseSender,
			ArcturusUserSender userSender,
			ArcturusAppLogger appLogger,
			String service,
			int entityVersion
		)
		{
			this.useCaseProvider = useCaseProvider;
			this.useCaseInfoProvider = useCaseInfoProvider;
			this.entityUpdater = entityUpdater;
			this.entityFactory = entityFactory;
			this.entitySerializer = entitySerializer;
			this.entityInitializer = entityInitializer;
			this.entityMigrator = entityMigrator;
			this.db = db;
			this.transactionManager = transactionManager;
			this.localEntityService = localEntityService;
			this.responseSender = responseSender;
			this.userSender = userSender;
			this.appLogger = appLogger;
			this.service = service;
			this.entityVersion = entityVersion;
		}
	}

	private static final int HARD_RELEASE_DURATION_MILLIS = 1000 * 60 * 60;

	private final Logger log;
	private final DependencyContainer<E> dependencyContainer;
	private final UUID id;
	private final String strDocId;
	private final EntityNanoProcessTransactionComponent<E> transactionComponent;
	private final AtomicBoolean startedShutdown = new AtomicBoolean(false);

	private List<Message> initPhaseQueuedMessages = null;
	private boolean isInitialized = false;
	private E entity = null;
	private boolean isEntityDirty = false;
	private boolean isEntityCreated = false;
	private long lastMessageTime = System.currentTimeMillis();

	EntityNanoProcess(
		NanoProcessThread<Message> thread,
		LoggerFactory loggerFactory,
		DependencyContainer<E> dependencyContainer,
		UUID id,
		String strDocId
	)
	{
		super(thread, loggerFactory);

		this.dependencyContainer = dependencyContainer;

		log = loggerFactory.create(getClass());
		this.id = id;
		this.strDocId = strDocId;
		transactionComponent = new EntityNanoProcessTransactionComponent<>(loggerFactory);
	}

	void startInit()
	{
		if (dependencyContainer.db == null)
		{
			queueMessage(new LocalManagementMessage<>(EntityNanoProcess::init));
		}
		else
		{
			dependencyContainer.db.asyncGet(strDocId, (key, value) ->
			{
				if (value == null)
				{
					queueMessage(new LocalManagementMessage<>(EntityNanoProcess::init));
					return;
				}

				queueMessage(new LocalManagementMessage<>(proc ->
				{
					var migratedEntityJson = dependencyContainer.entityMigrator.migrateEntityIfNecessary(
						value);
					proc.entity = dependencyContainer.entitySerializer.entityFromString(
						migratedEntityJson);
					/*try
					{
						proc.entity = dependencyContainer.entityInitializer.initializeEntity((E) proc.entity);
					}
					catch (ArcturusAppException e)
					{
						if (log.isLogLevel(LogLevel.debug))
						{
							log.log(LogLevel.debug, e);
						}
						proc.entity = null;
					}*/

					proc.init();
				}));
			});
		}
	}

	private void init()
	{
		if (initPhaseQueuedMessages != null)
		{
			for (var msg : initPhaseQueuedMessages)
			{
				handleDomainTypeMessage(msg);
			}
			initPhaseQueuedMessages = null;
		}

		isInitialized = true;
	}

	@Override
	public void dirty()
	{
		isEntityDirty = true;
	}

	@Override
	public boolean isDirty()
	{
		return isEntityDirty;
	}

	@Override
	public void remove()
	{
		entity = null;
		isEntityDirty = false;
		dependencyContainer.db.asyncRemove(strDocId, KeyValueStore.NOOP_REMOVE_RESULT_HANDLER);
	}

	@Override
	public boolean isEntityCreated()
	{
		return isEntityCreated;
	}

	@Override
	protected void handleMessage(Message msg)
	{
		var localManagementMessage = (LocalManagementMessage<E>) msg.getLocalManagementMessage();
		if (localManagementMessage != null)
		{
			localManagementMessage.execute(this);
			return;
		}

		var managementMessage = msg.getManagementMessage();
		if (managementMessage != null)
		{
			managementMessage.getBehavior().execute(managementMessage, this);
			return;
		}

		if (!isInitialized)
		{
			if (initPhaseQueuedMessages == null)
			{
				initPhaseQueuedMessages = new ArrayList<>(1);
			}
			initPhaseQueuedMessages.add(msg);
			return;
		}

		handleDomainTypeMessage(msg);
	}

	@Override
	public void handleDomainTypeMessage(Message msg)
	{
		var now = System.currentTimeMillis();
		lastMessageTime = now;

		var domainMsg = msg.getDomainMessage();
		if (domainMsg != null)
		{
			if (transactionComponent.handlePotentialTransaction(msg))
			{
				return;
			}

			handleDomainMessage(domainMsg, now);
			return;
		}

		var domainTransactionMessage = msg.getDomainTransactionMessage();
		if (domainTransactionMessage != null)
		{
			transactionComponent.handleDomainTransactionMessage(
				dependencyContainer.transactionManager,
				dependencyContainer.useCaseProvider,
				this,
				this,
				domainTransactionMessage,
				entity,
				id,
				now
			);
			return;
		}
	}

	private void handleDomainMessage(DomainMessage msg, long now)
	{
		var useCaseHandler = dependencyContainer.useCaseProvider.getUseCaseHandler(msg.getUseCase());
		if (useCaseHandler == null)
		{
			if (log.isLogLevel(LogLevel.debug))
			{
				log.log(LogLevel.debug, "Unknown UseCase: " + msg.getUseCase());
			}
			return;
		}

		try
		{
			if ((entity == null)
				&& dependencyContainer.useCaseInfoProvider.isCreateEntity(msg.getUseCase()))
			{
				entity = dependencyContainer.entityFactory.createDefaultEntity(msg.getId());
				isEntityCreated = true;
			}
			else
			{
				isEntityCreated = false;
			}
			useCaseHandler.handle(
				entity,
				msg.getId(),
				msg.getRequestId(),
				msg.getRequestingUserId(),
				msg.getPayload(),
				this
			);
		}
		catch (Throwable e)
		{
			if (log.isLogLevel(LogLevel.error))
			{
				log.log(
					LogLevel.error,
					"Error in service="
						+ dependencyContainer.service
						+ ", usecase="
						+ msg.getUseCase(),
					e
				);
			}

			if (dependencyContainer.appLogger != null)
			{
				dependencyContainer.appLogger.log(AppLogLevel.ERROR, e.getMessage());
			}

			NetCodes.sendErrorToPotentialClient(
				dependencyContainer.responseSender,
				dependencyContainer.userSender,
				msg.getRequestId(),
				msg.getRequestingUserId(),
				dependencyContainer.service,
				msg.getUseCase(),
				e
			);
		}

		writeThroughIfDirty();

		var diffTime = System.currentTimeMillis() - now;
		if (diffTime >= Constants.SLOW_TASK_THRESHOLD_MILLIS_DEFAULT)
		{
			log.log(LogLevel.warn, "Usecase " + msg.getUseCase() + " took " + diffTime + "ms");
		}
	}

	@Override
	public void writeThroughIfDirty()
	{
		if ((entity != null) && isEntityDirty)
		{
			dependencyContainer.db.asyncPut(
				strDocId,
				dependencyContainer.entitySerializer.entityToString(entity,
					dependencyContainer.entityVersion
				),
				KeyValueStore.NOOP_PUT_RESULT_HANDLER
			);

			isEntityDirty = false;
		}
	}

	private void checkForAndHandleTooLongSurvival(long now)
	{
		if ((now - lastMessageTime) > HARD_RELEASE_DURATION_MILLIS)
		{
			shutdown(false);
		}
	}

	@Override
	protected void handleScheduled(long now, long deltaTime)
	{
		if ((dependencyContainer.entityUpdater != null)
			&& isInitialized
			&& !transactionComponent.isTransactionOpen())
		{
			try
			{
				dependencyContainer.entityUpdater.update(entity, id, now, deltaTime, this);
			}
			catch (ArcturusAppException e)
			{
				if (log.isLogLevel(LogLevel.error))
				{
					log.log(LogLevel.error, e);
				}
			}
		}

		checkForAndHandleTooLongSurvival(now);
	}

	/**
	 * Must be called in its own context.
	 */
	public void shutdown(boolean handleRemainingMessages)
	{
		dependencyContainer.localEntityService.removeNanoProcessForEntityId(id);

		kill(handleRemainingMessages);

		if (isInitialized)
		{
			writeThroughIfDirty();
		}
	}

	void startShutdown()
	{
		if (!startedShutdown.getAndSet(true))
		{
			queueMessage(new EntityManagementMessage(EntityManagementMessageBehavior.kill, id));
		}
	}
}
