ACTION_STORE = 1;

arc.entity.user = arc.createEntityService();
arc.entity.bankAccount = arc.createEntityService();
arc.entity.person = arc.createEntityService();
arc.entity.personNameTestList = arc.createListService('string');
arc.entity.personNameList = arc.createListService('uuid');
arc.entity.personAgeList = arc.createListService('uuid');

arc.entity.user.defaultEntity = function (id) {
	return {
		name: '',
		bankAccount1: null,
		bankAccount2: null
	}
}

arc.entity.bankAccount.defaultEntity = function (id) {
	return {
		money: 0
	}
}

arc.entity.user.initEntity = function (entityJson) {
	return entityJson
}

arc.entity.bankAccount.initEntity = function (entityJson) {
	return entityJson
}

arc.entity.bankAccount.update = function (entity, id, now, deltaTime) {
}

arc.entity.person.defaultEntity = function (id) {
	return { name: '', age: 0 }
}

arc.entity.person.initEntity = function (entityJson) {
	return entityJson
}

MAX_NAME_LENGTH = 8
arc.entity.user.func.pub.create.setName = {}
arc.entity.user.func.pub.create.setName.description = 'Sets the freakin name of a user'
arc.entity.user.func.pub.create.setName.requestExample = { name: 'Screwhead' }
arc.entity.user.func.pub.create.setName.responseExample = { uc: 'setName', status: 'ok', name: 'Screwhead' }
arc.entity.user.func.pub.create.setName.func = function (entity, id, requestId, requestingUserId, payload) {
	if (payload.name.length > MAX_NAME_LENGTH) {
		responseSender.send(requestId, { uc: 'setName', status: 'nameTooLong' })
		return;
	}

	entity.name = payload.name

	responseSender.send(requestId, { uc: 'setName', status: 'ok', name: entity.name })

	return ACTION_STORE;
}

arc.entity.bankAccount.func.pub.create.create = function (entity, id, requestId, requestingUserId, payload) {
	entity.money = payload.money

	services.send('user', 'writeBankAccount', requestingUserId, requestId, requestingUserId, { bankAccountId: id })

	return ACTION_STORE;
}

arc.entity.user.func.pri.load.writeBankAccount = function (entity, id, requestId, requestingUserId, payload) {
	if (entity.bankAccount1 != null && entity.bankAccount2 != null) {
		entity.bankAccount1 = null
		entity.bankAccount2 = null
	}

	if (entity.bankAccount1 == null) {
		entity.bankAccount1 = payload.bankAccountId
	} else {
		entity.bankAccount2 = payload.bankAccountId
	}

	responseSender.send(requestId, { uc: 'create', status: 'ok', id: payload.bankAccountId })

	return ACTION_STORE;
}

arc.entity.user.func.pub.load.transferMoney = function (entity, id, requestId, requestingUserId, payload) {
	transactionManager.startTransaction({
		entities: [
			{
				id: entity.bankAccount1,
				service: 'bankAccount',
				func: 'account1'
			},
			{
				id: entity.bankAccount2,
				service: 'bankAccount',
				func: 'account2'
			}
		]
	}, requestId, requestingUserId, payload)
}

arc.entity.bankAccount.tfunc.account1 = {};
arc.entity.bankAccount.tfunc.account1.validate = function (entity, id, requestId, requestingUserId, payload) {
	if (entity.money != 100) {
		return { ok: false }
	}

	tools.sleep(500)

	return { ok: true, payload: {} }
}

arc.entity.bankAccount.tfunc.account1.commit = function (entity, id, requestId, requestingUserId, payload) {
	entity.money = 50
	return ACTION_STORE;
}

arc.entity.bankAccount.tfunc.account2 = {};
arc.entity.bankAccount.tfunc.account2.validate = function (entity, id, requestId, requestingUserId, payload) {
	if (entity.money != 0) {
		return { ok: false }
	}

	return { ok: true, payload: {} }
}

arc.entity.bankAccount.tfunc.account2.commit = function (entity, id, requestId, requestingUserId, payload) {
	entity.money = 50
	return ACTION_STORE;
}

arc.entity.bankAccount.func.pub.load.mutationAttemptDuringTransaction = function (entity, id, requestId, requestingUserId, payload) {
	entity.money = entity.money / 2

	responseSender.send(requestId, { uc: 'mutationAttemptDuringTransaction', moneyOfFirstAccount: entity.money })

	return ACTION_STORE;
}

arc.func.pub.serviceless = function (requestId, requestingUserId, payload) {
	responseSender.send(requestId, { uc: 'serviceless' })
}

arc.func.pub.servicelessWithDoc = {}
arc.func.pub.servicelessWithDoc.description = 'Some useless usecase - but hey it is documented'
arc.func.pub.servicelessWithDoc.requestExample = { someField: 'bullshit' }
arc.func.pub.servicelessWithDoc.responseExample = { nothing: 'yes that is right' }
arc.func.pub.servicelessWithDoc.func = function (requestId, requestingUserId, payload) {
	responseSender.send(requestId, arc.usecase.servicelessWithDoc.responseExample)
}

arc.entity.bankAccount.func.pub.load.read = function (entity, id, requestId, requestingUserId, payload) {
	responseSender.send(requestId, { uc: 'read', money: entity.money })
}

arc.entity.user.func.pub.load.internalErrorTest = function (entity, id, requestId, requestingUserId, payload) {
	test = arc.whatever.test
}

arc.func.pub.internalErrorServicelessTest = function (requestId, requestingUserId, payload) {
	test = arc.whatever.test
}

arc.entity.person.func.pub.create.createPerson = function (entity, id, requestId, requestingUserId, payload) {
	entity.name = payload.name;
	entity.age = payload.age;

	listServices.add('personNameTestList', entity.name, payload.testSessionId);

	listServices.add('personNameList', id, tools.hashStringToUUID(entity.name));
	listServices.add('personAgeList', id, tools.createUUID(entity.age, 0));

	responseSender.send(requestId, { uc: 'createPerson', status: 'ok' });

	return ACTION_STORE;
}

arc.func.pub.getAllPersons = function (requestId, requestingUserId, payload) {
	listServices.collect(
		'personNameTestList',
		'getAllPersonsFinal',
		payload.testSessionId,
		requestId,
		requestingUserId
	);
}

arc.func.pri.getAllPersonsFinal = function (requestId, requestingUserId, payload) {
	responseSender.send(requestId, { uc: 'getAllPersons', names: payload.list });
}

arc.func.pub.getPersonsAgg = function (requestId, requestingUserId, payload) {
	aggregationService.start(
		'person',
		'map',
		'getAllPersonsAggFinal',
		{
			indices: [
				{
					name: 'personNameList',
					id: tools.hashStringToUUID('testName' + payload.testSessionId)
				},
				{
					name: 'personAgeList',
					id: tools.createUUID(10, 0)
				}
			]
		},
		requestId,
		requestingUserId
	);
}

arc.entity.person.mapper.map = function (entity, id) {
	return { id: id, name: entity.name, age: entity.age };
}

arc.func.pri.getAllPersonsAggFinal = function (requestId, requestingUserId, payload) {
	var names = [];
	for (i in payload.list) {
		element = payload.list[i];
		names.push(element.name);
	};
	responseSender.send(requestId, { uc: 'getPersonsAgg', names: names });

	userSender.send(requestingUserId, { uc: 'userSenderPush' });
}