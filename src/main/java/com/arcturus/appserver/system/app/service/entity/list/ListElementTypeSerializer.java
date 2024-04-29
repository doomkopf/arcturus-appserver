package com.arcturus.appserver.system.app.service.entity.list;

public interface ListElementTypeSerializer<T>
{
	String elementToString(T elem);

	T elementFromString(String str);
}