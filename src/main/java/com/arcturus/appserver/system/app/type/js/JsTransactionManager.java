package com.arcturus.appserver.system.app.type.js;

import com.arcturus.api.service.entity.transaction.TransactionBuilder;
import com.arcturus.api.service.entity.transaction.TransactionEntity;
import com.arcturus.appserver.json.JsonFactory;
import com.arcturus.appserver.system.Tools;
import com.arcturus.appserver.system.app.service.entity.transaction.ArcturusTransactionManager;

import java.util.UUID;

public class JsTransactionManager
{
	private static final String KEY_ENTITIES = "entities";
	private static final String KEY_ID = "id";
	private static final String KEY_SERVICE = "service";
	private static final String KEY_USECASE = "func";

	private final ArcturusTransactionManager transactionManager;
	private final JsonFactory jsonFactory;

	public JsTransactionManager(
		ArcturusTransactionManager transactionManager, JsonFactory jsonFactory
	)
	{
		this.transactionManager = transactionManager;
		this.jsonFactory = jsonFactory;
	}

	public void startTransaction(
		String entities, String requestId, String requestingUserId, String payload
	)
	{
		var json = jsonFactory.parseReadonly(entities);

		var entitiesArray = json.getArray(KEY_ENTITIES);

		var transactionBuilder = TransactionBuilder.create();
		for (var i = 0; i < entitiesArray.length(); i++)
		{
			var entity = entitiesArray.getObject(i);
			transactionBuilder.addEntity(new TransactionEntity(
				UUID.fromString(entity.getString(KEY_ID)),
				entity.getString(KEY_SERVICE),
				entity.getString(KEY_USECASE)
			));
		}

		transactionManager.startTransaction(
			transactionBuilder,
			Tools.parseLongFromRadix36EncodedString(requestId).longValue(),
			UUID.fromString(requestingUserId),
			payload
		);
	}
}