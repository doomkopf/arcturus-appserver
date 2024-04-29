package com.arcturus.appserver.system.app.type.js.script;

import com.arcturus.api.ArcturusAppException;

public class JavaObjectsRegistrator
{
	private JavaObjectsContainer javaObjectsContainer;

	public void init(JavaObjectsContainer javaObjectsContainer)
	{
		this.javaObjectsContainer = javaObjectsContainer;
	}

	void registerJavaObjects(AppScript appScript) throws ArcturusAppException
	{
		AppScriptObject appScriptObject;

		appScriptObject = appScript.registerObject("tools", javaObjectsContainer.jsTools);
		appScriptObject.registerJavaMethod("randomUUID", null);
		appScriptObject.registerJavaMethod("createUUID", new Class<?>[] {long.class, long.class});
		appScriptObject.registerJavaMethod("hashStringToUUID", new Class<?>[] {String.class});
		appScriptObject.registerJavaMethod("isUUID", new Class<?>[] {String.class});
		appScriptObject.registerJavaMethod("currentTimeMillis", null);
		appScriptObject.registerJavaMethod("randomNumber", new Class<?>[] {long.class, long.class});
		appScriptObject.registerJavaMethod("println", new Class<?>[] {String.class});
		appScriptObject.registerJavaMethod("sleep", new Class<?>[] {long.class});

		appScriptObject = appScript.registerObject("_userSender", javaObjectsContainer.userSender);
		appScriptObject.registerJavaMethod("send", new Class<?>[] {String.class, String.class});

		appScriptObject = appScript.registerObject("_responseSender",
			javaObjectsContainer.responseSender
		);
		appScriptObject.registerJavaMethod("send", new Class<?>[] {String.class, String.class});

		appScriptObject = appScript.registerObject("_transactionManager",
			javaObjectsContainer.transactionManager
		);
		appScriptObject.registerJavaMethod("startTransaction",
			new Class<?>[] {String.class, String.class, String.class, String.class}
		);

		appScriptObject = appScript.registerObject("_aggregationService",
			javaObjectsContainer.jsAggregationService
		);
		appScriptObject.registerJavaMethod("start", new Class<?>[] {
			String.class, String.class, String.class, String.class, String.class, String.class});

		appScriptObject = appScript.registerObject("log", javaObjectsContainer.appLoggerJsWrapper);
		appScriptObject.registerJavaMethod("log", new Class<?>[] {int.class, String.class});

		appScriptObject = appScript.registerObject("_httpClient", javaObjectsContainer.httpClient);
		appScriptObject.registerJavaMethod("request", new Class<?>[] {
			String.class,
			int.class,
			String.class,
			String.class,
			String.class,
			String.class,
			String.class,
			String.class});

		appScriptObject = appScript.registerObject("_fileReader", javaObjectsContainer.fileReader);
		appScriptObject.registerJavaMethod("read", new Class<?>[] {String.class});

		appScriptObject = appScript.registerObject("_services", javaObjectsContainer.services);
		appScriptObject.registerJavaMethod("send", new Class<?>[] {
			String.class, String.class, String.class, String.class, String.class, String.class});

		appScriptObject = appScript.registerObject("listServices",
			javaObjectsContainer.listServices
		);
		appScriptObject.registerJavaMethod("add",
			new Class<?>[] {String.class, String.class, String.class}
		);
		appScriptObject.registerJavaMethod("remove",
			new Class<?>[] {String.class, String.class, String.class}
		);
		appScriptObject.registerJavaMethod("collect",
			new Class<?>[] {String.class, String.class, String.class, String.class, String.class}
		);
	}
}