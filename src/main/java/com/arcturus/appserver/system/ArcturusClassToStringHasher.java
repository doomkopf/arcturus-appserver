package com.arcturus.appserver.system;

import com.arcturus.api.tool.ClassToStringHasher;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author doomkopf
 * @see ClassToStringHasher
 */
public class ArcturusClassToStringHasher implements ClassToStringHasher
{
	private static String hash(Class<?> clazz)
	{
		return "-" + Integer.toString(clazz.getCanonicalName().hashCode(), Character.MAX_RADIX);
	}

	private Map<Class<?>, String> classToStringMap = Collections.emptyMap();

	@Override
	public String classToString(Class<?> clazz)
	{
		var value = classToStringMap.get(clazz);
		if (value == null)
		{
			value = hash(clazz);
			synchronized (this)
			{
				Map<Class<?>, String> newMap = new HashMap<>(classToStringMap);
				newMap.put(clazz, value);
				classToStringMap = newMap;
			}
		}

		return value;
	}
}