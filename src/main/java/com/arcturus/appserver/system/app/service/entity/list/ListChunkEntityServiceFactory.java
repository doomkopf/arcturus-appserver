package com.arcturus.appserver.system.app.service.entity.list;

import com.arcturus.api.LoggerFactory;
import com.arcturus.api.service.entity.EntityUseCaseHandler;
import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.cluster.Cluster;
import com.arcturus.appserver.cluster.hazelcast.SharedHazelcastInstance;
import com.arcturus.appserver.concurrent.nanoprocess.NanoProcessSystem;
import com.arcturus.appserver.config.Config;
import com.arcturus.appserver.database.keyvaluestore.StringKeyValueStore;
import com.arcturus.appserver.json.JsonFactory;
import com.arcturus.appserver.system.*;
import com.arcturus.appserver.system.app.inject.AppId;
import com.arcturus.appserver.system.app.service.ServiceClusterConfigProvider;
import com.arcturus.appserver.system.app.service.UseCaseProcessor;
import com.arcturus.appserver.system.app.service.entity.*;
import com.arcturus.appserver.system.app.service.entity.aggregation.entityservice.AggregationEntityServiceProvider;
import com.arcturus.appserver.system.app.service.entity.aggregation.usecase.CollectEntityIdsForAggregation;
import com.arcturus.appserver.system.app.service.entity.list.usecase.AddElement;
import com.arcturus.appserver.system.app.service.entity.list.usecase.CollectElements;
import com.arcturus.appserver.system.app.service.entity.list.usecase.RemoveElement;
import com.arcturus.appserver.system.app.service.entity.list.usecase.TransferList;
import com.arcturus.appserver.system.app.service.info.EntityServiceInfo;
import com.arcturus.appserver.system.app.service.info.EntityUseCaseInfo;
import com.arcturus.appserver.system.app.type.java.JavaEntityInitializer;
import com.arcturus.appserver.system.app.type.java.JavaEntitySerializer;

import java.lang.reflect.Type;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

public class ListChunkEntityServiceFactory
{
	private static final int ENTITY_VERSION = 1;

	public static class ListChunkEntityServiceResult<T>
	{
		public final ArcturusEntityService entityService;
		public final ArcturusListChunkService<T> listService;

		ListChunkEntityServiceResult(
			ArcturusEntityService entityService, ArcturusListChunkService<T> listService
		)
		{
			this.entityService = entityService;
			this.listService = listService;
		}
	}

	private final String appId;
	private final LoggerFactory loggerFactory;
	private final NanoProcessSystem<Message> nanoProcessSystem;
	private final JsonFactory jsonFactory;
	private final StringKeyValueStore db;
	private final SharedHazelcastInstance sharedHazelcastInstance;
	private final Cluster cluster;
	private final Config config;
	private final SourceNodeIdProvider sourceNodeIdProvider;
	private final JsonStringSerializer jsonStringSerializer;
	private final ArcturusResponseSender responseSender;
	private final ArcturusUserSender userSender;
	private final UseCaseProcessor useCaseProcessor;
	private final AggregationEntityServiceProvider aggregationEntityServiceProvider;

	public ListChunkEntityServiceFactory(
		@AppId
			String appId,
		LoggerFactory loggerFactory,
		NanoProcessSystem<Message> nanoProcessSystem,
		JsonFactory jsonFactory,
		StringKeyValueStore db,
		SharedHazelcastInstance sharedHazelcastInstance,
		Cluster cluster,
		Config config,
		SourceNodeIdProvider sourceNodeIdProvider,
		JsonStringSerializer jsonStringSerializer,
		ArcturusResponseSender responseSender,
		ArcturusUserSender userSender,
		UseCaseProcessor useCaseProcessor,
		AggregationEntityServiceProvider aggregationEntityServiceProvider
	)
	{
		this.appId = appId;
		this.loggerFactory = loggerFactory;
		this.nanoProcessSystem = nanoProcessSystem;
		this.jsonFactory = jsonFactory;
		this.db = db;
		this.sharedHazelcastInstance = sharedHazelcastInstance;
		this.cluster = cluster;
		this.config = config;
		this.sourceNodeIdProvider = sourceNodeIdProvider;
		this.jsonStringSerializer = jsonStringSerializer;
		this.responseSender = responseSender;
		this.userSender = userSender;
		this.useCaseProcessor = useCaseProcessor;
		this.aggregationEntityServiceProvider = aggregationEntityServiceProvider;
	}

