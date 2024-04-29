package com.arcturus.appserver.system.app.type.js;

import com.arcturus.api.tool.StringToUuidHasher;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Some helper methods that can be used in JS based apps.
 *
 * @author doomkopf
 */
public class JsTools
{
	private final StringToUuidHasher stringToUuidHasher;

	public JsTools(StringToUuidHasher stringToUuidHasher)
	{
		this.stringToUuidHasher = stringToUuidHasher;
	}

	public String randomUUID()
	{
		return UUID.randomUUID().toString();
	}

	public String createUUID(long mostSigBits, long leastSigBits)
	{
		return new UUID(mostSigBits, leastSigBits).toString();
	}

	public String hashStringToUUID(String str)
	{
		return stringToUuidHasher.hash(str).toString();
	}

	public boolean isUUID(String uuidString)
	{
		try
		{
			UUID.fromString(uuidString);
		}
		catch (IllegalArgumentException e)
		{
			return false;
		}

		return true;
	}

	public double currentTimeMillis()
	{
		return System.currentTimeMillis();
	}

	public double randomNumber(long origin, long bound)
	{
		return ThreadLocalRandom.current().nextLong(origin, bound);
	}

	public void println(String obj)
	{
		System.out.println(obj); // NOSONAR
	}

	public void sleep(long millis)
	{
		try
		{
			Thread.sleep(millis);
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
		}
	}
}