package com.arcturus.appserver.json;

/**
 * @author doomkopf
 */
public interface ReadonlyJsonObject extends Iterable<String>
{
	boolean has(String name);

	Integer getInt(String name);

	Long getLong(String name);

	Boolean getBool(String name);

	String getString(String name);

	ReadonlyJsonObject getObject(String name);

	boolean isObject(String name);

	ReadonlyJsonArray getArray(String name);

	boolean isArray(String name);
}