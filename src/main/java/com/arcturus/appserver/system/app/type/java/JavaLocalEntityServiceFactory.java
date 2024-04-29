package com.arcturus.appserver.system.app.type.java;

import com.arcturus.api.ArcturusAppException;
import com.arcturus.api.LoggerFactory;
import com.arcturus.api.service.ServiceAssignment;
import com.arcturus.api.service.entity.EntityMigration;
import com.arcturus.api.service.entity.EntityUpdater;
import com.arcturus.api.service.entity.EntityUseCaseHandler;
import com.arcturus.api.service.entity.aggregation.MappingEntityUseCase;
import com.arcturus.api.service.entity.aggregation.MappingEntityUseCaseHandler;
import com.arcturus.api.service.entity.transaction.EntityTransactionUseCaseHandler;
import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.concurrent.nanoprocess.NanoProcessSystem;
import com.arcturus.appserver.database.keyvaluestore.StringKeyValueStore;
import com.arcturus.appserver.inject.Injector;
import com.arcturus.appserver.json.JsonFactory;
import com.arcturus.appserver.system.ArcturusResponseSender;
import com.arcturus.appserver.system.ArcturusUserSender;
import com.arcturus.appserver.system.InternalUseCases;
import com.arcturus.appserver.system.Message;
import com.arcturus.appserver.system.app.inject.AppId;
import com.arcturus.appserver.system.app.logmessage.ArcturusAppLogger;
import com.arcturus.appserver.system.app.service.entity.EntityMigrator;
import com.arcturus.appserver.system.app.service.entity.EntityUseCaseProvider;
import com.arcturus.appserver.system.app.service.entity.LocalEntityService;
import com.arcturus.appserver.system.app.service.entity.LocalEntityServiceFactory;
import com.arcturus.appserver.system.app.service.entity.aggregation.entityservice.AggregationEntityServiceProvider;
import com.arcturus.appserver.system.app.service.entity.aggregation.usecase.AggregateEntity;
import com.arcturus.appserver.system.app.service.entity.mapping.MappingEntityUseCaseProvider;
import com.arcturus.appserver.system.app.service.entity.transaction.ArcturusTransactionManager;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;

public class JavaLocalEntityServiceFactory<T> implements LocalEntityServiceFactory<T>
{
	private static class EntityFactoryAndType<T>
	{
		final JavaEntityFactory<T> entityFactory;
		final Class<T> type;

		EntityFactoryAndType(JavaEntityFactory<T> entityFactory, Class<T> type)
		{
			this.entityFactory = entityFactory;
			this.type = type;
		}
	}

	private final String appId;
	private final LoggerFactory loggerFactory;
	private final StringKeyValueStore db;
	private final NanoProcessSystem<Message> nanoProcessSystem;
	private final JsonStringSerializer jsonStringSerializer;
	private final JsonFactory jsonFactory;
	private final AppClasses appClasses;
	private final Injector appInjector;
	private final JavaServiceInfos serviceInfos;
	private final ArcturusResponseSender responseSender;
	private final ArcturusUserSender userSender;
	private final AggregationEntityServiceProvider aggregationEntityServiceProvider;
	private final ArcturusTransactionManager transactionManager;
	private final ArcturusAppLogger appLogger;

	public JavaLocalEntityServiceFactory( // NOSONAR
		@AppId
			String appId,
		LoggerFactory loggerFactory,
		StringKeyValueStore db,
		NanoProcessSystem<Message> nanoProcessSystem,
		JsonStringSerializer jsonStringSerializer,
		JsonFactory jsonFactory,
		AppClasses appClasses,
		Injector appInjector,
		JavaServiceInfos serviceInfos,
		ArcturusResponseSender responseSender,
		ArcturusUserSender userSender,
		AggregationEntityServiceProvider aggregationEntityServiceProvider,
		ArcturusTransactionManager transactionManager,
		ArcturusAppLogger appLogger
	)
	{
		this.appId = appId;
		this.loggerFactory = loggerFactory;
		this.db = db;
		this.nanoProcessSystem = nanoProcessSystem;
		this.jsonStringSerializer = jsonStringSerializer;
		this.jsonFactory = jsonFactory;
		this.appClasses = appClasses;
		this.appInjector = appInjector;
		this.serviceInfos = serviceInfos;
		this.responseSender = responseSender;
		this.userSender = userSender;
		this.aggregationEntityServiceProvider = aggregationEntityServiceProvider;
		this.transactionManager = transactionManager;
		this.appLogger = appLogger;
	}

