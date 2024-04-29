package com.arcturus.appserver.system.app.service.entity;

import com.arcturus.api.ArcturusAppException;
import com.arcturus.api.LoggerFactory;
import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.cluster.Cluster;
import com.arcturus.appserver.cluster.hazelcast.SharedHazelcastInstance;
import com.arcturus.appserver.config.Config;
import com.arcturus.appserver.system.NodeIdentityList;
import com.arcturus.appserver.system.SourceNodeIdProvider;
import com.arcturus.appserver.system.app.inject.AppId;
import com.arcturus.appserver.system.app.service.ServiceClusterConfigProvider;
import com.arcturus.appserver.system.app.service.UserEntityServiceProvider;

import java.net.UnknownHostException;

/**
 * Creates {@link ArcturusEntityService}s.
 *
 * @author doomkopf
 */
public class EntityServiceFactory<T>
{
	private final String appId;
	private final LoggerFactory loggerFactory;
	private final SharedHazelcastInstance sharedHazelcastInstance;
	private final Config config;
	private final Cluster cluster;
	private final LocalEntityServiceFactory<T> localEntityServiceFactory;
	private final ServiceClusterConfigProvider serviceClusterConfigProvider;
	private final SourceNodeIdProvider sourceNodeIdProvider;
	private final JsonStringSerializer jsonStringSerializer;

	public EntityServiceFactory( // NOSONAR
		@AppId
			String appId,
		LoggerFactory loggerFactory,
		SharedHazelcastInstance sharedHazelcastInstance,
		Config config,
		Cluster cluster,
		LocalEntityServiceFactory<T> localEntityServiceFactory,
		ServiceClusterConfigProvider serviceClusterConfigProvider,
		SourceNodeIdProvider sourceNodeIdProvider,
		JsonStringSerializer jsonStringSerializer
	)
	{
		this.appId = appId;
		this.loggerFactory = loggerFactory;
		this.sharedHazelcastInstance = sharedHazelcastInstance;
		this.config = config;
		this.cluster = cluster;
		this.localEntityServiceFactory = localEntityServiceFactory;
		this.serviceClusterConfigProvider = serviceClusterConfigProvider;
		this.sourceNodeIdProvider = sourceNodeIdProvider;
		this.jsonStringSerializer = jsonStringSerializer;
	}

	public ArcturusEntityService create(String name, UserEntityServiceProvider serviceProvider)
		throws UnknownHostException, ArcturusAppException
	{
		var serviceClusterConfig = serviceClusterConfigProvider.getByName(name);

		var nodeIdentityList = new NodeIdentityList(serviceClusterConfig.isIncludingNodes(),
			serviceClusterConfig.getNodes()
		);

		LocalEntityService<T> localEntityService = null;
		if (nodeIdentityList.isIncluded(cluster.getLocalNode()))
		{
			localEntityService = localEntityServiceFactory.create(name);
		}

		return new ArcturusEntityService(appId, name, loggerFactory, new EntityIdToNodeBalancer(
			loggerFactory,
			appId,
			name,
			sharedHazelcastInstance,
			config,
			cluster,
			localEntityService,
			nodeIdentityList
		), localEntityService, sourceNodeIdProvider, jsonStringSerializer);
	}
}