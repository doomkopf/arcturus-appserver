package com.arcturus.appserver.system.app.type.js;

import com.arcturus.api.service.entity.list.ListServiceConfig;
import com.arcturus.appserver.json.JsonFactory;
import com.arcturus.appserver.system.app.service.entity.list.ListChunk;
import com.arcturus.appserver.system.app.service.entity.list.usecase.CollectElements.CollectElementsMessage;
import com.arcturus.appserver.system.app.service.info.*;
import com.arcturus.appserver.system.app.type.js.script.InfoAppScript;
import com.arcturus.appserver.system.app.type.js.script.InfoAppScriptFactory;
import com.google.gson.reflect.TypeToken;

import java.util.*;

public class JsServiceInfos implements ServiceInfos
{
	private final String[] entityServiceNames;
	private final Map<String, EntityServiceInfo> entityServiceInfoMap = new HashMap<>();
	private final Map<String, Map<String, Boolean>> serviceUseCaseFunc = new HashMap<>();
	private final Collection<String> servicesWithUpdateFunction = new HashSet<>();
	private final Collection<ListServiceConfig> listServiceConfigs;
	private final ServicelessInfo servicelessInfo;
	private final Map<String, Collection<String>> serviceToMapperUseCaseIds = new HashMap<>();
	private final Map<String, Integer> serviceToEntityVersionMap = new HashMap<>();
	private final JsonFactory jsonFactory;

	public JsServiceInfos(InfoAppScriptFactory infoAppScriptFactory, JsonFactory jsonFactory)
	{
		this.jsonFactory = jsonFactory;

		var useCases = new LinkedList<UseCaseInfo>();
		var useCasesFunc = new HashMap<String, Boolean>();
		try (var appScript = infoAppScriptFactory.create())
		{
			loadServicelessInfo(appScript, true, useCases, useCasesFunc);
			loadServicelessInfo(appScript, false, useCases, useCasesFunc);
			servicelessInfo = new ServicelessInfo(useCases);

			entityServiceNames = appScript.entityServices();

			for (var serviceName : entityServiceNames)
			{
				loadServiceInfo(serviceName, appScript);
			}

			servicesWithUpdateFunction.addAll(Arrays.asList(appScript.findServicesWithUpdateFunction()));

			var listServicesJson = jsonFactory.parseReadonly(appScript.findListServices());
			var listServicesJsonArray = listServicesJson.getArray("services");
			var length = listServicesJsonArray.length();
			listServiceConfigs = new ArrayList<>(length);
			for (var i = 0; i < length; i++)
			{
				var listServiceJson = listServicesJsonArray.getObject(i);
				if ("string".equals(listServiceJson.getString("type")))
				{
					listServiceConfigs.add(new ListServiceConfig(listServiceJson.getString("name"),
						new TypeToken<ListChunk<String>>()
						{
						}.getType(),
						new TypeToken<CollectElementsMessage<String>>()
						{
						}.getType(),
						String.class
					));
				}
				else
				{
					listServiceConfigs.add(new ListServiceConfig(listServiceJson.getString("name"),
						new TypeToken<ListChunk<UUID>>()
						{
						}.getType(),
						new TypeToken<CollectElementsMessage<UUID>>()
						{
						}.getType(),
						UUID.class
					));
				}
			}
		}
	}

	private void loadServicelessInfo(
		InfoAppScript appScript,
		boolean findPublic,
		Collection<UseCaseInfo> useCases,
		Map<String, Boolean> useCaseFunc
	)
	{
		var useCasesArray = jsonFactory.parseReadonly(appScript.findUseCases(findPublic))
			.getArray("uc");
		var length = useCasesArray.length();
		serviceUseCaseFunc.put("", useCaseFunc);
		for (var i = 0; i < length; i++)
		{
			var useCaseJson = useCasesArray.getObject(i);
			var useCaseId = useCaseJson.getString("id");
			useCases.add(new ServicelessUseCaseInfo(useCaseId,
				findPublic,
				useCaseJson.getString("description"),
				useCaseJson.getString("requestExample"),
				useCaseJson.getString("responseExample")
			));

			useCaseFunc.put(useCaseId, useCaseJson.getBool("hasFunc"));
		}
	}

