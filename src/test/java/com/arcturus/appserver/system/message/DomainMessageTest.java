package com.arcturus.appserver.system.message;

import com.arcturus.appserver.buffer.DynamicByteBuffer;
import com.arcturus.appserver.system.SerializableMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.UUID;

class DomainMessageTest
{
	private static final String useCase = "useCase";
	private static final long requestId = 4321;
	private static final UUID userId = new UUID(12, 34);

	@Test
	void testSerializationWithNullPayload()
	{
		var msg = new DomainMessage(useCase, userId, requestId, userId, null);

		var byteBuffer = DynamicByteBuffer.get();
		msg.serializeToBuffer(byteBuffer);
		var bytes = byteBuffer.toByteArrayAndClear();

		var newMsg = SerializableMessage.deserialize(ByteBuffer.wrap(bytes)).getDomainMessage();

		Assertions.assertEquals(useCase, newMsg.getUseCase());
		Assertions.assertEquals(requestId, newMsg.getRequestId());
		Assertions.assertEquals(userId, newMsg.getRequestingUserId());
		Assertions.assertEquals(userId, newMsg.getId());
		Assertions.assertNull(newMsg.getPayload());
	}

	@Test
	void testSerializationWithNullRequestingUserId()
	{
		var msg = new DomainMessage(useCase, userId, requestId, null, null);

		var byteBuffer = DynamicByteBuffer.get();
		msg.serializeToBuffer(byteBuffer);
		var bytes = byteBuffer.toByteArrayAndClear();

		var newMsg = SerializableMessage.deserialize(ByteBuffer.wrap(bytes)).getDomainMessage();

		Assertions.assertEquals(useCase, newMsg.getUseCase());
		Assertions.assertEquals(requestId, newMsg.getRequestId());
		Assertions.assertNull(newMsg.getRequestingUserId());
		Assertions.assertEquals(userId, newMsg.getId());
		Assertions.assertNull(newMsg.getPayload());
	}
}