	@Override
	public LocalEntityService<T> create(String name) throws ArcturusAppException
	{
		var entityFactoryAndType = findAndCreateEntityFactoryAndType(name);

		var mappingUseCaseHandlers = findAndMappingUseCaseHandlers(name);

		var useCaseHandlers = createUseCaseHandlers(name);
		useCaseHandlers.put(InternalUseCases.AGGREGATE_ENTITY, new AggregateEntity<>(loggerFactory,
			jsonStringSerializer,
			aggregationEntityServiceProvider.get(),
			new MappingEntityUseCaseProvider<>(mappingUseCaseHandlers),
			responseSender
		));

		var entityVersion = entityFactoryAndType.entityFactory.getCurrentVersion();

		return new LocalEntityService<>(appId,
			name,
			entityVersion,
			loggerFactory,
			nanoProcessSystem,
			new EntityUseCaseProvider<>(useCaseHandlers, createTransactionUseCaseHandlers(name)),
			findAndCreateEntityUpdater(name),
			entityFactoryAndType.entityFactory,
			new JavaEntitySerializer<>(jsonStringSerializer, entityFactoryAndType.type),
			new JavaEntityInitializer<>(),
			new EntityMigrator(loggerFactory,
				entityVersion,
				findAndCreateEntityMigration(),
				jsonFactory
			),
			db,
			transactionManager,
			serviceInfos.getEntityServiceInfoByName(name),
			responseSender,
			userSender,
			appLogger
		);
	}

	private Map<String, EntityUseCaseHandler<T>> createUseCaseHandlers(String serviceName)
	{
		var useCaseHandlers = new HashMap();

		for (var entityUseCaseInfo : serviceInfos.getEntityServiceInfoByName(serviceName)
			.getUseCasesIterable())
		{
			useCaseHandlers.put(entityUseCaseInfo.getId(),
				appInjector.getInstance(serviceInfos.getEntityUseCaseHandlerClass(serviceName,
					entityUseCaseInfo.getId()
				))
			);
		}

		return useCaseHandlers;
	}

	private Map<String, EntityTransactionUseCaseHandler<T>> createTransactionUseCaseHandlers(
		String serviceName
	)
	{
		var useCaseHandlers = new HashMap();

		for (var entityTransactionUseCaseInfo : serviceInfos.getEntityServiceInfoByName(serviceName)
			.getTransactionUseCasesIterable())
		{
			useCaseHandlers.put(entityTransactionUseCaseInfo.getId(),
				appInjector.getInstance(serviceInfos.getEntityTransactionUseCaseHandlerClass(
					serviceName,
					entityTransactionUseCaseInfo.getId()
				))
			);
		}

		return useCaseHandlers;
	}

	@SuppressWarnings("unchecked")
	private EntityFactoryAndType<T> findAndCreateEntityFactoryAndType(String serviceName)
		throws ArcturusAppException
	{
		for (var clazz : appClasses.getClassesIterable())
		{
			if (JavaEntityFactory.class.isAssignableFrom(clazz))
			{
				var serviceAssignment = clazz.getAnnotation(ServiceAssignment.class);
				if (serviceAssignment != null)
				{
					if (serviceAssignment.service().equals(serviceName))
					{
						var interfaceType = (ParameterizedType) clazz.getGenericInterfaces()[0];
						var entityType = interfaceType.getActualTypeArguments()[0];
						var entityFactory = (JavaEntityFactory<T>) appInjector.getInstance(clazz);
						try
						{
							return new EntityFactoryAndType(entityFactory,
								Class.forName(entityType.getTypeName())
							);
						}
						catch (ClassNotFoundException e)
						{
							e.printStackTrace(); // TODO
						}
					}
				}
				else
				{
					// TODO log warn
				}
			}
		}

		throw new ArcturusAppException("Couldn't find " + JavaEntityFactory.class.getSimpleName());
	}

	@SuppressWarnings("unchecked")
	private EntityUpdater<T> findAndCreateEntityUpdater(String serviceName)
	{
		for (var clazz : appClasses.getClassesIterable())
		{
			if (EntityUpdater.class.isAssignableFrom(clazz))
			{
				var serviceAssignment = clazz.getAnnotation(ServiceAssignment.class);
				if (serviceAssignment != null)
				{
					if (serviceAssignment.service().equals(serviceName))
					{
						return (EntityUpdater<T>) appInjector.getInstance(clazz);
					}
				}
				else
				{
					// TODO log warn
				}
			}
		}

		return null;
	}

	private Map<String, MappingEntityUseCaseHandler<T>> findAndMappingUseCaseHandlers(String serviceName)
	{
		var useCaseHandlers = new HashMap();

		for (var clazz : appClasses.getClassesIterable())
		{
			if (MappingEntityUseCaseHandler.class.isAssignableFrom(clazz))
			{
				var mappingEntityUseCase = clazz.getAnnotation(MappingEntityUseCase.class);
				if (mappingEntityUseCase != null)
				{
					if (mappingEntityUseCase.service().equals(serviceName))
					{
						useCaseHandlers.put(mappingEntityUseCase.id(),
							appInjector.getInstance(clazz)
						);
					}
				}
				else
				{
					// TODO log warn
				}
			}
		}

		return useCaseHandlers;
	}

	@SuppressWarnings("static-method")
	private EntityMigration findAndCreateEntityMigration()
	{
		return null; // TODO
	}
}