package com.arcturus.appserver.system.app.service.entity.transaction;

import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;
import com.arcturus.api.service.entity.UseCaseContext;
import com.arcturus.api.service.entity.transaction.EntityTransactionUseCaseHandler;
import com.arcturus.api.service.entity.transaction.ValidationResult;
import com.arcturus.appserver.system.Constants;
import com.arcturus.appserver.system.Message;
import com.arcturus.appserver.system.app.service.entity.EntityNanoProcess;
import com.arcturus.appserver.system.app.service.entity.EntityUseCaseProvider;
import com.arcturus.appserver.system.message.DomainTransactionMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The transaction component of an {@link EntityNanoProcess}.
 *
 * @author doomkopf
 */
public class EntityNanoProcessTransactionComponent<E>
{
	private final Logger log;

	private Transaction currentTransaction = null;
	private int currentTransactionEntityIndex;
	private List<Message> transactionQueuedMessages = null;

	public EntityNanoProcessTransactionComponent(LoggerFactory loggerFactory)
	{
		log = loggerFactory.create(getClass());
	}

	public boolean isTransactionOpen()
	{
		return currentTransaction != null;
	}

	/**
	 * @return True if a transaction is currently open
	 */
	public boolean handlePotentialTransaction(Message msg)
	{
		if (isTransactionOpen())
		{
			queueDomainMessageDuringTransaction(msg);
			return true;
		}

		return false;
	}

	public void handleDomainTransactionMessage(
		ArcturusTransactionManager transactionManager,
		EntityUseCaseProvider<E> useCaseProvider,
		EntityPersistencyContext entityPersistencyContext,
		DomainTypeMessageHandler domainTypeMessageHandler,
		DomainTransactionMessage msg,
		E entity,
		UUID id,
		long now
	)
	{
		var transaction = msg.getTransaction();
		if (transaction.isCancel() && (currentTransaction != null) && transaction.getId()
			.equals(currentTransaction.getId()))
		{
			closeTransaction(domainTypeMessageHandler);
			return;
		}

		if (currentTransaction == null)
		{
			currentTransaction = transaction;
			currentTransactionEntityIndex = msg.getCurrentEntityIndex();
		}
		else
		{
			if (!transaction.getId().equals(currentTransaction.getId())
				&& (!Transactions.checkForDeadlock(msg,
				currentTransaction,
				currentTransactionEntityIndex
			) || (transaction.getId()
				.hashCode() > currentTransaction.getId().hashCode())))
			{
				queueDomainMessageDuringTransaction(msg);
				return;
			}
		}

		var transactionEntity = msg.getCurrentTransactionEntity();

		var useCaseHandler = useCaseProvider.getTransactionUseCaseHandler(transactionEntity.getUseCase());
		if (useCaseHandler == null)
		{
			if (log.isLogLevel(LogLevel.error))
			{
				log.log(LogLevel.error, "Unknown UseCase: " + transactionEntity.getUseCase());
			}
			return;
		}

		if (!transaction.isInCommitPhase())
		{
			ValidationResult validationResult;
			try
			{
				validationResult = useCaseHandler.validate(entity,
					id,
					msg.getRequestId(),
					msg.getRequestingUserId(),
					msg.getPayload()
				);
			}
			catch (Throwable e)
			{
				validationResult = null;
				if (log.isLogLevel(LogLevel.error))
				{
					log.log(LogLevel.error, e);
				}
			}

			if ((validationResult == null) || !validationResult.ok)
			{
				transaction.setCancel();
				for (var i = 0; i < currentTransactionEntityIndex; i++)
				{
					transactionManager.handleTransaction(transaction,
						msg.getRequestId(),
						msg.getRequestingUserId(),
						null,
						i
					);
				}

				closeTransaction(domainTypeMessageHandler);
			}
			else
			{
				if (msg.isAtLastEntity())
				{
					transaction.setCommitPhase();
					for (var i = 0; i < (transaction.getTransactionEntities().length - 1); i++)
					{
						transactionManager.handleTransaction(transaction,
							msg.getRequestId(),
							msg.getRequestingUserId(),
							msg.getPayload(),
							i
						);
					}

					commit(useCaseHandler,
						entityPersistencyContext,
						domainTypeMessageHandler,
						msg.getRequestId(),
						msg.getRequestingUserId(),
						validationResult.payload,
						entity,
						id
					);
				}
				else
				{
					transactionManager.handleTransaction(transaction,
						msg.getRequestId(),
						msg.getRequestingUserId(),
						validationResult.payload,
						currentTransactionEntityIndex + 1
					);
				}
			}
		}
		else
		{
			commit(useCaseHandler,
				entityPersistencyContext,
				domainTypeMessageHandler,
				msg.getRequestId(),
				msg.getRequestingUserId(),
				msg.getPayload(),
				entity,
				id
			);
		}

		entityPersistencyContext.writeThroughIfDirty();

		var diffTime = System.currentTimeMillis() - now;
		if (diffTime >= Constants.SLOW_TASK_THRESHOLD_MILLIS_DEFAULT)
		{
			log.log(LogLevel.warn,
				"Usecase " + transactionEntity.getUseCase() + " took " + diffTime + "ms"
			);
		}
	}

	private void queueDomainMessageDuringTransaction(Message msg)
	{
		if (transactionQueuedMessages == null)
		{
			transactionQueuedMessages = new ArrayList<>(1);
		}

		transactionQueuedMessages.add(msg);
	}

	private void commit( // NOSONAR
		EntityTransactionUseCaseHandler<E> useCaseHandler,
		UseCaseContext useCaseContext,
		DomainTypeMessageHandler domainTypeMessageHandler,
		long requestId,
		UUID requestingUserId,
		String payload,
		E entity,
		UUID id
	)
	{
		try
		{
			useCaseHandler.commit(entity, id, requestId, requestingUserId, payload);
			useCaseContext.dirty();
		}
		catch (Throwable e)
		{
			if (log.isLogLevel(LogLevel.error))
			{
				log.log(LogLevel.error, e);
			}
		}

		closeTransaction(domainTypeMessageHandler);
	}

	private void closeTransaction(DomainTypeMessageHandler domainTypeMessageHandler)
	{
		currentTransaction = null;

		if (transactionQueuedMessages != null)
		{
			for (var msg : transactionQueuedMessages)
			{
				domainTypeMessageHandler.handleDomainTypeMessage(msg);
			}

			transactionQueuedMessages = null;
		}
	}
}