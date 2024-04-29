package com.arcturus.appserver.system.app.type.js.script;

public class DomainAppScript implements AutoCloseable
{
	private final AppScript appScript;
	private final ThreadLocal<DomainAppScript> threadLocal;

	DomainAppScript(
		AppScript appScript, ThreadLocal<DomainAppScript> threadLocal
	)
	{
		this.appScript = appScript;
		this.threadLocal = threadLocal;
	}

	// Called from random threads including nano proc threads

	@Override
	public void close()
	{
		appScript.close();
		threadLocal.remove();
	}

	public void executeUseCase(
		String visibility,
		String functionName,
		boolean hasFunc,
		String requestId,
		String requestingUserId,
		String jsJsonPayload,
		String requestInfo
	)
	{
		appScript.invokeFunction(ScriptConstants.FUNC_USECASE_WITH_PREFIX,
			visibility,
			functionName,
			hasFunc,
			requestId,
			requestingUserId,
			jsJsonPayload,
			requestInfo
		);
	}

	// Called from nano proc threads only

	public String executeEntityMapper(
		String serviceName, String functionName, String jsJsonEntity, String id
	)
	{
		return (String) appScript.invokeFunction(ScriptConstants.FUNC_ENTITY_MAPPER_WITH_PREFIX,
			serviceName,
			functionName,
			jsJsonEntity,
			id
		);
	}

	public String createDefaultEntity(String serviceName, String id)
	{
		return (String) appScript.invokeFunction(ScriptConstants.FUNC_DEFAULT_ENTITY_WITH_PREFIX,
			serviceName,
			id
		);
	}

	public String initEntity(String serviceName, String entityJson)
	{
		return (String) appScript.invokeFunction(ScriptConstants.FUNC_INIT_ENTITY_WITH_PREFIX,
			serviceName,
			entityJson
		);
	}

	public String migrateToV(
		String serviceName, int version, String jsJson
	)
	{
		return (String) appScript.invokeFunction(ScriptConstants.FUNC_MIGRATE_TO_V_WITH_PREFIX,
			serviceName,
			version,
			jsJson
		);
	}

	public EntityUseCaseResult executeEntityUseCase( // NOSONAR
		String serviceName,
		String visibility,
		String createOrLoad,
		String functionName,
		boolean hasFunc,
		String jsJsonEntity,
		String id,
		String requestId,
		String requestingUserId,
		String jsJsonPayload
	)
	{
		var result = (String) appScript.invokeFunction(ScriptConstants.FUNC_ENTITY_USECASE_WITH_PREFIX,
			serviceName,
			visibility,
			createOrLoad,
			functionName,
			hasFunc,
			jsJsonEntity,
			id,
			requestId,
			requestingUserId,
			jsJsonPayload
		);

		if ((result == null) || result.isEmpty())
		{
			return null;
		}

		if (result.charAt(0) == '{')
		{
			return new EntityUseCaseResult(EntityUseCaseResultAction.STORE, result);
		}

		if (result.charAt(0) == EntityUseCaseResultAction.REMOVE.charValue)
		{
			return new EntityUseCaseResult(EntityUseCaseResultAction.REMOVE, null);
		}

		return null;
	}

	public String executeEntityTransactionUseCaseValidate(
		String serviceName,
		String functionName,
		String jsJsonEntity,
		String id,
		String requestId,
		String requestingUserId,
		String jsJsonPayload
	)
	{
		return (String) appScript.invokeFunction(ScriptConstants.FUNC_ENTITY_TRANSACTION_USECASE_VALIDATE_WITH_PREFIX,
			serviceName,
			functionName,
			jsJsonEntity,
			id,
			requestId,
			requestingUserId,
			jsJsonPayload
		);
	}

	public String executeEntityTransactionUseCaseCommit(
		String serviceName,
		String functionName,
		String jsJsonEntity,
		String id,
		String requestId,
		String requestingUserId,
		String jsJsonPayload
	)
	{
		return (String) appScript.invokeFunction(ScriptConstants.FUNC_ENTITY_TRANSACTION_USECASE_COMMIT_WITH_PREFIX,
			serviceName,
			functionName,
			jsJsonEntity,
			id,
			requestId,
			requestingUserId,
			jsJsonPayload
		);
	}

	public String executeUpdate(
		String serviceName, String jsJsonEntity, String id, long currentTimeMillis, long deltaTime
	)
	{
		return (String) appScript.invokeFunction(ScriptConstants.FUNC_UPDATE_WITH_PREFIX,
			serviceName,
			jsJsonEntity,
			id,
			currentTimeMillis,
			deltaTime
		);
	}

	public void executeProcessGlobals()
	{
		appScript.invokeFunction(ScriptConstants.FUNC_PROCESS_GLOBALS_WITH_PREFIX);
	}
}