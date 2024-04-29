package com.arcturus.appserver.system.app.type.java;

import com.arcturus.api.LoggerFactory;
import com.arcturus.api.service.entity.EntityService;
import com.arcturus.api.service.entity.EntityServiceProvider;
import com.arcturus.api.tool.ClassToStringHasher;
import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.system.app.service.UserEntityServiceProvider;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class JavaEntityServiceProvider implements EntityServiceProvider
{
	private final LoggerFactory loggerFactory;
	private final JsonStringSerializer jsonStringSerializer;
	private final ClassToStringHasher classToStringHasher;
	private final UserEntityServiceProvider serviceProvider;

	private Map<Enum<?>, EntityService> typeToServiceMap = Collections.emptyMap();

	public JavaEntityServiceProvider(
		LoggerFactory loggerFactory,
		JsonStringSerializer jsonStringSerializer,
		ClassToStringHasher classToStringHasher,
		UserEntityServiceProvider serviceProvider
	)
	{
		this.loggerFactory = loggerFactory;
		this.jsonStringSerializer = jsonStringSerializer;
		this.classToStringHasher = classToStringHasher;
		this.serviceProvider = serviceProvider;
	}

	@Override
	public EntityService getEntityServiceByType(Enum<?> type)
	{
		var entityService = typeToServiceMap.get(type);
		if (entityService == null)
		{
			synchronized (this)
			{
				entityService = typeToServiceMap.get(type);
				if (entityService == null)
				{
					var service = serviceProvider.getServiceByName(type.name());
					if (service == null)
					{
						return null;
					}

					var newMap = new HashMap<Enum<?>, EntityService>(typeToServiceMap);
					entityService = new JavaEntityService(loggerFactory,
						service,
						jsonStringSerializer,
						classToStringHasher
					);
					newMap.put(type, entityService);

					typeToServiceMap = newMap;
				}
			}
		}

		return entityService;
	}
}