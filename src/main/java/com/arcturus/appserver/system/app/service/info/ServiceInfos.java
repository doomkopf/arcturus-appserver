package com.arcturus.appserver.system.app.service.info;

import com.arcturus.api.service.entity.list.ListServiceConfig;

public interface ServiceInfos
{
	String[] getEntityServiceNames();

	EntityServiceInfo getEntityServiceInfoByName(String name);

	Iterable<EntityServiceInfo> getEntityServiceInfoIterable();

	Iterable<ListServiceConfig> getListServiceConfigIterable();

	ServicelessInfo getServicelessInfo();
}