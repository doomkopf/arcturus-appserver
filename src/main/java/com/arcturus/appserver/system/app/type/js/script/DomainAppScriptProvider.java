package com.arcturus.appserver.system.app.type.js.script;

import com.arcturus.api.ArcturusAppException;
import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;
import com.arcturus.appserver.concurrent.context.ContextExecutableThread;

import java.util.HashMap;
import java.util.Map;

public class DomainAppScriptProvider
{
	private final LoggerFactory loggerFactory;
	private final ScriptEnhancer scriptEnhancer;
	private final String appScriptCode;
	private final Logger log;
	private final JavaObjectsRegistrator javaObjectsRegistrator;

	private final ThreadLocal<DomainAppScript> threadLocal = new ThreadLocal<>();
	private final Map<ContextExecutableThread, DomainAppScript> appScripts = new HashMap<>();
	private boolean isShutdown = false;

	public DomainAppScriptProvider(
		LoggerFactory loggerFactory,
		ScriptEnhancer scriptEnhancer,
		String appScriptCode,
		JavaObjectsRegistrator javaObjectsRegistrator
	)
	{
		this.loggerFactory = loggerFactory;
		this.scriptEnhancer = scriptEnhancer;
		this.appScriptCode = appScriptCode;
		this.javaObjectsRegistrator = javaObjectsRegistrator;
		log = loggerFactory.create(getClass());
	}

	public DomainAppScript getAppScript() throws ArcturusAppException
	{
		var domainAppScript = threadLocal.get();
		if (domainAppScript == null)
		{
			var currentJavaThread = Thread.currentThread();
			if (!(currentJavaThread instanceof ContextExecutableThread))
			{
				throw new ArcturusAppException("Attempt to access "
					+ DomainAppScript.class.getSimpleName()
					+ " from a non-"
					+ ContextExecutableThread.class.getSimpleName());
			}

			var currentThread = (ContextExecutableThread) currentJavaThread;

			synchronized (this)
			{
				if (isShutdown)
				{
					log.log(
						LogLevel.info,
						"Tried to create new " + AppScript.class.getSimpleName() + " after shutdown"
					);
					return null;
				}

				AppScript appScript = null;
				DomainAppScript newDomainAppScript = null;
				try
				{
					appScript = new AppScript(loggerFactory, scriptEnhancer, appScriptCode);
					newDomainAppScript = new DomainAppScript(appScript, threadLocal);
					javaObjectsRegistrator.registerJavaObjects(appScript);
					newDomainAppScript.executeProcessGlobals();
				}
				catch (Throwable e)
				{
					if (log.isLogLevel(LogLevel.debug))
					{
						log.log(LogLevel.debug, e);
					}

					if (appScript != null)
					{
						appScript.close();
					}

					if (newDomainAppScript != null)
					{
						newDomainAppScript.close();
					}

					throw new ArcturusAppException(e);
				}

				threadLocal.set(newDomainAppScript);
				appScripts.put(currentThread, newDomainAppScript);

				return newDomainAppScript;
			}
		}

		return domainAppScript;
	}

	public synchronized void shutdown()
	{
		isShutdown = true;
		var threadAppScripts = appScripts.entrySet();
		for (var entry : threadAppScripts)
		{
			try
			{
				entry.getKey().executeSync(() -> entry.getValue().close(), 1000);
			}
			catch (Throwable e)
			{
				log.log(LogLevel.error, e);
			}
		}

		if (log.isLogLevel(LogLevel.debug))
		{
			log.log(LogLevel.debug, "Shut down " + threadAppScripts.size() + " app scripts");
		}
	}
}