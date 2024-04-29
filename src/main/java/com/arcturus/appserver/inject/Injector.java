package com.arcturus.appserver.inject;

/**
 * In case it is needed to get instances directly from DI.
 * 
 * @author doomkopf
 */
public interface Injector
{
	<T> T getInstance(Class<T> clazz);
}