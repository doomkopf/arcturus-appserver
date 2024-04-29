package com.arcturus.appserver.system;

import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;
import com.arcturus.appserver.cluster.hazelcast.SharedHazelcastInstance;
import com.arcturus.appserver.config.Config;
import com.arcturus.appserver.config.ServerConfigPropery;
import com.arcturus.appserver.net.PersistentLocalSession;
import com.arcturus.appserver.net.PersistentLocalSessionInfo;
import com.arcturus.appserver.net.RequestContext;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizeConfig;
import com.hazelcast.config.MaxSizeConfig.MaxSizePolicy;
import com.hazelcast.core.ExecutionCallback;
import com.hazelcast.core.IMap;
import com.hazelcast.map.listener.EntryEvictedListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Container for mapping various combinations from/to userId and sessionId as
 * well containing the mapping from userId to its
 * {@link PersistentLocalSession}.
 *
 * @author doomkopf
 */
public class UserSessionContainer
{
	private static final String SESSION_ID_TO_USER_ID_MAP = "sessionIdToUserIdMap";
	private static final String USER_ID_TO_SESSION_ID_MAP = "userIdToSessionIdMap";

	private final Logger log;
	private final IMap<Long, UUID> sessionIdToUserIdMap;
	private final Cache<Long, UUID> localSessionIdToUserIdCache;

	private final Cache<UUID, RequestContext> userIdToRequestContextMap;
	private final Cache<UUID, List<PersistentLocalSession>> userIdToPersistentLocalSessionMap;

	public UserSessionContainer(
		LoggerFactory loggerFactory, SharedHazelcastInstance sharedHazelcastInstance, Config config
	)
	{
		log = loggerFactory.create(UserSessionContainer.class);

		var hz = sharedHazelcastInstance.getHazelcastInstance();

		var mapConfig = new MapConfig(SESSION_ID_TO_USER_ID_MAP);
		mapConfig.setEvictionPolicy(EvictionPolicy.LRU);
		mapConfig.setMaxIdleSeconds(config.getInt(ServerConfigPropery.userSessionCachesEvictionSeconds));
		var maxSizeConfig = new MaxSizeConfig();
		maxSizeConfig.setMaxSizePolicy(MaxSizePolicy.USED_HEAP_PERCENTAGE);
		maxSizeConfig.setSize(config.getInt(ServerConfigPropery.hazelcastMaxUsedHeapSpacePercentage));
		mapConfig.setMaxSizeConfig(maxSizeConfig);
		mapConfig.setBackupCount(0);
		mapConfig.setAsyncBackupCount(1);

		hz.getConfig().addMapConfig(mapConfig);

		sessionIdToUserIdMap = hz.getMap(SESSION_ID_TO_USER_ID_MAP);

		localSessionIdToUserIdCache = CacheBuilder.newBuilder()
			.expireAfterWrite(config.getInt(ServerConfigPropery.userSessionCachesEvictionSeconds)
					/ 2,
				TimeUnit.SECONDS
			)
			.build();

		sessionIdToUserIdMap.addEntryListener((EntryEvictedListener<UUID, UUID>) event -> localSessionIdToUserIdCache
			.invalidate(event.getKey()), false);

		mapConfig = new MapConfig(USER_ID_TO_SESSION_ID_MAP);
		mapConfig.setEvictionPolicy(EvictionPolicy.LRU);
		mapConfig.setMaxIdleSeconds(config.getInt(ServerConfigPropery.userSessionCachesEvictionSeconds));
		maxSizeConfig = new MaxSizeConfig();
		maxSizeConfig.setMaxSizePolicy(MaxSizePolicy.USED_HEAP_PERCENTAGE);
		maxSizeConfig.setSize(config.getInt(ServerConfigPropery.hazelcastMaxUsedHeapSpacePercentage));
		mapConfig.setMaxSizeConfig(maxSizeConfig);
		mapConfig.setBackupCount(0);
		mapConfig.setAsyncBackupCount(1);

		hz.getConfig().addMapConfig(mapConfig);

		userIdToRequestContextMap = CacheBuilder.newBuilder()
			.expireAfterWrite(60, TimeUnit.SECONDS) // TODO config
			.build();

		userIdToPersistentLocalSessionMap = CacheBuilder.newBuilder()
			.expireAfterWrite(config.getInt(ServerConfigPropery.userSessionCachesEvictionSeconds)
					/ 2,
				TimeUnit.SECONDS
			)
			.build();
	}

	public void put(Long sessionId, UUID userId, Runnable completionHandler)
	{
		sessionIdToUserIdMap.putAsync(sessionId, userId).andThen(new ExecutionCallback<>()
		{
			@Override
			public void onResponse(UUID responseUserId)
			{
				completionHandler.run();
			}

			@Override
			public void onFailure(Throwable t)
			{
				log.log(LogLevel.error, t);
				completionHandler.run();
			}
		});
	}

	public void getUserIdBySessionId(Long sessionId, Consumer<UUID> consumer)
	{
		if (sessionId == null)
		{
			consumer.accept(null);
			return;
		}

		var userId = localSessionIdToUserIdCache.getIfPresent(sessionId);
		if (userId != null)
		{
			consumer.accept(userId);
			return;
		}

		sessionIdToUserIdMap.getAsync(sessionId).andThen(new ExecutionCallback<>()
		{
			@Override
			public void onResponse(UUID response)
			{
				if (response == null)
				{
					consumer.accept(null);
					return;
				}

				localSessionIdToUserIdCache.put(sessionId, response);
				consumer.accept(response);
			}

			@Override
			public void onFailure(Throwable t)
			{
				log.log(LogLevel.error, t);
				consumer.accept(null);
			}
		});
	}

	private static List<PersistentLocalSession> createSingleElemList()
	{
		return Collections.synchronizedList(new ArrayList<>(1));
	}

	public void putPersistentLocalSession(
		UUID userId, PersistentLocalSession persistentLocalSession
	)
	{
		try
		{
			var sessions = userIdToPersistentLocalSessionMap.get(userId,
				UserSessionContainer::createSingleElemList
			);
			sessions.add(persistentLocalSession);
		}
		catch (ExecutionException e)
		{
			log.log(LogLevel.error, e);
		}
	}

	void removePersistentLocalSessionByUserId(UUID userId)
	{
		userIdToPersistentLocalSessionMap.invalidate(userId);
	}

	boolean sendToPersistentLocalSessionsByUserId(UUID userId, String payload)
	{
		var persistentLocalSessions = userIdToPersistentLocalSessionMap.getIfPresent(userId);
		if (persistentLocalSessions != null)
		{
			synchronized (persistentLocalSessions)
			{
				var size = persistentLocalSessions.size();
				if (size == 1)
				{
					var session = persistentLocalSessions.get(0);
					if (session.isOpen())
					{
						session.send(payload);
						return true;
					}
				}
				else if (size > 1)
				{
					for (var session : persistentLocalSessions)
					{
						session.send(payload);
					}
					return true;
				}
			}
		}

		return false;
	}

	void putRequestContext(UUID userId, RequestContext requestContext)
	{
		userIdToRequestContextMap.put(userId, requestContext);
	}

	RequestContext getRequestContextByUserId(UUID userId)
	{
		return userIdToRequestContextMap.getIfPresent(userId);
	}

	public void connectPersistenLocalSession(
		PersistentLocalSession persistentLocalSession, UUID userId, String appId
	)
	{
		persistentLocalSession.setInfo(new PersistentLocalSessionInfo(userId, appId));
		putPersistentLocalSession(userId, persistentLocalSession);
	}
}