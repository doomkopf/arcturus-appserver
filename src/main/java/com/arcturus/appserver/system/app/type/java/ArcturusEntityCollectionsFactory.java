package com.arcturus.appserver.system.app.type.java;

import java.util.HashMap;
import java.util.Map;

import com.arcturus.api.service.entity.EntityCollectionsFactory;

/**
 * @see EntityCollectionsFactory
 * @author doomkopf
 */
public class ArcturusEntityCollectionsFactory implements EntityCollectionsFactory
{
	@Override
	public <K, V> Map<K, V> createMap()
	{
		return new HashMap<>();
	}
}