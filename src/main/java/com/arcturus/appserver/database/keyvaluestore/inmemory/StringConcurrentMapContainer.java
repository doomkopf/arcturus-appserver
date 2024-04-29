package com.arcturus.appserver.database.keyvaluestore.inmemory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A wrapper to have a concrete type for DI. Used for having direct access to
 * the underlying {@link Map} of a {@link InMemorySyncKeyValueStore}.
 * 
 * @author doomkopf
 */
public class StringConcurrentMapContainer
{
	private final ConcurrentMap<String, String> map = new ConcurrentHashMap<>();

	public ConcurrentMap<String, String> getConcurrentMap()
	{
		return map;
	}
}