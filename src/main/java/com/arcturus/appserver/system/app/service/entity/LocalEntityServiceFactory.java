package com.arcturus.appserver.system.app.service.entity;

import com.arcturus.api.ArcturusAppException;

/**
 * Creates {@link LocalEntityService}s.
 *
 * @author doomkopf
 */
public interface LocalEntityServiceFactory<T>
{
	LocalEntityService<T> create(String name) throws ArcturusAppException;
}