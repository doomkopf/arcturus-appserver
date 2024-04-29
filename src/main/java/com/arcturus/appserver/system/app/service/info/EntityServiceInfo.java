package com.arcturus.appserver.system.app.service.info;

import com.arcturus.appserver.system.app.service.entity.EntityUseCaseInfoProvider;

import java.util.Map;

public class EntityServiceInfo implements EntityUseCaseInfoProvider
{
	private final String name;
	private final Map<String, EntityUseCaseInfo> useCases;
	private final Map<String, EntityTransactionUseCaseInfo> transactionUseCases;

	public EntityServiceInfo(
		String name,
		Map<String, EntityUseCaseInfo> useCases,
		Map<String, EntityTransactionUseCaseInfo> transactionUseCases
	)
	{
		this.name = name;
		this.useCases = useCases;
		this.transactionUseCases = transactionUseCases;
	}

	public String getName()
	{
		return name;
	}

	@Override
	public boolean isCreateEntity(String useCaseId)
	{
		var entityUseCaseInfo = useCases.get(useCaseId);
		return (entityUseCaseInfo != null) && entityUseCaseInfo.isCreateEntity();

	}

	public int getUseCasesCount()
	{
		return useCases.size();
	}

	public Iterable<EntityUseCaseInfo> getUseCasesIterable()
	{
		return useCases.values();
	}

	public int getTransactionUseCasesCount()
	{
		return transactionUseCases.size();
	}

	public Iterable<EntityTransactionUseCaseInfo> getTransactionUseCasesIterable()
	{
		return transactionUseCases.values();
	}
}