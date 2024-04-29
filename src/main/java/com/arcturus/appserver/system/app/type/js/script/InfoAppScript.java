package com.arcturus.appserver.system.app.type.js.script;

import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;
import com.arcturus.appserver.system.Constants;

public class InfoAppScript implements AutoCloseable
{
	private final Logger log;
	private final AppScript appScript;

	InfoAppScript(LoggerFactory loggerFactory, AppScript appScript)
	{
		log = loggerFactory.create(getClass());
		this.appScript = appScript;
	}

	public int currentVersion(String serviceName)
	{
		var value = (Integer) appScript.invokeFunction(ScriptConstants.FUNC_CURRENT_VERSION_WITH_PREFIX,
			serviceName
		);
		if (value == null)
		{
			if (log.isLogLevel(LogLevel.error))
			{
				log.log(LogLevel.error,
					serviceName + ScriptConstants.VAL_CURRENT_VERSION + " is null"
				);
			}
			return -1;
		}
		return value.intValue();
	}

	public String[] entityServices()
	{
		var strServices = (String) appScript.invokeFunction(ScriptConstants.FUNC_ENTITY_SERVICES_WITH_PREFIX);

		if ((strServices == null) || strServices.isEmpty())
		{
			return Constants.EMPTY_STRING_ARRAY;
		}

		return strServices.split(",");
	}

	public String findEntityServiceUseCases(
		String serviceName, boolean findPublic, boolean findCreate
	)
	{
		return (String) appScript.invokeFunction(ScriptConstants.FUNC_FIND_ENTITY_USECASES_WITH_PREFIX,
			serviceName,
			findPublic,
			findCreate
		);
	}

	public String findEntityMappers(String serviceName)
	{
		return (String) appScript.invokeFunction(ScriptConstants.FUNC_FIND_ENTITY_MAPPERS_WITH_PREFIX,
			serviceName
		);
	}

	public String[] findEntityTransactionServiceUseCases(String serviceName)
	{
		var strUseCases = (String) appScript.invokeFunction(ScriptConstants.FUNC_FIND_ENTITY_TRANSACTION_USECASES_WITH_PREFIX,
			serviceName
		);

		if ((strUseCases == null) || strUseCases.isEmpty())
		{
			return Constants.EMPTY_STRING_ARRAY;
		}

		return strUseCases.split(",");
	}

	public String findUseCases(boolean findPublic)
	{
		return (String) appScript.invokeFunction(ScriptConstants.FUNC_FIND_USECASES_WITH_PREFIX,
			findPublic
		);
	}

	public String[] findServicesWithUpdateFunction()
	{
		var strServices = (String) appScript.invokeFunction(ScriptConstants.FUNC_FIND_ENTITY_SERVICES_WITH_UPDATE_WITH_PREFIX);
		return strServices.split(",");
	}

	public String findListServices()
	{
		return (String) appScript.invokeFunction(ScriptConstants.FUNC_FIND_LIST_SERVICES_WITH_PREFIX);
	}

	@Override
	public void close()
	{
		appScript.close();
	}
}