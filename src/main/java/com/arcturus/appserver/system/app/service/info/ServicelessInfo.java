package com.arcturus.appserver.system.app.service.info;

import java.util.Collection;

public class ServicelessInfo
{
	private final Collection<UseCaseInfo> useCases;

	public ServicelessInfo(Collection<UseCaseInfo> useCases)
	{
		this.useCases = useCases;
	}

	public int getUseCasesCount()
	{
		return useCases.size();
	}

	public Iterable<UseCaseInfo> getUseCasesIterable()
	{
		return useCases;
	}
}