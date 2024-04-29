package com.arcturus.appserver.system.app.type.js.script;

import com.arcturus.appserver.system.app.type.js.*;

public class JavaObjectsContainer
{
	final JsServices services;
	final JsListServices listServices;
	final JsTools jsTools;
	final JsUserSender userSender;
	final JsResponseSender responseSender;
	final JsTransactionManager transactionManager;
	final JsAggregationService jsAggregationService;
	final AppLoggerJsWrapper appLoggerJsWrapper;
	final JsHttpClient httpClient;
	final JsFileReader fileReader;

	public JavaObjectsContainer(
		JsServices services,
		JsListServices listServices,
		JsTools jsTools,
		JsUserSender userSender,
		JsResponseSender responseSender,
		JsTransactionManager transactionManager,
		JsAggregationService jsAggregationService,
		AppLoggerJsWrapper appLoggerJsWrapper,
		JsHttpClient httpClient,
		JsFileReader fileReader
	)
	{
		this.services = services;
		this.listServices = listServices;
		this.jsTools = jsTools;
		this.userSender = userSender;
		this.responseSender = responseSender;
		this.transactionManager = transactionManager;
		this.jsAggregationService = jsAggregationService;
		this.appLoggerJsWrapper = appLoggerJsWrapper;
		this.httpClient = httpClient;
		this.fileReader = fileReader;
	}
}