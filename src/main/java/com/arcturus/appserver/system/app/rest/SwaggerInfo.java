package com.arcturus.appserver.system.app.rest;

import com.arcturus.appserver.json.JsonFactory;
import com.arcturus.appserver.json.JsonObject;
import com.arcturus.appserver.system.app.service.DefaultUseCases;
import com.arcturus.appserver.system.app.service.info.ServiceInfos;
import com.arcturus.appserver.system.app.service.info.UseCaseInfo;

public class SwaggerInfo
{
	private static final String KEY_SCHEMA = "schema";

	private static final String KEY_TYPE_STRING = "string";

	private final String swaggerInfoJson;

	public SwaggerInfo(
		String appId,
		DefaultUseCases defaultRestUseCases,
		ServiceInfos serviceInfos,
		JsonFactory jsonFactory
	)
	{
		var rootJson = jsonFactory.create();
		rootJson.setString("swagger", "2.0");
		rootJson.createObject("info").setString("title", "App");
		var paths = rootJson.createObject("paths");

		for (var useCaseInfo : defaultRestUseCases.getDefaultUseCases())
		{
			createUseCase(appId, "", useCaseInfo, paths, jsonFactory);
		}

		for (var useCaseInfo : serviceInfos.getServicelessInfo().getUseCasesIterable())
		{
			if (useCaseInfo.isPublic())
			{
				createUseCase(appId, "", useCaseInfo, paths, jsonFactory);
			}
		}

		for (var entityServiceInfo : serviceInfos.getEntityServiceInfoIterable())
		{
			for (var useCaseInfo : entityServiceInfo.getUseCasesIterable())
			{
				if (useCaseInfo.isPublic())
				{
					createUseCase(appId,
						entityServiceInfo.getName(),
						useCaseInfo,
						paths,
						jsonFactory
					);
				}
			}
		}

		swaggerInfoJson = rootJson.toString();
	}

	private static void createUseCase(
		String appId,
		String serviceName,
		UseCaseInfo useCaseInfo,
		JsonObject paths,
		JsonFactory jsonFactory
	)
	{
		var p = "/arcapi/" + appId + '/' + useCaseInfo.getId();
		if ((serviceName != null) && !serviceName.isEmpty())
		{
			p += "?service=" + serviceName;
		}

		var path = paths.createObject(p);
		var post = path.createObject("post");
		post.setString("description", useCaseInfo.getDescription());
		var parameters = post.createArray("parameters");
		var responses = post.createObject("responses");

		parameters.createObject()
			.setString("in", "query")
			.setString("name", "entityid")
			.createObject(KEY_SCHEMA)
			.setString("type", KEY_TYPE_STRING);
		parameters.createObject()
			.setString("in", "query")
			.setString("name", "sessionid")
			.createObject(KEY_SCHEMA)
			.setString("type", KEY_TYPE_STRING);

		var request = parameters.createObject();
		request.setString("in", "body");
		request.setString("name", "request");
		var schema = request.createObject(KEY_SCHEMA);

		if ((useCaseInfo.getRequestBody() != null) && !useCaseInfo.getRequestBody().isEmpty())
		{
			schema.setObject("example", jsonFactory.parse(useCaseInfo.getRequestBody()));
		}

		responses.createObject("200")
			.setString("description", "Http status codes not available yet.")
			.createObject("examples")
			.setString("application/json", useCaseInfo.getSuccessResponseBody());
	}

	public String getSwaggerInfoJson()
	{
		return swaggerInfoJson;
	}
}