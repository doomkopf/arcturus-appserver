package com.arcturus.appserver.crypt;

import com.arcturus.api.tool.StringToUuidHasher;
import com.arcturus.appserver.system.Constants;

import java.util.UUID;

public class ArcturusStringToUuidHasher implements StringToUuidHasher
{
	@Override
	public UUID hash(String str)
	{
		return UUID.nameUUIDFromBytes(str.getBytes(Constants.CHARSET_UTF8));
	}
}