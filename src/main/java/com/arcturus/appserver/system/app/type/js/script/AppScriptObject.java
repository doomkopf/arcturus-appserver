package com.arcturus.appserver.system.app.type.js.script;

import com.eclipsesource.v8.V8Object;

public class AppScriptObject implements AutoCloseable
{
	private final AppScript appScript;
	private final String key;
	private final V8Object v8Object;
	private final Object obj;

	AppScriptObject(AppScript appScript, String key, V8Object v8Object, Object obj)
	{
		this.appScript = appScript;
		this.key = key;
		this.v8Object = v8Object;
		this.obj = obj;
	}

	void registerJavaMethod(String name, Class<?>[] paramTypes)
	{
		v8Object.registerJavaMethod(obj, name, name, paramTypes);
	}

	@Override
	public void close()
	{
		appScript.removeObject(key);
		v8Object.release();
	}
}