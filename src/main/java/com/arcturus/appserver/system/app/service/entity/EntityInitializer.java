package com.arcturus.appserver.system.app.service.entity;

import com.arcturus.api.ArcturusAppException;

public interface EntityInitializer<E>
{
	E initializeEntity(E entity) throws ArcturusAppException;
}