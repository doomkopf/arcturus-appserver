package com.arcturus.appserver.system.message;

import com.arcturus.appserver.buffer.DynamicByteBuffer;
import com.arcturus.appserver.system.SerializableMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.UUID;

class UserOutgoingMessageTest
{
	private static final String JSON_PAYLOAD =
		"{\"uc\":\"joinArea\",\"id\":\"00000000-0000-0000-0000-000000000000\",\"updInt\":400,\"dimL\":16,\"tiles\":[10002,10002,10002,10002,10002,10002,10002,10002,10002,10002,10002,10002,10002,10002,10002,10002,10002,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10002,10002,10002,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10002,10002,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10002,10002,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10002,10002,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10002,10002,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10002,10002,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10002,10002,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10002,10002,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10002,10002,10000,10002,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10002,10002,10000,10002,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10002,10002,10002,10002,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10002,10002,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10002,10002,10000,10000,10000,10000,10000,10000,10002,10000,10000,10000,10000,10000,10000,10000,10002,10002,10002,10002,10002,10002,10002,10002,10002,10002,10002,10002,10002,10002,10002,10002,10002],\"mo\":["
			+ "{\"id\":\"c767c1f9-337e-4d 75-a5b5-63f eb3b85e40\",\"pos\":{\"x\":1,\"y\":1},\"dir\":{\"x\":0,\"y\":0},\"isMoving\":false,\"speed\":4,\"cr\":0.4}]}";

	@Test
	void testSerialization()
	{
		var userId = new UUID(12, 34);
		var payload = "thePayload";

		var msg = new UserOutgoingMessage(userId, payload);

		var byteBuffer = DynamicByteBuffer.get();
		msg.serializeToBuffer(byteBuffer);
		var bytes = byteBuffer.toByteArrayAndClear();

		var newMsg = SerializableMessage.deserialize(ByteBuffer.wrap(bytes))
			.getUserOutgoingMessage();

		Assertions.assertEquals(userId, newMsg.getUserId());
		Assertions.assertEquals(payload, newMsg.getPayload());
	}

	@Test
	void testBigPayloadSerialization()
	{
		var userId = new UUID(12, 34);

		var msg = new UserOutgoingMessage(userId, JSON_PAYLOAD);

		var byteBuffer = DynamicByteBuffer.get();
		msg.serializeToBuffer(byteBuffer);
		var bytes = byteBuffer.toByteArrayAndClear();

		var newMsg = SerializableMessage.deserialize(ByteBuffer.wrap(bytes))
			.getUserOutgoingMessage();

		Assertions.assertEquals(userId, newMsg.getUserId());
		Assertions.assertEquals(JSON_PAYLOAD, newMsg.getPayload());
	}
}