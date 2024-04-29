package com.arcturus.appserver.system;

import com.arcturus.appserver.buffer.DynamicByteBuffer;
import com.arcturus.appserver.cluster.Node;
import com.arcturus.appserver.system.message.SerializableMessageType;

import java.nio.ByteBuffer;

/**
 * Some messages need to be serializable in order to be transferred via network.
 *
 * @author doomkopf
 */
public interface SerializableMessage extends Message
{
	static SerializableMessage deserialize(ByteBuffer byteBuffer)
	{
		var pos = byteBuffer.position();
		var msg = SerializableMessageType.values()[byteBuffer.getInt()].createMessage();
		byteBuffer.position(pos);

		msg.deserializeFromByteBuffer(byteBuffer);

		return msg;
	}

	SerializableMessageType getType();

	void serializeToBuffer(DynamicByteBuffer byteBuffer);

	void deserializeFromByteBuffer(ByteBuffer byteBuffer);

	default void sendToNode(Node node)
	{
		var dynamicByteBuffer = DynamicByteBuffer.get();
		serializeToBuffer(dynamicByteBuffer);
		node.send(dynamicByteBuffer.toByteArrayAndClear());
	}
}