	public <T> ListChunkEntityServiceResult<T> create(
		String name,
		Type listChunkEntityType,
		Type collectElementsMessageType,
		ListElementTypeSerializer<T> listElementTypeSerializer,
		Iterable<ListChunkUseCase<T>> additionalUseCases
	) throws UnknownHostException
	{
		var idToUseCaseMap = new HashMap<String, EntityUseCaseHandler<ListChunk<T>>>();
		var idToUseCaseInfoMap = new HashMap<String, EntityUseCaseInfo>();

		var localEntityService = new LocalEntityService<>(appId,
			name,
			ENTITY_VERSION,
			loggerFactory,
			nanoProcessSystem,
			new EntityUseCaseProvider<>(idToUseCaseMap, null),
			null,
			uuid -> new ListChunk<>(new ArrayList<>()),
			new JavaEntitySerializer<>(jsonStringSerializer, listChunkEntityType),
			new JavaEntityInitializer<>(),
			new EntityMigrator(loggerFactory, ENTITY_VERSION, null, jsonFactory),
			db,
			null,
			new EntityServiceInfo(name, idToUseCaseInfoMap, null),
			responseSender,
			userSender,
			null
		);

		var nodeIdentityList = new NodeIdentityList(
			ServiceClusterConfigProvider.DEFAULT_CONFIG.isIncludingNodes(),
			ServiceClusterConfigProvider.DEFAULT_CONFIG.getNodes()
		);

		var entityService = new ArcturusEntityService(appId,
			name,
			loggerFactory,
			new EntityIdToNodeBalancer(loggerFactory,
				appId,
				name,
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

		var listService = new ArcturusListChunkService<>(entityService,
			jsonStringSerializer,
			listElementTypeSerializer,
			jsonFactory
		);

		idToUseCaseMap.put(ListUseCase.add.name(),
			new AddElement<>(listService, listElementTypeSerializer)
		);
		idToUseCaseInfoMap.put(ListUseCase.add.name(),
			new EntityUseCaseInfo(ListUseCase.add.name(), true, false, null, null, null)
		);

		idToUseCaseMap.put(ListUseCase.rem.name(),
			new RemoveElement<>(listService, listElementTypeSerializer)
		);
		idToUseCaseInfoMap.put(ListUseCase.rem.name(),
			new EntityUseCaseInfo(ListUseCase.rem.name(), true, false, null, null, null)
		);

		idToUseCaseMap.put(ListUseCase.tli.name(),
			new TransferList<>(jsonFactory, listElementTypeSerializer)
		);
		idToUseCaseInfoMap.put(ListUseCase.tli.name(),
			new EntityUseCaseInfo(ListUseCase.tli.name(), true, false, null, null, null)
		);

		idToUseCaseMap.put(ListUseCase.col.name(), new CollectElements<>(loggerFactory,
			jsonStringSerializer,
			entityService,
			useCaseProcessor,
			collectElementsMessageType
		));
		idToUseCaseInfoMap.put(ListUseCase.col.name(),
			new EntityUseCaseInfo(ListUseCase.col.name(), false, false, null, null, null)
		);

		idToUseCaseMap.put(ListUseCase.cola.name(),
			new CollectEntityIdsForAggregation(jsonStringSerializer,
				entityService,
				aggregationEntityServiceProvider.get()
			)
		);
		idToUseCaseInfoMap.put(ListUseCase.cola.name(),
			new EntityUseCaseInfo(ListUseCase.cola.name(), false, false, null, null, null)
		);

		if (additionalUseCases != null)
		{
			for (var useCase : additionalUseCases)
			{
				idToUseCaseMap.put(useCase.useCaseId, useCase.useCaseHandler);
				idToUseCaseInfoMap.put(useCase.useCaseId,
					new EntityUseCaseInfo(useCase.useCaseId, false, false, null, null, null)
				);
			}
		}

		return new ListChunkEntityServiceResult<>(entityService, listService);
	}
}