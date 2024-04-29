package com.arcturus.appserver.system.message;

import java.nio.ByteBuffer;
import java.util.UUID;

import com.arcturus.appserver.buffer.ByteBufferTools;
import com.arcturus.appserver.buffer.DynamicByteBuffer;
import com.arcturus.appserver.system.SerializableMessage;
import com.arcturus.appserver.system.message.management.EntityManagementMessageBehavior;

/**
 * A message that is used internally for technical/non-domain behavior.
 * 
 * @author doomkopf
 */
public class EntityManagementMessage implements SerializableMessage
{
	private EntityManagementMessageBehavior behavior;
	private UUID entityId;

	public EntityManagementMessage()
	{
	}

	public EntityManagementMessage(EntityManagementMessageBehavior behavior, UUID entityId)
	{
		this.behavior = behavior;
		this.entityId = entityId;
	}

	public EntityManagementMessageBehavior getBehavior()
	{
		return behavior;
	}

	public UUID getEntityId()
	{
		return entityId;
	}

	@Override
	public EntityManagementMessage getManagementMessage()
	{
		return this;
	}

	@Override
	public SerializableMessageType getType()
	{
		return SerializableMessageType.entityManagement;
	}

	@Override
	public void serializeToBuffer(DynamicByteBuffer byteBuffer)
	{
		byteBuffer.putInt(getType().ordinal());

		byteBuffer.putInt(behavior.ordinal());
		byteBuffer.putUUID(entityId);
	}

	@Override
	public void deserializeFromByteBuffer(ByteBuffer byteBuffer)
	{
		byteBuffer.getInt();

		behavior = EntityManagementMessageBehavior.values()[byteBuffer.getInt()];
		entityId = ByteBufferTools.readUUID(byteBuffer);
	}
}