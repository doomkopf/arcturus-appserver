var ARC_SERVICE_ENTITY_STANDARD = 0;
var ARC_SERVICE_ENTITY_LIST = 1;

var arc = {
	createEntityService: function () {
		return {
			type: ARC_SERVICE_ENTITY_STANDARD,
			currentVersion: 1,
			func: {
				pub: {
					create: {},
					load: {}
				},
				pri: {
					create: {},
					load: {}
				}
			},
			tfunc: {},
			mapper: {}
		};
	},
	createListService: function (listType) {
		return {
			type: ARC_SERVICE_ENTITY_LIST,
			listType: listType
		};
	},
	entity: {},
	func: {
		pub: {},
		pri: {}
	},
	global: {}
};

var services = {
	send: function (serviceName, useCase, id, requestId, requestingUserId, jsJsonPayload) {
		_services.send(serviceName, useCase, id, requestId, requestingUserId, JSON.stringify(jsJsonPayload));
	}
};

var userSender = {
	send: function (userId, payload) {
		_userSender.send(userId, JSON.stringify(payload));
	}
};

var responseSender = {
	send: function (requestId, payload) {
		_responseSender.send(requestId, JSON.stringify(payload));
	}
};

var transactionManager = {
	startTransaction: function (entities, requestId, requestingUserId, payload) {
		_transactionManager.startTransaction(JSON.stringify(entities), requestId, requestingUserId, JSON.stringify(payload));
	}
};

var aggregationService = {
	start: function (entityServiceName, mappingUseCaseId, finalUseCaseId, aggregationIndicesJsJson, requestId, requestingUserId) {
		_aggregationService.start(entityServiceName, mappingUseCaseId, finalUseCaseId, JSON.stringify(aggregationIndicesJsJson), requestId, requestingUserId);
	}
};

var httpClient = {
	request: function (url, method, body, headersJson, resultFunc, requestId, requestingUserId, payload) {
		_httpClient.request(url, method, body, JSON.stringify(headersJson), resultFunc, requestId, requestingUserId, JSON.stringify(payload));
	}
};

function arc_findEntityServices() {
	var commaStringResult = '';
	for (var service in arc.entity) {
		if (commaStringResult.length > 0) {
			commaStringResult += ',';
		}

		if (arc.entity[service].type === ARC_SERVICE_ENTITY_STANDARD) {
			commaStringResult += service;
		}
	}

	return commaStringResult;
}

function arc_findListServices() {
	var result = {
		services: []
	};

	for (var service in arc.entity) {
		if (arc.entity[service].type === ARC_SERVICE_ENTITY_LIST) {
			var serviceElem = arc.entity[service];
			result.services.push({ name: service, type: serviceElem.listType });
		}
	}

	return JSON.stringify(result);
}

function parseDocumentation(useCaseResultObject, useCaseObject) {
	useCaseResultObject.description = useCaseObject.description;
	useCaseResultObject.requestExample = JSON.stringify(useCaseObject.requestExample);
	useCaseResultObject.responseExample = JSON.stringify(useCaseObject.responseExample);
}

function arc_findUseCases(findPublic) {
	var result = {
		uc: []
	};

	var visibility;
	if (findPublic) {
		visibility = 'pub';
	} else {
		visibility = 'pri';
	}

	for (var func in arc.func[visibility]) {
		var useCaseObject = arc.func[visibility][func];
		var useCaseResultObject = {
			id: func
		};

		if (useCaseObject.func) {
			useCaseResultObject.hasFunc = true;
			parseDocumentation(useCaseResultObject, useCaseObject);
		} else {
			useCaseResultObject.hasFunc = false;
		}

		result.uc.push(useCaseResultObject);
	}

	return JSON.stringify(result);
}

function arc_findEntityServiceUseCases(service, findPublic, findCreate) {
	var result = {
		uc: []
	};

	var visibility;
	if (findPublic) {
		visibility = 'pub';
	} else {
		visibility = 'pri';
	}

	var createOrLoad;
	if (findCreate) {
		createOrLoad = 'create';
	} else {
		createOrLoad = 'load';
	}

	for (var func in arc.entity[service].func[visibility][createOrLoad]) {
		var useCaseObject = arc.entity[service].func[visibility][createOrLoad][func];
		var useCaseResultObject = {
			id: func
		};

		if (useCaseObject.func) {
			useCaseResultObject.hasFunc = true;
			parseDocumentation(useCaseResultObject, useCaseObject);
		} else {
			useCaseResultObject.hasFunc = false;
		}

		result.uc.push(useCaseResultObject);
	}

	return JSON.stringify(result);
}

