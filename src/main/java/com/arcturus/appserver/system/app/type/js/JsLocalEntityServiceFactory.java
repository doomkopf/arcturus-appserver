package com.arcturus.appserver.system.app.type.js;

import com.arcturus.api.LoggerFactory;
import com.arcturus.api.service.entity.EntityUseCaseHandler;
import com.arcturus.api.service.entity.aggregation.MappingEntityUseCaseHandler;
import com.arcturus.api.service.entity.transaction.EntityTransactionUseCaseHandler;
import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.concurrent.nanoprocess.NanoProcessSystem;
import com.arcturus.appserver.database.keyvaluestore.StringKeyValueStore;
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
import com.arcturus.appserver.system.app.type.js.script.DomainAppScriptProvider;

import java.util.HashMap;
import java.util.Map;

public class JsLocalEntityServiceFactory implements LocalEntityServiceFactory<JsonString>
{
	private final String appId;
	private final LoggerFactory loggerFactory;
	private final StringKeyValueStore db;
	private final NanoProcessSystem<Message> nanoProcessSystem;
	private final JsonFactory jsonFactory;
	private final DomainAppScriptProvider domainAppScriptProvider;
	private final JsServiceInfos serviceInfos;
	private final ArcturusResponseSender responseSender;
	private final ArcturusUserSender userSender;
	private final ArcturusTransactionManager transactionManager;
	private final JsonStringSerializer jsonStringSerializer;
	private final AggregationEntityServiceProvider aggregationEntityServiceProvider;
	private final ArcturusAppLogger appLogger;

	public JsLocalEntityServiceFactory(
		@AppId
			String appId,
		LoggerFactory loggerFactory,
		StringKeyValueStore db,
		NanoProcessSystem<Message> nanoProcessSystem,
		JsonFactory jsonFactory,
		DomainAppScriptProvider domainAppScriptProvider,
		JsServiceInfos serviceInfos,
		ArcturusResponseSender responseSender,
		ArcturusUserSender userSender,
		ArcturusTransactionManager transactionManager,
		JsonStringSerializer jsonStringSerializer,
		AggregationEntityServiceProvider aggregationEntityServiceProvider,
		ArcturusAppLogger appLogger
	)
	{
		this.appId = appId;
		this.loggerFactory = loggerFactory;
		this.db = db;
		this.nanoProcessSystem = nanoProcessSystem;
		this.jsonFactory = jsonFactory;
		this.domainAppScriptProvider = domainAppScriptProvider;
		this.serviceInfos = serviceInfos;
		this.responseSender = responseSender;
		this.userSender = userSender;
		this.transactionManager = transactionManager;
		this.jsonStringSerializer = jsonStringSerializer;
		this.aggregationEntityServiceProvider = aggregationEntityServiceProvider;
		this.appLogger = appLogger;
	}

	@Override
	public LocalEntityService<JsonString> create(String name)
	{
		var jsEntityManagement = new JsEntityManagement(domainAppScriptProvider, jsonFactory, name);

		var entityUpdater = serviceInfos.hasUpdateFunction(name) ?
			new JsEntityUpdater(domainAppScriptProvider, name) :
			null;

		var serviceInfo = serviceInfos.getEntityServiceInfoByName(name);

		var useCases = new HashMap<String, EntityUseCaseHandler<JsonString>>(serviceInfo.getUseCasesCount());
		useCases.put(InternalUseCases.AGGREGATE_ENTITY, new AggregateEntity<>(loggerFactory,
			jsonStringSerializer,
			aggregationEntityServiceProvider.get(),
			new MappingEntityUseCaseProvider<>(createEntityMappers(name)),
			responseSender
		));
		for (var useCase : serviceInfo.getUseCasesIterable())
		{
			useCases.put(useCase.getId(), new JsEntityUseCaseHandler(domainAppScriptProvider,
				name,
				useCase.isPublic(),
				useCase.isCreateEntity(),
				useCase.getId(),
				serviceInfos.useCaseHasFunc(name, useCase.getId())
			));
		}

		var transactionUseCases = new HashMap<String, EntityTransactionUseCaseHandler<JsonString>>(
			serviceInfo.getTransactionUseCasesCount());
		for (var useCase : serviceInfo.getTransactionUseCasesIterable())
		{
			transactionUseCases.put(useCase.getId(),
				new JsEntityTransactionUseCaseHandler(domainAppScriptProvider,
					name,
					useCase.getId(),
					jsonFactory
				)
			);
		}

		var entityVersion = serviceInfos.getEntityVersion(name);

		return new LocalEntityService<>(appId,
			name,
			entityVersion,
			loggerFactory,
			nanoProcessSystem,
			new EntityUseCaseProvider<>(useCases, transactionUseCases),
			entityUpdater,
			jsEntityManagement,
			jsEntityManagement,
			jsEntityManagement,
			new EntityMigrator(loggerFactory, entityVersion, jsEntityManagement, jsonFactory),
			db,
			transactionManager,
			serviceInfos.getEntityServiceInfoByName(name),
			responseSender,
			userSender,
			appLogger
		);
	}

	private Map<String, MappingEntityUseCaseHandler<JsonString>> createEntityMappers(String serviceName)
	{
		var mappingEntityUseCaseHandlerMap = new HashMap<String, MappingEntityUseCaseHandler<JsonString>>();

		for (var useCaseId : serviceInfos.getMappingUseCaseIds(serviceName))
		{
			mappingEntityUseCaseHandlerMap.put(useCaseId,
				new JsMappingEntityUseCaseHandler(domainAppScriptProvider, serviceName, useCaseId)
			);
		}

		return mappingEntityUseCaseHandlerMap;
	}
}