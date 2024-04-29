package com.arcturus.appserver.system;

import com.arcturus.api.ArcturusAppException;
import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;
import com.arcturus.appserver.cluster.Cluster;
import com.arcturus.appserver.system.app.App;
import com.arcturus.appserver.system.app.AppContainer;
import com.arcturus.appserver.system.message.AppManagementMessage;
import com.arcturus.appserver.system.message.management.AppManagementMessageBehavior;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds and manages all {@link App}s.
 *
 * @author doomkopf
 */
public class AppManager
{
	private final Logger log;
	private final AppLoader appLoader;
	private final Cluster cluster;

	private final UUID localNodeId;
	private final Map<String, AppMaintenanceContext> appToMaintenanceContextMap = new ConcurrentHashMap<>();
	private final Map<String, AppContainer> apps = new ConcurrentHashMap<>();

	public AppManager(LoggerFactory loggerFactory, AppLoader appLoader, Cluster cluster)
	{
		log = loggerFactory.create(getClass());
		this.appLoader = appLoader;
		this.cluster = cluster;

		localNodeId = cluster.getLocalNode().getId();
	}

	App getAppIgnoringMaintenance(String id)
	{
		var app = apps.get(id);
		if (app == null)
		{
			app = loadApp(id);
			if (app == null)
			{
				return null;
			}
		}

		return app.getApp();
	}

	public App getApp(String id)
	{
		if (isAppUnderMaintenance(id))
		{
			return null;
		}

		return getAppIgnoringMaintenance(id);
	}

	private AppMaintenanceContext enableMaintenance(String id)
	{
		return appToMaintenanceContextMap.computeIfAbsent(id, key -> new AppMaintenanceContext());
	}

	public void disableMaintenance(String id, boolean broadCastToOtherNodes)
	{
		appToMaintenanceContextMap.remove(id);

		if (broadCastToOtherNodes)
		{
			for (var nodeId : cluster.getAllNodesIterable())
			{
				var node = cluster.getNodeById(nodeId);
				if (node.isLocal())
				{
					continue;
				}

				new AppManagementMessage(AppManagementMessageBehavior.disableMaintenance,
					id,
					null
				).sendToNode(node);
			}
		}
	}

	public boolean isAppUnderMaintenance(String id)
	{
		return appToMaintenanceContextMap.containsKey(id);
	}

	public void shutdownApp(String id, UUID sourceNodeId) throws InterruptedException
	{
		var maintenanceContext = enableMaintenance(id);

		if (sourceNodeId == null)
		{
			broadcastAppShutdownToCluster(id, maintenanceContext);
		}

		var appContainer = apps.remove(id);
		if (appContainer == null)
		{
			if (log.isLogLevel(LogLevel.debug))
			{
				log.log(LogLevel.debug, "App " + id + " not loaded here - no shutdown necessary");
			}
		}
		else
		{
			if (log.isLogLevel(LogLevel.debug))
			{
				log.log(LogLevel.debug, "Shutting down app " + id + "...");
			}

			appContainer.shutdown();

			if (log.isLogLevel(LogLevel.debug))
			{
				log.log(LogLevel.debug, "Done shutting down app " + id);
			}
		}

		if (sourceNodeId == null)
		{
			if (!maintenanceContext.waitForCallbacks() && log.isLogLevel(LogLevel.debug))
			{
				log.log(LogLevel.info, "Waiting for node callbacks timed out for appId " + id);
			}
		}
		else
		{
			var sourceNode = cluster.getNodeById(sourceNodeId);
			if (sourceNode == null)
			{
				log.log(LogLevel.info, "Source node was not found: " + sourceNodeId);
				return;
			}

			new AppManagementMessage(AppManagementMessageBehavior.shutdownConfirmationCallback,
				id,
				null
			).sendToNode(sourceNode);
		}
	}

	private void broadcastAppShutdownToCluster(String id, AppMaintenanceContext maintenanceContext)
	{
		maintenanceContext.initExpectedNodesToCallBack(cluster.getNodeCount() - 1);

		for (var nodeId : cluster.getAllNodesIterable())
		{
			var node = cluster.getNodeById(nodeId);
			if (node.isLocal())
			{
				continue;
			}

			new AppManagementMessage(AppManagementMessageBehavior.shutdown,
				id,
				localNodeId
			).sendToNode(node);
		}
	}

	public void shutdownAppConfirmationCallback(String appId)
	{
		var maintenanceContext = appToMaintenanceContextMap.get(appId);
		if (maintenanceContext == null)
		{
			log.log(LogLevel.warn,
				"No "
					+ AppMaintenanceContext.class.getSimpleName()
					+ " was found for appId "
					+ appId
					+ " - the shutdown on the calling back node probably took too long"
			);
			return;
		}

		maintenanceContext.callback();
	}

	private AppContainer loadApp(String id)
	{
		var app = apps.get(id);
		if (app != null)
		{
			return app;
		}

		return apps.computeIfAbsent(id, i ->
		{
			try
			{
				return appLoader.loadApp(i);
			}
			catch (InterruptedException | ArcturusAppException e)
			{
				log.log(LogLevel.error, e);
				return null;
			}
		});
	}

	public void shutdown()
	{
		for (var app : apps.values())
		{
			try
			{
				app.shutdown();
			}
			catch (Throwable e)
			{
				log.log(LogLevel.error, e);
			}
		}
	}
}