	private void loadServiceInfo(String serviceName, InfoAppScript appScript)
	{
		var useCases = new HashMap<String, EntityUseCaseInfo>();
		var useCaseFunc = new HashMap<String, Boolean>();
		serviceUseCaseFunc.put(serviceName, useCaseFunc);
		loadEntityServiceInfos(appScript, serviceName, true, true, useCases, useCaseFunc);
		loadEntityServiceInfos(appScript, serviceName, true, false, useCases, useCaseFunc);
		loadEntityServiceInfos(appScript, serviceName, false, true, useCases, useCaseFunc);
		loadEntityServiceInfos(appScript, serviceName, false, false, useCases, useCaseFunc);

		var transactionUseCasesArray = appScript.findEntityTransactionServiceUseCases(serviceName);
		var transactionUseCases = new HashMap<String, EntityTransactionUseCaseInfo>();
		for (var useCase : transactionUseCasesArray)
		{
			transactionUseCases.put(useCase, new EntityTransactionUseCaseInfo(useCase));
		}

		entityServiceInfoMap.put(serviceName,
			new EntityServiceInfo(serviceName, useCases, transactionUseCases)
		);

		var json = jsonFactory.parseReadonly(appScript.findEntityMappers(serviceName))
			.getArray("uc");
		var mapperUseCaseIds = new ArrayList<String>(json.length());
		for (var i = 0; i < json.length(); i++)
		{
			var useCaseId = json.getString(i);
			mapperUseCaseIds.add(useCaseId);
		}
		serviceToMapperUseCaseIds.put(serviceName, mapperUseCaseIds);

		serviceToEntityVersionMap.put(serviceName, appScript.currentVersion(serviceName));
	}

	private void loadEntityServiceInfos(
		InfoAppScript appScript,
		String serviceName,
		boolean findPublic,
		boolean findCreate,
		Map<String, EntityUseCaseInfo> useCases,
		Map<String, Boolean> useCaseFunc
	)
	{
		var useCasesJsonString = appScript.findEntityServiceUseCases(serviceName,
			findPublic,
			findCreate
		);

		var json = jsonFactory.parseReadonly(useCasesJsonString);
		var useCasesArray = json.getArray("uc");

		var length = useCasesArray.length();
		for (var i = 0; i < length; i++)
		{
			var useCaseJson = useCasesArray.getObject(i);

			var useCaseId = useCaseJson.getString("id");

			useCases.put(useCaseId, new EntityUseCaseInfo(useCaseId,
				findCreate,
				findPublic,
				useCaseJson.getString("description"),
				useCaseJson.getString("requestExample"),
				useCaseJson.getString("responseExample")
			));

			useCaseFunc.put(useCaseId, useCaseJson.getBool("hasFunc"));
		}
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
		return listServiceConfigs;
	}

	@Override
	public ServicelessInfo getServicelessInfo()
	{
		return servicelessInfo;
	}

	boolean useCaseHasFunc(String service, String useCase)
	{
		if (service == null)
		{
			service = "";
		}

		var hasFunc = serviceUseCaseFunc.get(service).get(useCase);
		if (hasFunc == null)
		{
			return false;
		}

		return hasFunc.booleanValue();
	}

	boolean hasUpdateFunction(String service)
	{
		return servicesWithUpdateFunction.contains(service);
	}

	Collection<String> getMappingUseCaseIds(String service)
	{
		return serviceToMapperUseCaseIds.get(service);
	}

	int getEntityVersion(String service)
	{
		return serviceToEntityVersionMap.get(service).intValue();
	}
}