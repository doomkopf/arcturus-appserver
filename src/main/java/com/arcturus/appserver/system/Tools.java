package com.arcturus.appserver.system;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Static helper methods.
 *
 * @author doomkopf
 */
public interface Tools
{
	int NUMBER_36 = 36;

	static <E> List<E> arrayToList(E[] array)
	{
		var list = new ArrayList<E>(array.length);

		Collections.addAll(list, array);

		return list;
	}

	static long integersToLong(int a, int b)
	{
		return ((long) a << 32) | (b & 0xFFFFFFFFL);
	}

	static long currentDayLong()
	{
		return System.currentTimeMillis() / Constants.DAY_MILLIS;
	}

	static UUID currentDayUUID()
	{
		return new UUID(currentDayLong(), 0);
	}

	static UUID getPreviousDayOf(UUID day)
	{
		return new UUID(day.getMostSignificantBits() - 1, 0);
	}

	static OffsetDateTime fromDayUUID(UUID day)
	{
		return OffsetDateTime.ofInstant(Instant.ofEpochMilli(day.getMostSignificantBits()
			* Constants.DAY_MILLIS), Constants.GMT);
	}

	static Long parseLongFromRadix36EncodedString(String str)
	{
		try
		{
			return Long.valueOf(Long.parseLong(str, NUMBER_36));
		}
		catch (NumberFormatException e)
		{
			return null;
		}
	}

	static String encodeLongToRadix36String(long l)
	{
		return Long.toString(l, NUMBER_36);
	}
}
