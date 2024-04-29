package com.arcturus.appserver.system.app.type.java;

import com.arcturus.api.AppConfig;
import com.arcturus.api.service.ServiceAssignment;
import com.arcturus.api.service.UseCase;
import com.arcturus.api.service.UseCaseHandler;
import com.arcturus.api.service.entity.EntityUseCase;
import com.arcturus.api.service.entity.EntityUseCaseHandler;
import com.arcturus.api.service.entity.list.ListServiceConfig;
import com.arcturus.api.service.entity.transaction.EntityTransactionUseCaseHandler;
import com.arcturus.api.tool.ClassToStringHasher;
import com.arcturus.appserver.system.Tools;
import com.arcturus.appserver.system.app.service.info.*;

import java.util.*;

public class JavaServiceInfos implements ServiceInfos
{
	private final AppConfig appConfig;

	private final String[] entityServiceNames;
	private final Map<String, EntityServiceInfo> entityServiceInfoMap = new HashMap<>();
	private final ServicelessInfo servicelessInfo;

	private final Collection<Class<UseCaseHandler>> useCaseHandlerClasses = new LinkedList<>();
	private final Map<String, Class<EntityUseCaseHandler<?>>> entityUseCaseHandlerClasses = new HashMap<>();
	private final Map<String, Class<EntityTransactionUseCaseHandler<?>>> entityTransactionUseCaseHandlerClasses = new HashMap<>();

	public JavaServiceInfos(
		AppConfig appConfig, AppClasses appClasses, ClassToStringHasher classToStringHasher
	)
	{
		this.appConfig = appConfig;
		entityServiceNames = appConfig.entityServiceNames();

		var entityUseCaseInfos = new HashMap<String, Map<String, EntityUseCaseInfo>>();
		var entityTransactionUseCaseInfos = new HashMap<String, Map<String, EntityTransactionUseCaseInfo>>();

		for (var clazz : appClasses.getClassesIterable())
		{
			if (UseCaseHandler.class.isAssignableFrom(clazz))
			{
				useCaseHandlerClasses.add((Class<UseCaseHandler>) clazz);
			}
			else if (EntityUseCaseHandler.class.isAssignableFrom(clazz))
			{
				var entityUseCase = clazz.getAnnotation(EntityUseCase.class);
				if (entityUseCase != null)
				{
					var map = entityUseCaseInfos.computeIfAbsent(entityUseCase.service(),
						k -> new HashMap<>()
					);

					var useCaseId = entityUseCase.id().isEmpty() ?
						classToStringHasher.classToString(clazz) :
						entityUseCase.id();

					map.put(useCaseId, new EntityUseCaseInfo(useCaseId,
						entityUseCase.isCreateEntity(),
						entityUseCase.isPublic(),
						"",
						"",
						""
					));

					entityUseCaseHandlerClasses.put(entityUseCase.service() + '_' + useCaseId,
						(Class<EntityUseCaseHandler<?>>) clazz
					);
				}
				else
				{
					// TODO log
				}
			}
			else if (EntityTransactionUseCaseHandler.class.isAssignableFrom(clazz))
			{
				var serviceAssignment = clazz.getAnnotation(ServiceAssignment.class);
				if (serviceAssignment != null)
				{
					var map = entityTransactionUseCaseInfos.computeIfAbsent(serviceAssignment.service(),
						k -> new HashMap<>()
					);
					var id = classToStringHasher.classToString(clazz);
					map.put(id, new EntityTransactionUseCaseInfo(id));

					entityTransactionUseCaseHandlerClasses.put(serviceAssignment.service()
							+ '_'
							+ classToStringHasher.classToString(clazz),
						(Class<EntityTransactionUseCaseHandler<?>>) clazz
					);
				}
			}
		}

		for (var serviceName : entityServiceNames)
		{
			entityServiceInfoMap.put(serviceName, new EntityServiceInfo(serviceName,
				entityUseCaseInfos.getOrDefault(serviceName, Collections.emptyMap()),
				entityTransactionUseCaseInfos.getOrDefault(serviceName, Collections.emptyMap())
			));
		}

		var useCaseInfos = new ArrayList<UseCaseInfo>(useCaseHandlerClasses.size());
		for (var clazz : useCaseHandlerClasses)
		{
			var useCase = clazz.getAnnotation(UseCase.class);
			useCaseInfos.add(new ServicelessUseCaseInfo(useCase.id(),
				useCase.isPublic(),
				"",
				"",
				""
			));
		}

		servicelessInfo = new ServicelessInfo(useCaseInfos);
	}

	@Override
	public String[] getEntityServiceNames()
	{
		return entityServiceNames;
	}

	@Override
	public EntityServiceInfo getEntityServiceInfoByName(String name)
	{
		return entityServiceInfoMap.get(name);
	}

	@Override
	public Iterable<EntityServiceInfo> getEntityServiceInfoIterable()
	{
		return entityServiceInfoMap.values();
	}

	@Override
	public Iterable<ListServiceConfig> getListServiceConfigIterable()
	{
		return Tools.arrayToList(appConfig.listServiceConfigs());
	}

	@Override
	public ServicelessInfo getServicelessInfo()
	{
		return servicelessInfo;
	}

	public Iterable<Class<UseCaseHandler>> getUseCaseHandlerClassesIterable()
	{
		return useCaseHandlerClasses;
	}

	public Class<EntityUseCaseHandler<?>> getEntityUseCaseHandlerClass(String service, String id)
	{
		return entityUseCaseHandlerClasses.get(service + '_' + id);
	}

	public Class<EntityTransactionUseCaseHandler<?>> getEntityTransactionUseCaseHandlerClass(
		String service, String id
	)
	{
		return entityTransactionUseCaseHandlerClasses.get(service + '_' + id);
	}
}
