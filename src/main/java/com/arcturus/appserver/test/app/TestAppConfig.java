package com.arcturus.appserver.test.app;

import com.arcturus.api.AppConfig;
import com.arcturus.api.service.entity.list.ListServiceConfig;
import com.arcturus.appserver.system.app.service.entity.list.ListChunk;
import com.arcturus.appserver.system.app.service.entity.list.usecase.CollectElements.CollectElementsMessage;
import com.google.gson.reflect.TypeToken;

import java.util.UUID;

public class TestAppConfig implements AppConfig
{
	@Override
	public String getAppId()
	{
		return "test_java";
	}

	@Override
	public String getRootPackage()
	{
		return TestAppConfig.class.getPackage().getName();
	}

	@Override
	public String[] entityServiceNames()
	{
		return new String[] {
			TestService.user.name(), TestService.bankAccount.name(), TestService.person.name()};
	}

	@Override
	public ListServiceConfig[] listServiceConfigs()
	{
		return new ListServiceConfig[] {
			new ListServiceConfig(TestService.personNameTestList.name(),
				new TypeToken<ListChunk<String>>()
				{
				}.getType(),
				new TypeToken<CollectElementsMessage<String>>()
				{
				}.getType(),
				String.class
			),
			new ListServiceConfig(TestService.personNameList.name(),
				new TypeToken<ListChunk<UUID>>()
				{
				}.getType(),
				new TypeToken<CollectElementsMessage<UUID>>()
				{
				}.getType(),
				UUID.class
			),
			new ListServiceConfig(TestService.personAgeList.name(), new TypeToken<ListChunk<UUID>>()
			{
			}.getType(), new TypeToken<CollectElementsMessage<UUID>>()
			{
			}.getType(), UUID.class)};
	}
}