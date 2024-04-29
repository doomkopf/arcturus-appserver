package com.arcturus.appserver.system.message;

import java.nio.ByteBuffer;
import java.util.UUID;

import com.arcturus.appserver.buffer.ByteBufferTools;
import com.arcturus.appserver.buffer.DynamicByteBuffer;
import com.arcturus.appserver.system.SerializableMessage;
import com.arcturus.appserver.system.message.management.AppManagementMessageBehavior;

public class AppManagementMessage implements SerializableMessage
{
	private AppManagementMessageBehavior behavior;
	private String appId;
	private UUID sourceNodeId;

	public AppManagementMessage()
	{
	}

	public AppManagementMessage(
			AppManagementMessageBehavior behavior,
			String appId,
			UUID sourceNodeId)
	{
		this.behavior = behavior;
		this.appId = appId;
		this.sourceNodeId = sourceNodeId;
	}

	public AppManagementMessageBehavior getBehavior()
	{
		return behavior;
	}

	public String getAppId()
	{
		return appId;
	}

	public UUID getSourceNodeId()
	{
		return sourceNodeId;
	}

	@Override
	public AppManagementMessage getAppManagementMessage()
	{
		return this;
	}

	@Override
	public SerializableMessageType getType()
	{
		return SerializableMessageType.appManagement;
	}

	@Override
	public void serializeToBuffer(DynamicByteBuffer byteBuffer)
	{
		byteBuffer.putInt(getType().ordinal());

		byteBuffer.putInt(behavior.ordinal());
		byteBuffer.putStringWithLength(appId);
		byteBuffer.putUUID(sourceNodeId);
	}

	@Override
	public void deserializeFromByteBuffer(ByteBuffer byteBuffer)
	{
		byteBuffer.getInt();

		behavior = AppManagementMessageBehavior.values()[byteBuffer.getInt()];
		appId = ByteBufferTools.readStringWithLengthHeader(byteBuffer);
		sourceNodeId = ByteBufferTools.readUUID(byteBuffer);
	}
}