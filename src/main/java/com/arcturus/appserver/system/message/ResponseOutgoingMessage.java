package com.arcturus.appserver.system.message;

import java.nio.ByteBuffer;

import com.arcturus.appserver.buffer.ByteBufferTools;
import com.arcturus.appserver.buffer.DynamicByteBuffer;
import com.arcturus.appserver.system.SerializableMessage;

/**
 * A response that is sent back to a client.
 * 
 * @author doomkopf
 */
public class ResponseOutgoingMessage implements SerializableMessage
{
	private long requestId;
	private String payload;

	public ResponseOutgoingMessage()
	{
	}

	public ResponseOutgoingMessage(long requestId, String payload)
	{
		this.requestId = requestId;
		this.payload = payload;
	}

	public long getRequestId()
	{
		return requestId;
	}

	public String getPayload()
	{
		return payload;
	}

	@Override
	public ResponseOutgoingMessage getResponseOutgoingMessage()
	{
		return this;
	}

	@Override
	public SerializableMessageType getType()
	{
		return SerializableMessageType.responseOutgoing;
	}

	@Override
	public void serializeToBuffer(DynamicByteBuffer byteBuffer)
	{
		byteBuffer.putInt(getType().ordinal());

		byteBuffer.putLong(requestId);
		byteBuffer.putStringWithLength(payload);
	}

	@Override
	public void deserializeFromByteBuffer(ByteBuffer byteBuffer)
	{
		byteBuffer.getInt();

		requestId = byteBuffer.getLong();
		payload = ByteBufferTools.readStringWithLengthHeader(byteBuffer);
	}
}