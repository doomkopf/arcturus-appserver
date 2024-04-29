package com.arcturus.appserver.system.app.type.java;

import com.arcturus.appserver.system.app.service.entity.EntityInitializer;

public class JavaEntityInitializer<E> implements EntityInitializer<E>
{
	@Override
	public E initializeEntity(E entity)
	{
		return entity;
	}
}