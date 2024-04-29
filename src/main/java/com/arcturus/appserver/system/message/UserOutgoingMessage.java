package com.arcturus.appserver.system.message;

import java.nio.ByteBuffer;
import java.util.UUID;

import com.arcturus.appserver.buffer.ByteBufferTools;
import com.arcturus.appserver.buffer.DynamicByteBuffer;
import com.arcturus.appserver.system.SerializableMessage;

/**
 * A message that is sent to a user/client (a response or pushed message).
 * 
 * @author doomkopf
 */
public class UserOutgoingMessage implements SerializableMessage
{
	private UUID userId;
	private String payload;

	public UserOutgoingMessage()
	{
	}

	public UserOutgoingMessage(UUID userId, String payload)
	{
		this.userId = userId;
		this.payload = payload;
	}

	public UUID getUserId()
	{
		return userId;
	}

	public String getPayload()
	{
		return payload;
	}

	@Override
	public UserOutgoingMessage getUserOutgoingMessage()
	{
		return this;
	}

	@Override
	public SerializableMessageType getType()
	{
		return SerializableMessageType.userOutgoing;
	}

	@Override
	public void serializeToBuffer(DynamicByteBuffer byteBuffer)
	{
		byteBuffer.putInt(getType().ordinal());

		byteBuffer.putUUID(userId);
		byteBuffer.putStringWithLength(payload);
	}

	@Override
	public void deserializeFromByteBuffer(ByteBuffer byteBuffer)
	{
		byteBuffer.getInt();

		userId = ByteBufferTools.readUUID(byteBuffer);
		payload = ByteBufferTools.readStringWithLengthHeader(byteBuffer);
	}
}