package com.arcturus.appserver.system.app.service;

import com.arcturus.appserver.system.app.service.entity.ArcturusEntityService;
import com.arcturus.appserver.system.app.service.entity.aggregation.entityservice.AggregationEntityServiceProvider;
import com.arcturus.appserver.system.app.service.entity.list.ArcturusListServiceProvider;

public class EntityServiceProvider
{
	private final UserEntityServiceProvider serviceProvider;
	private final ArcturusListServiceProvider listServiceProvider;
	private final AggregationEntityServiceProvider aggregationEntityServiceProvider;

	public EntityServiceProvider(
		UserEntityServiceProvider serviceProvider,
		ArcturusListServiceProvider listServiceProvider,
		AggregationEntityServiceProvider aggregationEntityServiceProvider
	)
	{
		this.serviceProvider = serviceProvider;
		this.listServiceProvider = listServiceProvider;
		this.aggregationEntityServiceProvider = aggregationEntityServiceProvider;
	}

	public ArcturusEntityService get(String name)
	{
		if (AggregationEntityServiceProvider.SERVICE_NAME.equals(name))
		{
			return aggregationEntityServiceProvider.get();
		}

		var service = serviceProvider.getServiceByName(name);
		if (service == null)
		{
			var listService = listServiceProvider.getInternalServiceByName(name);
			if (listService == null)
			{
				return null;
			}

			service = listService.getEntityService();
		}

		return service;
	}
}