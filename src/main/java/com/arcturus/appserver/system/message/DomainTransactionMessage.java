package com.arcturus.appserver.system.message;

import com.arcturus.api.service.entity.transaction.TransactionEntity;
import com.arcturus.appserver.buffer.ByteBufferTools;
import com.arcturus.appserver.buffer.DynamicByteBuffer;
import com.arcturus.appserver.system.SerializableMessage;
import com.arcturus.appserver.system.app.service.entity.transaction.Transaction;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Similar to a {@link DomainMessage} but used for transaction based use cases.
 *
 * @author doomkopf
 */
public class DomainTransactionMessage implements SerializableMessage
{
	private Transaction transaction;
	private int currentEntityIndex;
	private long requestId;
	private UUID requestingUserId;
	private String payload;

	public DomainTransactionMessage()
	{
	}

	public DomainTransactionMessage(
		Transaction transaction,
		int currentEntityIndex,
		long requestId,
		UUID requestingUserId,
		String payload
	)
	{
		this.transaction = transaction;
		this.currentEntityIndex = currentEntityIndex;
		this.requestId = requestId;
		this.requestingUserId = requestingUserId;
		this.payload = payload;
	}

	public Transaction getTransaction()
	{
		return transaction;
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

	public TransactionEntity getCurrentTransactionEntity()
	{
		return transaction.getTransactionEntities()[currentEntityIndex];
	}

	public boolean isAtLastEntity()
	{
		return currentEntityIndex == (transaction.getTransactionEntities().length - 1);
	}

	public int getCurrentEntityIndex()
	{
		return currentEntityIndex;
	}

	@Override
	public DomainTransactionMessage getDomainTransactionMessage()
	{
		return this;
	}

	@Override
	public SerializableMessageType getType()
	{
		return SerializableMessageType.domainTransaction;
	}

	@Override
	public void serializeToBuffer(DynamicByteBuffer byteBuffer)
	{
		byteBuffer.putInt(getType().ordinal());

		transaction.serializeToBuffer(byteBuffer);

		byteBuffer.putInt(currentEntityIndex);
		byteBuffer.putLong(requestId);
		byteBuffer.putUUID(requestingUserId);
		byteBuffer.putStringWithLength(payload);
	}

	@Override
	public void deserializeFromByteBuffer(ByteBuffer byteBuffer)
	{
		byteBuffer.getInt();

		transaction = Transaction.deserializeFromByteBuffer(byteBuffer);

		currentEntityIndex = byteBuffer.getInt();
		requestId = byteBuffer.getLong();
		requestingUserId = ByteBufferTools.readUUID(byteBuffer);
		payload = ByteBufferTools.readStringWithLengthHeader(byteBuffer);
	}
}