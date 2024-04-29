package com.arcturus.appserver.system.message;

import java.nio.ByteBuffer;
import java.util.UUID;

import com.arcturus.appserver.buffer.ByteBufferTools;
import com.arcturus.appserver.buffer.DynamicByteBuffer;
import com.arcturus.appserver.system.SerializableMessage;

/**
 * A message that is used for the actual processing of domain behavior (use
 * cases in a service).
 * 
 * @author doomkopf
 */
public class DomainMessage implements SerializableMessage
{
	private String useCase;
	private UUID id;
	private long requestId;
	private UUID requestingUserId;
	private String payload;

	public DomainMessage()
	{
	}

	public DomainMessage(
			String useCase,
			UUID id,
			long requestId,
			UUID requestingUserId,
			String payload)
	{
		this.useCase = useCase;
		this.id = id;
		this.requestId = requestId;
		this.requestingUserId = requestingUserId;
		this.payload = payload;
	}

	public String getUseCase()
	{
		return useCase;
	}

	public UUID getId()
	{
		return id;
	}

	public long getRequestId()
	{
		return requestId;
	}

	public UUID getRequestingUserId()
	{
		return requestingUserId;
	}

	public String getPayload()
	{
		return payload;
	}

	@Override
	public DomainMessage getDomainMessage()
	{
		return this;
	}

	@Override
	public SerializableMessageType getType()
	{
		return SerializableMessageType.domain;
	}

	@Override
	public void serializeToBuffer(DynamicByteBuffer byteBuffer)
	{
		byteBuffer.putInt(getType().ordinal());

		byteBuffer.putUUID(id);
		byteBuffer.putLong(requestId);
		byteBuffer.putUUID(requestingUserId);
		byteBuffer.putStringWithLength(useCase);
		byteBuffer.putStringWithLength(payload);
	}

	@Override
	public void deserializeFromByteBuffer(ByteBuffer byteBuffer)
	{
		byteBuffer.getInt();

		id = ByteBufferTools.readUUID(byteBuffer);
		requestId = byteBuffer.getLong();
		requestingUserId = ByteBufferTools.readUUID(byteBuffer);
		useCase = ByteBufferTools.readStringWithLengthHeader(byteBuffer);
		payload = ByteBufferTools.readStringWithLengthHeader(byteBuffer);
	}
}