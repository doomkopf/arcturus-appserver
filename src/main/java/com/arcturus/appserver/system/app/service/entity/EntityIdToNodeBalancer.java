package com.arcturus.appserver.system.app.service.entity;

import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;
import com.arcturus.appserver.cluster.Cluster;
import com.arcturus.appserver.cluster.Node;
import com.arcturus.appserver.cluster.hazelcast.SharedHazelcastInstance;
import com.arcturus.appserver.config.Config;
import com.arcturus.appserver.config.ServerConfigPropery;
import com.arcturus.appserver.system.NodeIdentityList;
import com.arcturus.appserver.system.message.EntityManagementMessage;
import com.arcturus.appserver.system.message.ServiceMessage;
import com.arcturus.appserver.system.message.management.EntityManagementMessageBehavior;
import com.google.common.annotations.VisibleForTesting;
import com.hazelcast.concurrent.atomiclong.AtomicLongService;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizeConfig;
import com.hazelcast.config.MaxSizeConfig.MaxSizePolicy;
import com.hazelcast.core.*;
import com.hazelcast.map.listener.EntryEvictedListener;

import java.util.Comparator;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

/**
 * A stateful and distributed load balancer for mapping an entity id to a node
 * inside our cluster.
 *
 * @author doomkopf
 */
public class EntityIdToNodeBalancer
{
	private static final String ENTITY_TO_NODE_MAP_NAME = "entityIdToNodeId";
	private static final String ATOMIC_LONG_PREFIX = "al";

	private class LocalEntryListener implements EntryEvictedListener<UUID, UUID>
	{
		LocalEntryListener()
		{
		}

		@Override
		public void entryEvicted(EntryEvent<UUID, UUID> event)
		{
			onLocalEntryEvicted(event);
		}
	}

	private final Logger log;
	private final String appId;
	private final String serviceName;
	private final Cluster cluster;
	private final HazelcastInstance hz;
	private final IMap<UUID, UUID> entityIdToNodeIdMap;
	private final LocalEntityService<?> localEntityService;
	private final NodeIdentityList nodeIdentityList;
	private final UUID localNodeId;

	public EntityIdToNodeBalancer( // NOSONAR
		LoggerFactory loggerFactory,
		String appId,
		String serviceName,
		SharedHazelcastInstance sharedHazelcastInstance,
		Config config,
		Cluster cluster,
		LocalEntityService<?> localEntityService,
		NodeIdentityList nodeIdentityList
	)
	{
		log = loggerFactory.create(getClass());
		this.appId = appId;
		this.serviceName = serviceName;
		this.cluster = cluster;
		this.localEntityService = localEntityService;
		this.nodeIdentityList = nodeIdentityList;
		localNodeId = cluster.getLocalNode().getId();

		hz = sharedHazelcastInstance.getHazelcastInstance();

		var mapName = appId + serviceName + ENTITY_TO_NODE_MAP_NAME;

		var mapConfig = new MapConfig(mapName);
		mapConfig.setEvictionPolicy(EvictionPolicy.LRU);
		mapConfig.setMaxIdleSeconds(config.getInt(ServerConfigPropery.entityInMemoryEvictionSeconds));
		var maxSizeConfig = new MaxSizeConfig();
		maxSizeConfig.setMaxSizePolicy(MaxSizePolicy.USED_HEAP_PERCENTAGE);
		maxSizeConfig.setSize(config.getInt(ServerConfigPropery.hazelcastMaxUsedHeapSpacePercentage));
		mapConfig.setMaxSizeConfig(maxSizeConfig);
		mapConfig.setBackupCount(0);
		mapConfig.setAsyncBackupCount(1);

		hz.getConfig().addMapConfig(mapConfig);

		entityIdToNodeIdMap = hz.getMap(mapName);
		entityIdToNodeIdMap.addLocalEntryListener(new LocalEntryListener());
	}

