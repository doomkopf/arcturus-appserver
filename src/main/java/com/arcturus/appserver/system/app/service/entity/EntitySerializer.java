package com.arcturus.appserver.system.app.service.entity;

/**
 * For serializing an entity/json back and forth, making sure the entities
 * version is always added.
 * 
 * @author doomkopf
 */
public interface EntitySerializer<E>
{
	String entityToString(E entity, int version);

	E entityFromString(String str);
}