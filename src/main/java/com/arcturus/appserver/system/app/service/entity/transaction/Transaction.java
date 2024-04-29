package com.arcturus.appserver.system.app.service.entity.transaction;

import com.arcturus.api.service.entity.transaction.TransactionEntity;
import com.arcturus.appserver.buffer.ByteBufferTools;
import com.arcturus.appserver.buffer.DynamicByteBuffer;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * An actual transaction.
 *
 * @author doomkopf
 */
public class Transaction
{
	public static Transaction deserializeFromByteBuffer(ByteBuffer byteBuffer)
	{
		var id = ByteBufferTools.readUUID(byteBuffer);
		var isInCommitPhase = ByteBufferTools.readBoolean(byteBuffer);
		var isCancel = ByteBufferTools.readBoolean(byteBuffer);

		var entities = new TransactionEntity[byteBuffer.getInt()];
		for (var i = 0; i < entities.length; i++)
		{
			entities[i] = new TransactionEntity(
				ByteBufferTools.readUUID(byteBuffer),
				ByteBufferTools.readStringWithLengthHeader(byteBuffer),
				ByteBufferTools.readStringWithLengthHeader(byteBuffer)
			);
		}

		return new Transaction(id, entities, isInCommitPhase, isCancel);
	}

	private final UUID id;
	private final TransactionEntity[] entities;

	private volatile boolean isInCommitPhase;
	private volatile boolean isCancel;

	public Transaction(
		UUID id, TransactionEntity[] entities, boolean isInCommitPhase, boolean isCancel
	)
	{
		this.id = id;
		this.entities = entities;
		this.isInCommitPhase = isInCommitPhase;
		this.isCancel = isCancel;
	}

	public UUID getId()
	{
		return id;
	}

	public void setCancel()
	{
		isCancel = true;
	}

	public boolean isCancel()
	{
		return isCancel;
	}

	public TransactionEntity[] getTransactionEntities()
	{
		return entities;
	}

	public void setCommitPhase()
	{
		isInCommitPhase = true;
	}

	public boolean isInCommitPhase()
	{
		return isInCommitPhase;
	}

	public void serializeToBuffer(DynamicByteBuffer byteBuffer)
	{
		byteBuffer.putUUID(id);
		byteBuffer.putBoolean(isInCommitPhase);
		byteBuffer.putBoolean(isCancel);

		byteBuffer.putInt(entities.length);
		for (var transactionEntity : entities)
		{
			byteBuffer.putUUID(transactionEntity.getId());
			byteBuffer.putStringWithLength(transactionEntity.getService());
			byteBuffer.putStringWithLength(transactionEntity.getUseCase());
		}
	}
}