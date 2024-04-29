package com.arcturus.appserver.database.keyvaluestore;

/**
 * Just to define the type on a concrete class level, which is necessary for
 * certain frameworks like spring that ignore generics.
 * 
 * @author doomkopf
 */
public interface StringKeyValueStore extends KeyValueStore<String, String>
{
	// Nothing
}