package com.arcturus.appserver.system.app.type.java;

import com.arcturus.api.service.entity.EntityFactory;

public interface JavaEntityFactory<E> extends EntityFactory<E>
{
	int getCurrentVersion();
}