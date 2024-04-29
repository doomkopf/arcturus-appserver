package com.arcturus.appserver.system.app;

import com.arcturus.appserver.system.app.service.info.ServiceInfos;

import java.util.Collection;
import java.util.HashSet;

public class PrivateUseCases
{
	private static String toKey(String service, String useCaseId)
	{
		return (service == null ? "" : service) + '_' + useCaseId;
	}

	private final Collection<String> privateUseCases = new HashSet<>(); // NOSONAR

	public PrivateUseCases(ServiceInfos serviceInfos)
	{
		for (var useCaseInfo : serviceInfos.getServicelessInfo().getUseCasesIterable())
		{
			if (!useCaseInfo.isPublic())
			{
				privateUseCases.add(toKey(null, useCaseInfo.getId()));
			}
		}

		for (var entityServiceInfo : serviceInfos.getEntityServiceInfoIterable())
		{
			for (var entityUseCaseInfo : entityServiceInfo.getUseCasesIterable())
			{
				if (!entityUseCaseInfo.isPublic())
				{
					privateUseCases.add(toKey(entityServiceInfo.getName(),
						entityUseCaseInfo.getId()
					));
				}
			}
		}
	}

	public boolean isPrivate(String service, String useCaseId)
	{
		return privateUseCases.contains(toKey(service, useCaseId));
	}
}
