package com.arcturus.appserver.system;

import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;
import com.arcturus.appserver.cluster.hazelcast.HazelcastCluster;
import com.arcturus.appserver.cluster.hazelcast.SharedHazelcastInstance;
import com.arcturus.appserver.concurrent.ArcturusContextExecutor;
import com.arcturus.appserver.concurrent.nanoprocess.NanoProcessSystem;
import com.arcturus.appserver.database.keyvaluestore.StringKeyValueStore;
import com.arcturus.appserver.net.HttpSessionService;
import com.arcturus.appserver.net.PersistentLocalSessionService;

/**
 * Handling the manual startup and orderly shutdown of arcturus main components.
 *
 * @author doomkopf
 */
public class SystemHandler
{
	@FunctionalInterface
	interface Shutdownable
	{
		void shutdown() throws InterruptedException;
	}

	private final Logger log;
	private final StringKeyValueStore db;
	private final SharedHazelcastInstance sharedHazelcastInstance;
	private final PersistentLocalSessionService persistentLocalSessionService;
	private final HttpSessionService httpSessionService;
	private final NanoProcessSystem<Message> nanoProcessSystem;
	private final AppManager appManager;
	private final HazelcastCluster hazelcastCluster;
	private final ArcturusContextExecutor contextExecutor;

	public SystemHandler(
		LoggerFactory loggerFactory,
		StringKeyValueStore db,
		SharedHazelcastInstance sharedHazelcastInstance,
		PersistentLocalSessionService persistentLocalSessionService,
		HttpSessionService httpSessionService,
		NanoProcessSystem<Message> nanoProcessSystem,
		AppManager appManager,
		HazelcastCluster hazelcastCluster,
		ArcturusContextExecutor contextExecutor
	)
	{
		this.db = db;
		this.sharedHazelcastInstance = sharedHazelcastInstance;
		this.persistentLocalSessionService = persistentLocalSessionService;
		this.httpSessionService = httpSessionService;
		this.nanoProcessSystem = nanoProcessSystem;
		this.appManager = appManager;
		this.hazelcastCluster = hazelcastCluster;
		this.contextExecutor = contextExecutor;

		nanoProcessSystem.start();

		log = loggerFactory.create(getClass());

		log.log(LogLevel.info, "--------------------------------"); // NOSONAR
		log.log(LogLevel.info, "|     Arcturus node running    |");
		log.log(LogLevel.info, "--------------------------------");
	}

	private void tryCatchedShutdown(Shutdownable shutdownable)
	{
		try
		{
			shutdownable.shutdown();
		}
		catch (Throwable e)
		{
			log.log(LogLevel.error, e);
		}
	}

	public void shutdown()
	{
		tryCatchedShutdown(persistentLocalSessionService::shutdown);
		tryCatchedShutdown(httpSessionService::shutdown);
		tryCatchedShutdown(appManager::shutdown);
		tryCatchedShutdown(nanoProcessSystem::shutdown);
		tryCatchedShutdown(contextExecutor::shutdown);
		tryCatchedShutdown(hazelcastCluster::shutdown);
		tryCatchedShutdown(sharedHazelcastInstance::shutdown);
		tryCatchedShutdown(db::shutdown);

		log.log(LogLevel.info, "--------------------------------");
		log.log(LogLevel.info, "|  Arcturus node has shut down  |");
		log.log(LogLevel.info, "--------------------------------");
	}
}