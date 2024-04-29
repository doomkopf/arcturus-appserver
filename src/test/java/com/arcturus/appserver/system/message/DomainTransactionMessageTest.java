package com.arcturus.appserver.system.message;

import com.arcturus.api.service.entity.transaction.TransactionEntity;
import com.arcturus.appserver.buffer.DynamicByteBuffer;
import com.arcturus.appserver.system.SerializableMessage;
import com.arcturus.appserver.system.app.service.entity.transaction.Transaction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.UUID;

class DomainTransactionMessageTest
{
	@Test
	void testSerialization()
	{
		var msg = new DomainTransactionMessage(new Transaction(new UUID(1234, 4321),
			new TransactionEntity[] {
				new TransactionEntity(new UUID(1111, 2222), "service1", "useCase1"),
				new TransactionEntity(new UUID(3333, 4444), "service2", "useCase2")},
			true,
			true
		),
			1,
			4567,
			new UUID(9876, 6789),
			"thePayload"
		);

		var byteBuffer = DynamicByteBuffer.get();
		msg.serializeToBuffer(byteBuffer);
		var bytes = byteBuffer.toByteArrayAndClear();

		var newMsg = SerializableMessage.deserialize(ByteBuffer.wrap(bytes))
			.getDomainTransactionMessage();

		Assertions.assertEquals(msg.getTransaction().getId(), newMsg.getTransaction().getId());
		Assertions.assertEquals(Boolean.valueOf(msg.getTransaction().isInCommitPhase()),
			Boolean.valueOf(newMsg.getTransaction().isInCommitPhase())
		);
		Assertions.assertEquals(Boolean.valueOf(msg.getTransaction().isCancel()),
			Boolean.valueOf(newMsg.getTransaction().isCancel())
		);

		Assertions.assertEquals(msg.getTransaction().getTransactionEntities().length,
			newMsg.getTransaction().getTransactionEntities().length
		);
		for (var i = 0; i < msg.getTransaction().getTransactionEntities().length; i++)
		{
			Assertions.assertEquals(msg.getTransaction().getTransactionEntities()[i].getId(),
				newMsg.getTransaction().getTransactionEntities()[i].getId()
			);
			Assertions.assertEquals(msg.getTransaction().getTransactionEntities()[i].getService(),
				newMsg.getTransaction().getTransactionEntities()[i].getService()
			);
			Assertions.assertEquals(msg.getTransaction().getTransactionEntities()[i].getUseCase(),
				newMsg.getTransaction().getTransactionEntities()[i].getUseCase()
			);
		}

		Assertions.assertEquals(msg.getCurrentEntityIndex(), newMsg.getCurrentEntityIndex());
		Assertions.assertEquals(msg.getRequestId(), newMsg.getRequestId());
		Assertions.assertEquals(msg.getRequestingUserId(), newMsg.getRequestingUserId());
		Assertions.assertEquals(msg.getPayload(), newMsg.getPayload());
	}
}