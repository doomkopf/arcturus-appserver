package com.arcturus.appserver.json;

/**
 * @author doomkopf
 */
public interface ReadonlyJsonArray
{
	int length();

	Integer getInt(int index);

	Long getLong(int index);

	Boolean getBool(int index);

	String getString(int index);

	ReadonlyJsonObject getObject(int index);

	boolean isObject(int index);

	ReadonlyJsonArray getArray(int index);

	boolean isArray(int index);
}