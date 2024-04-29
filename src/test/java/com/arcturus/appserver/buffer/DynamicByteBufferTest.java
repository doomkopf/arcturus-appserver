package com.arcturus.appserver.buffer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.UUID;

class DynamicByteBufferTest
{
	@Test
	void testToByteArrayAndClear()
	{
		var intValue = 97531;
		var theUUID = new UUID(1234, 9876);
		var theString = "thisIsThe SuperString";
		byte theByte = Byte.MAX_VALUE / 2;

		var byteBuffer = new DynamicByteBuffer(1);
		byteBuffer.putInt(intValue);
		byteBuffer.putUUID(theUUID);
		byteBuffer.putStringWithLength(theString);
		byteBuffer.putByte(theByte);
		byteBuffer.putBoolean(true);

		var bytes = byteBuffer.toByteArrayAndClear();
		var newByteBuffer = ByteBuffer.wrap(bytes);

		Assertions.assertEquals(intValue, newByteBuffer.getInt());
		Assertions.assertEquals(theUUID, ByteBufferTools.readUUID(newByteBuffer));
		Assertions.assertEquals(theString,
			ByteBufferTools.readStringWithLengthHeader(newByteBuffer)
		);
		Assertions.assertEquals(theByte, newByteBuffer.get());
		Assertions.assertTrue(ByteBufferTools.readBoolean(newByteBuffer));
	}

	@Test
	void testNullString()
	{
		var byteBuffer = new DynamicByteBuffer(1);

		byteBuffer.putStringWithLength(null);

		var bytes = byteBuffer.toByteArrayAndClear();
		var newByteBuffer = ByteBuffer.wrap(bytes);

		Assertions.assertNull(ByteBufferTools.readStringWithLengthHeader(newByteBuffer));
	}

	@Test
	void testNullUUID()
	{
		var byteBuffer = new DynamicByteBuffer(1);

		byteBuffer.putUUID(null);

		var bytes = byteBuffer.toByteArrayAndClear();
		var newByteBuffer = ByteBuffer.wrap(bytes);

		Assertions.assertNull(ByteBufferTools.readUUID(newByteBuffer));
	}
}