	@VisibleForTesting
	EntityIdToNodeBalancer(Cluster cluster, NodeIdentityList nodeIdentityList)
	{
		this.cluster = cluster;
		this.nodeIdentityList = nodeIdentityList;
		log = null;
		appId = null;
		serviceName = null;
		hz = null;
		entityIdToNodeIdMap = null;
		localEntityService = null;
		localNodeId = null;
	}

	private void onLocalEntryEvicted(EntryEvent<UUID, UUID> event)
	{
		if (log.isLogLevel(LogLevel.debug))
		{
			log.log(LogLevel.debug, "Local entry evicted " + event);
		}

		var nodeId = event.getOldValue();

		var node = cluster.getNodeById(nodeId);
		if (node == null)
		{
			return;
		}

		getNodeLoad(nodeId).decrementAndGetAsync();

		var msg = new EntityManagementMessage(EntityManagementMessageBehavior.kill, event.getKey());
		if (node.isLocal())
		{
			localEntityService.send(msg);
		}
		else
		{
			new ServiceMessage(appId, serviceName, null, msg).sendToNode(node);
		}
	}

	void determineNodeForEntityId(UUID entityId, Consumer<Node> resultConsumer)
	{
		entityIdToNodeIdMap.getAsync(entityId).andThen(new ExecutionCallback<>()
		{
			@Override
			public void onResponse(UUID returnedNodeId)
			{
				if (returnedNodeId == null)
				{
					var node = determineNode(entityId);
					resultConsumer.accept(node);
					return;
				}

				var node = cluster.getNodeById(returnedNodeId);
				if (node == null)
				{
					entityIdToNodeIdMap.remove(entityId, returnedNodeId);
					node = determineNode(entityId);
				}

				resultConsumer.accept(node);
			}

			@Override
			public void onFailure(Throwable t)
			{
				log.log(LogLevel.error, t);
			}
		});
	}

	UUID filterAndFindMinLoadNode()
	{
		return StreamSupport.stream(cluster.getAllNodesIterable().spliterator(), false)
			.filter(id -> nodeIdentityList.isIncluded(cluster.getNodeById(id)))
			.min(Comparator.comparingLong(id2 -> getNodeLoad(id2).get()))
			.get();
	}

	private Node determineNode(UUID entityId)
	{
		var nodeId = filterAndFindMinLoadNode();

		if (log.isLogLevel(LogLevel.debug))
		{
			log.log(
				LogLevel.debug,
				"Determined node as the one with the lowest load: "
					+ nodeId
					+ " load: "
					+ getNodeLoad(nodeId).get()
			);
		}

		var node = cluster.getNodeById(nodeId);
		if (node == null)
		{
			log.log(LogLevel.info, "No node could be determined - using local node");
			node = cluster.getLocalNode();
			nodeId = node.getId();
		}

		var alreadyInMapNodeId = entityIdToNodeIdMap.putIfAbsent(entityId, nodeId);
		if (alreadyInMapNodeId == null)
		{
			getNodeLoad(nodeId).incrementAndGetAsync();
		}
		else
		{
			node = cluster.getNodeById(alreadyInMapNodeId);
			if (log.isLogLevel(LogLevel.debug))
			{
				log.log(
					LogLevel.debug,
					"EntityId -> nodeId determination collision. Using existing mapping."
				);
			}
		}

		return node;
	}

	private IAtomicLong getNodeLoad(UUID nodeId)
	{
		return hz.getDistributedObject(AtomicLongService.SERVICE_NAME, ATOMIC_LONG_PREFIX + nodeId);
	}

	public void shutdown()
	{
		try
		{
			getNodeLoad(localNodeId).addAndGetAsync(-entityIdToNodeIdMap.localKeySet().size());
			entityIdToNodeIdMap.destroy();
		}
		catch (Throwable e)
		{
			log.log(LogLevel.error, e);
		}
	}
}