function arc_findEntityMappers(service) {
	var result = {
		uc: []
	};

	for (var func in arc.entity[service].mapper) {
		result.uc.push(func);
	}

	return JSON.stringify(result);
}

function arc_findEntityTransactionServiceUseCases(service) {
	var commaStringResult = '';
	for (var func in arc.entity[service].tfunc) {
		if (commaStringResult.length > 0) {
			commaStringResult += ',';
		}
		commaStringResult += func;
	}

	return commaStringResult;
}

function arc_currentVersion(service) {
	return arc.entity[service].currentVersion;
}

function arc_defaultEntity(service, id) {
	return JSON.stringify(arc.entity[service].defaultEntity(id));
}

function arc_initEntity(service, entityJsonString) {
	return JSON.stringify(arc.entity[service].initEntity(JSON.parse(entityJsonString)));
}

function arc_migrateToV(service, version, jsonString) {
	return JSON.stringify(arc.entity[service]['migrate' + version](JSON.parse(jsonString)));
}

function arc_entityMapper(service, func, entityString, id) {
	return JSON.stringify(arc.entity[service].mapper[func](JSON.parse(entityString), id));
}

function arc_useCase(visibility, func, hasFunc, requestId, requestingUserId, payloadString, requestInfoString) {
	var payload = JSON.parse(payloadString);

	if (func.startsWith('_httpclient_')) {
		httpClientUseCase(visibility, func, hasFunc, requestId, requestingUserId, payload);
	} else {
		executeUseCase(visibility, func, hasFunc, requestId, requestingUserId, payload, !requestInfoString ? null : JSON.parse(requestInfoString));
	}
}

function executeUseCase(visibility, func, hasFunc, requestId, requestingUserId, payload, requestInfo) {
	if (hasFunc) {
		arc.func[visibility][func].func(requestId, requestingUserId, payload, requestInfo);
	} else {
		arc.func[visibility][func](requestId, requestingUserId, payload, requestInfo);
	}
}

function httpClientUseCase(visibility, func, hasFunc, requestId, requestingUserId, payload) {
	payload.payload = JSON.parse(payload.payload);
	executeUseCase(visibility, func, hasFunc, requestId, requestingUserId, payload);
}

function arc_findServicesWithUpdate() {
	var commaStringResult = '';
	for (var service in arc.entity) {
		if (commaStringResult.length > 0) {
			commaStringResult += ',';
		}

		if (arc.entity[service].update) {
			commaStringResult += service;
		}
	}

	return commaStringResult;
}

function arc_entityUseCase(service, visibility, createOrLoad, func, hasFunc, entityString, id, requestId, requestingUserId, payloadString) {
	var entity = JSON.parse(entityString);
	var payload = JSON.parse(payloadString);
	var action;
	if (hasFunc) {
		action = arc.entity[service].func[visibility][createOrLoad][func].func(entity, id, requestId, requestingUserId, payload);
	} else {
		action = arc.entity[service].func[visibility][createOrLoad][func](entity, id, requestId, requestingUserId, payload);
	}

	if (!action) {
		return null;
	}

	//const ACTION_STORE = 1;
	const ACTION_REMOVE = 2;

	if (action === ACTION_REMOVE) {
		return ACTION_REMOVE.toString();
	}

	return JSON.stringify(entity);
}

function arc_update(service, entityString, id, currentTimeMillis, deltaTime) {
	var entity = JSON.parse(entityString);
	if (arc.entity[service].update(entity, id, currentTimeMillis, deltaTime)) {
		return JSON.stringify(entity);
	}

	return null;
}

function arc_entityTransactionUseCaseValidate(service, func, entityString, id, requestId, requestingUserId, payloadString) {
	var entity = JSON.parse(entityString);
	var payload = JSON.parse(payloadString);
	return JSON.stringify(arc.entity[service].tfunc[func].validate(entity, id, requestId, requestingUserId, payload));
}

function arc_entityTransactionUseCaseCommit(service, func, entityString, id, requestId, requestingUserId, payloadString) {
	var entity = JSON.parse(entityString);
	var payload = JSON.parse(payloadString);
	if (arc.entity[service].tfunc[func].commit(entity, id, requestId, requestingUserId, payload)) {
		return JSON.stringify(entity);
	}

	return null;
}

function arc_processGlobals() {
	for (var global in arc.global) {
		var globalElem = arc.global[global];
		arc.global[global] = globalElem.func(_fileReader.read(globalElem.file));
	}
}
