package com.arcturus.appserver.system.app.type.js.script;

import com.arcturus.api.ArcturusAppException;
import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;
import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AppScript implements AutoCloseable
{
	private final Logger log;
	private final V8 runtime;
	private final Map<String, AppScriptObject> appScriptObjects = new HashMap<>();

	AppScript(LoggerFactory loggerFactory, ScriptEnhancer scriptEnhancer, String appScriptCode)
	{
		log = loggerFactory.create(getClass());
		var script = scriptEnhancer.enhance(appScriptCode);

		runtime = V8.createV8Runtime();
		runtime.executeVoidScript(script);
	}

	AppScriptObject registerObject(String key, Object value) throws ArcturusAppException
	{
		if (appScriptObjects.containsKey(key))
		{
			throw new ArcturusAppException("Key " + key + " already registered");
		}

		var v8Object = new V8Object(runtime);
		runtime.add(key, v8Object);

		var appScriptObject = new AppScriptObject(this, key, v8Object, value);
		appScriptObjects.put(key, appScriptObject);

		return appScriptObject;
	}

	void removeObject(String key)
	{
		appScriptObjects.remove(key);
	}

	Object invokeFunction(String name, Object... args)
	{
		if (runtime.isReleased())
		{
			if (log.isLogLevel(LogLevel.debug))
			{
				log.log(
					LogLevel.debug,
					"Attempt to call function " + name + " while script has already been released"
				);
			}
			return null;
		}

		return runtime.executeJSFunction(name, args);
	}

	@Override
	public void close()
	{
		var objects = new ArrayList<>(appScriptObjects.values());
		for (var appScriptObject : objects)
		{
			appScriptObject.close();
		}

		runtime.release();
	}
}