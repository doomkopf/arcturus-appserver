package com.arcturus.appserver.system.app.service.entity.aggregation.entityservice;

import com.arcturus.api.LoggerFactory;
import com.arcturus.api.service.entity.EntityUseCaseHandler;
import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.cluster.Cluster;
import com.arcturus.appserver.cluster.hazelcast.SharedHazelcastInstance;
import com.arcturus.appserver.concurrent.nanoprocess.NanoProcessSystem;
import com.arcturus.appserver.config.Config;
import com.arcturus.appserver.json.JsonFactory;
import com.arcturus.appserver.system.*;
import com.arcturus.appserver.system.app.service.ServiceClusterConfigProvider;
import com.arcturus.appserver.system.app.service.UseCaseProcessor;
import com.arcturus.appserver.system.app.service.UserEntityServiceProvider;
import com.arcturus.appserver.system.app.service.entity.*;
import com.arcturus.appserver.system.app.service.entity.aggregation.usecase.AggregateEntityIds;
import com.arcturus.appserver.system.app.service.entity.aggregation.usecase.HandleFinishedAggregations;
import com.arcturus.appserver.system.app.service.info.EntityServiceInfo;
import com.arcturus.appserver.system.app.service.info.EntityUseCaseInfo;

import java.net.UnknownHostException;
import java.util.HashMap;

public class AggregationEntityServiceProvider
{
	public static final String SERVICE_NAME = "_agg";
	private static final int ENTITY_VERSION = 1;

	private final ArcturusEntityService entityService;

	public AggregationEntityServiceProvider(
		String appId,
		LoggerFactory loggerFactory,
		NanoProcessSystem<Message> nanoProcessSystem,
		JsonFactory jsonFactory,
		ArcturusResponseSender responseSender,
		ArcturusUserSender userSender,
		SharedHazelcastInstance sharedHazelcastInstance,
		Cluster cluster,
		Config config,
		SourceNodeIdProvider sourceNodeIdProvider,
		JsonStringSerializer jsonStringSerializer,
		UseCaseProcessor useCaseProcessor,
		UserEntityServiceProvider serviceProvider
	) throws UnknownHostException
	{
		var idToUseCaseMap = new HashMap<String, EntityUseCaseHandler<AggregationContext>>();
		var idToUseCaseInfoMap = new HashMap<String, EntityUseCaseInfo>();

		var localEntityService = new LocalEntityService<>(appId,
			SERVICE_NAME,
			ENTITY_VERSION,
			loggerFactory,
			nanoProcessSystem,
			new EntityUseCaseProvider<>(idToUseCaseMap, null),
			null,
			id -> new AggregationContext(),
			null,
			null,
			new EntityMigrator(loggerFactory, ENTITY_VERSION, null, jsonFactory),
			null,
			null,
			new EntityServiceInfo(SERVICE_NAME, idToUseCaseInfoMap, null),
			responseSender,
			userSender,
			null
		);

		var nodeIdentityList = new NodeIdentityList(
			ServiceClusterConfigProvider.DEFAULT_CONFIG.isIncludingNodes(),
			ServiceClusterConfigProvider.DEFAULT_CONFIG.getNodes()
		);

		entityService = new ArcturusEntityService(appId,
			SERVICE_NAME,
			loggerFactory,
			new EntityIdToNodeBalancer(loggerFactory,
				appId,
				SERVICE_NAME,
				sharedHazelcastInstance,
				config,
				cluster,
				localEntityService,
				nodeIdentityList
			),
			localEntityService,
			sourceNodeIdProvider,
			jsonStringSerializer
		);

		idToUseCaseMap.put(AggregationUseCase.agg.name(),
			new AggregateEntityIds(jsonStringSerializer, serviceProvider, useCaseProcessor)
		);
		idToUseCaseInfoMap.put(AggregationUseCase.agg.name(),
			new EntityUseCaseInfo(AggregationUseCase.agg.name(), true, false, null, null, null)
		);

		idToUseCaseMap.put(AggregationUseCase.fin.name(),
			new HandleFinishedAggregations(useCaseProcessor, jsonStringSerializer)
		);
		idToUseCaseInfoMap.put(AggregationUseCase.fin.name(),
			new EntityUseCaseInfo(AggregationUseCase.fin.name(), false, false, null, null, null)
		);
	}

	public ArcturusEntityService get()
	{
		return entityService;
	}

	void shutdown() throws InterruptedException
	{
		entityService.shutdown();
	}
}