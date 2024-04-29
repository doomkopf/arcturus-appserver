package com.arcturus.appserver.system.message;

import java.nio.ByteBuffer;
import java.util.UUID;

import com.arcturus.appserver.buffer.ByteBufferTools;
import com.arcturus.appserver.buffer.DynamicByteBuffer;
import com.arcturus.appserver.system.SerializableMessage;

/**
 * Wrapping another {@link SerializableMessage} adding the info necessary to
 * send to a service.
 * 
 * @author doomkopf
 */
public class ServiceMessage implements SerializableMessage
{
	private String appId;
	private String serviceName;
	private UUID sourceNode;
	private SerializableMessage message;

	public ServiceMessage()
	{
	}

	public ServiceMessage(
			String appId,
			String serviceName,
			UUID sourceNode,
			SerializableMessage message)
	{
		this.appId = appId;
		this.serviceName = serviceName;
		this.sourceNode = sourceNode;
		this.message = message;
	}

	public String getAppId()
	{
		return appId;
	}

	public String getServiceName()
	{
		return serviceName;
	}

	public UUID getSourceNode()
	{
		return sourceNode;
	}

	public SerializableMessage getMessage()
	{
		return message;
	}

	@Override
	public ServiceMessage getServiceMessage()
	{
		return this;
	}

	@Override
	public SerializableMessageType getType()
	{
		return SerializableMessageType.entityService;
	}

	@Override
	public void serializeToBuffer(DynamicByteBuffer byteBuffer)
	{
		byteBuffer.putInt(getType().ordinal());

		byteBuffer.putStringWithLength(appId);
		byteBuffer.putStringWithLength(serviceName);
		byteBuffer.putUUID(sourceNode);

		message.serializeToBuffer(byteBuffer);
	}

	@Override
	public void deserializeFromByteBuffer(ByteBuffer byteBuffer)
	{
		byteBuffer.getInt();

		appId = ByteBufferTools.readStringWithLengthHeader(byteBuffer);
		serviceName = ByteBufferTools.readStringWithLengthHeader(byteBuffer);
		sourceNode = ByteBufferTools.readUUID(byteBuffer);

		message = SerializableMessage.deserialize(byteBuffer);
	}
}