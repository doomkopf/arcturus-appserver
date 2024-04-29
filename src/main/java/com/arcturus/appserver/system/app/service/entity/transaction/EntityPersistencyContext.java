package com.arcturus.appserver.system.app.service.entity.transaction;

import com.arcturus.api.service.entity.UseCaseContext;

/**
 * Entity persistency related methods of an {@link UseCaseContext}.
 * 
 * @author doomkopf
 */
public interface EntityPersistencyContext extends UseCaseContext
{
	/**
	 * Writes the entity through to persistency in case it's marked as dirty.
	 * Unmarks it after that.
	 */
	void writeThroughIfDirty();
}