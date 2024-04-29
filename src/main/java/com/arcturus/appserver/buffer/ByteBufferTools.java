package com.arcturus.appserver.buffer;

import com.arcturus.appserver.system.Constants;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Some helper methods for {@link ByteBuffer}.
 *
 * @author doomkopf
 */
public interface ByteBufferTools
{
	static String readStringWithLengthHeader(ByteBuffer byteBuffer)
	{
		var len = byteBuffer.getInt();
		if (len == -1)
		{
			return null;
		}

		var bytes = new byte[len];
		byteBuffer.get(bytes);

		return new String(bytes, Constants.CHARSET_UTF8);
	}

	static UUID readUUID(ByteBuffer byteBuffer)
	{
		if (readBoolean(byteBuffer))
		{
			return new UUID(byteBuffer.getLong(), byteBuffer.getLong());
		}

		return null;
	}

	static boolean readBoolean(ByteBuffer byteBuffer)
	{
		return byteBuffer.get() == DynamicByteBuffer.BYTE_TRUE;
	}
}