package com.arcturus.appserver.system.message;

import com.arcturus.appserver.buffer.DynamicByteBuffer;
import com.arcturus.appserver.system.SerializableMessage;
import com.arcturus.appserver.system.message.management.EntityManagementMessageBehavior;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.UUID;

class ServiceMessageTest
{
	@Test
	void testSerialization()
	{
		testSerializationWithData("appId",
			"serviceName",
			new UUID(7654, 3458),
			new UUID(54342, 4567765)
		);
	}

	@Test
	void testSerializationWithSourceNodeNull()
	{
		testSerializationWithData("appId", "serviceName", null, new UUID(54342, 4567765));
	}

	private static void testSerializationWithData(
		String appId, String serviceName, UUID sourceNode, UUID entityId
	)
	{
		var msg = new ServiceMessage(appId,
			serviceName,
			sourceNode,
			new EntityManagementMessage(EntityManagementMessageBehavior.kill, entityId)
		);

		var byteBuffer = DynamicByteBuffer.get();
		msg.serializeToBuffer(byteBuffer);
		var bytes = byteBuffer.toByteArrayAndClear();

		var newMsg = SerializableMessage.deserialize(ByteBuffer.wrap(bytes)).getServiceMessage();
		var managementMessage = newMsg.getMessage().getManagementMessage();

		Assertions.assertEquals(appId, newMsg.getAppId());
		Assertions.assertEquals(serviceName, newMsg.getServiceName());
		Assertions.assertEquals(sourceNode, newMsg.getSourceNode());
		Assertions.assertEquals(EntityManagementMessageBehavior.kill,
			managementMessage.getBehavior()
		);
		Assertions.assertEquals(entityId, managementMessage.getEntityId());
	}
}