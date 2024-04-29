package com.arcturus.appserver.buffer;

import com.arcturus.appserver.system.Constants;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * A byte buffer similar to {@link ByteBuffer} that resizes itself automatically
 * and has some more methods for types to put. This class is not threadsafe.
 *
 * @author doomkopf
 */
public class DynamicByteBuffer
{
	private static final byte BYTE_FALSE = 0;
	static final byte BYTE_TRUE = 1;

	private static final int RESIZE_AMOUNT = 1024;

	private static final ThreadLocal<DynamicByteBuffer> threadLocalDynamicByteBuffer = new ThreadLocal<>();

	public static DynamicByteBuffer get()
	{
		var dynamicByteBuffer = threadLocalDynamicByteBuffer.get();
		if (dynamicByteBuffer == null)
		{
			dynamicByteBuffer = new DynamicByteBuffer(RESIZE_AMOUNT);
			threadLocalDynamicByteBuffer.set(dynamicByteBuffer);
		}

		return dynamicByteBuffer;
	}

	private ByteBuffer byteBuffer;

	DynamicByteBuffer(int capacity)
	{
		byteBuffer = ByteBuffer.allocate(capacity);
	}

	private void checkAndResize(int sizeToAdd)
	{
		if (byteBuffer.remaining() < sizeToAdd)
		{
			var newBuffer = ByteBuffer.allocate(byteBuffer.capacity() + sizeToAdd + RESIZE_AMOUNT);
			var currentPos = byteBuffer.position();
			byteBuffer.position(0);
			newBuffer.put(byteBuffer);
			newBuffer.position(currentPos);
			byteBuffer = newBuffer;
		}
	}

	public byte[] toByteArrayAndClear()
	{
		var bytes = new byte[byteBuffer.position()];
		byteBuffer.position(0);
		byteBuffer.get(bytes);
		byteBuffer.clear();

		return bytes;
	}

	public void putByte(byte value)
	{
		checkAndResize(Byte.BYTES);
		byteBuffer.put(value);
	}

	public void putBoolean(boolean value)
	{
		putByte(value ? BYTE_TRUE : BYTE_FALSE);
	}

	public void putInt(int value)
	{
		checkAndResize(Integer.BYTES);
		byteBuffer.putInt(value);
	}

	public void putLong(long value)
	{
		checkAndResize(Long.BYTES);
		byteBuffer.putLong(value);
	}

	public void putUUID(UUID value)
	{
		if (value == null)
		{
			putBoolean(false);
			return;
		}

		putBoolean(true);
		putLong(value.getMostSignificantBits());
		putLong(value.getLeastSignificantBits());
	}

	private void putBytes(byte[] bytes)
	{
		checkAndResize(bytes.length);
		byteBuffer.put(bytes);
	}

	public void putStringWithLength(String str)
	{
		if (str == null)
		{
			putInt(-1);
			return;
		}

		var bytes = str.getBytes(Constants.CHARSET_UTF8);
		putInt(bytes.length);
		putBytes(bytes);
	}
}