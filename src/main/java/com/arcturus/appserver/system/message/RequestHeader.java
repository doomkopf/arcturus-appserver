package com.arcturus.appserver.system.message;

import com.arcturus.appserver.system.Tools;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * The header that each request from a client must contain.
 *
 * @author doomkopf
 */
public final class RequestHeader
{
	static final char DELIMITER = '&';

	private static final int HEADER_ELEMENTS = 5;
	private static final String HEADER_SPLIT_REGEX = String.valueOf(DELIMITER);

	public static RequestHeader parse(String headerString)
	{
		if ((headerString == null) || headerString.isEmpty())
		{
			return null;
		}

		var headerElements = headerString.split(HEADER_SPLIT_REGEX);
		if (headerElements.length > HEADER_ELEMENTS)
		{
			return null;
		}

		var useCaseId = parseStringElement(headerElements, 0, HEADER_ELEMENTS - 4);
		if (useCaseId == null)
		{
			return null;
		}

		var appId = parseStringElement(headerElements, 1, HEADER_ELEMENTS - 3);

		var service = parseStringElement(headerElements, 2, HEADER_ELEMENTS - 2);

		var strEntityId = parseStringElement(headerElements, 3, HEADER_ELEMENTS - 1);
		UUID entityId = null;
		if (strEntityId != null)
		{
			entityId = parseUuid(strEntityId);
			if (entityId == null)
			{
				return null;
			}
		}

		var strSessionId = parseStringElement(headerElements, 4, HEADER_ELEMENTS);
		Long sessionId = null;
		if (strSessionId != null)
		{
			sessionId = Tools.parseLongFromRadix36EncodedString(strSessionId);
			if (sessionId == null)
			{
				return null;
			}
		}

		return new RequestHeader(useCaseId, appId, service, entityId, sessionId);
	}

	static RequestHeader parseFromPathAndQueryString(String path, String queryString)
	{
		var pathElements = path.split("/");
		if (pathElements.length < 3)
		{
			return null;
		}

		String service = null;
		UUID entityId = null;
		Long sessionId = null;

		if (queryString != null)
		{
			queryString = queryString.trim();
			if (!queryString.isEmpty())
			{
				var queryKeyValues = queryString.split("&");
				for (var queryKeyValueString : queryKeyValues)
				{
					var queryKeyValue = queryKeyValueString.split("=");
					if (queryKeyValue.length > 1)
					{
						if ((service == null) && "service".equalsIgnoreCase(queryKeyValue[0]))
						{
							service = queryKeyValue[1];
						}
						else if ((entityId == null)
							&& "entityid".equalsIgnoreCase(queryKeyValue[0]))
						{
							entityId = UUID.fromString(queryKeyValue[1]);
						}
						else if ((sessionId == null)
							&& "sessionid".equalsIgnoreCase(queryKeyValue[0]))
						{
							sessionId = Tools.parseLongFromRadix36EncodedString(queryKeyValue[1]);
						}
					}
				}
			}
		}

		return new RequestHeader(pathElements[2], pathElements[1], service, entityId, sessionId);
	}

	private static String parseStringElement(
		String[] headerElements, int index, int minRequiredLength
	)
	{
		String strId = null;
		if (headerElements.length >= minRequiredLength)
		{
			strId = headerElements[index];
		}

		if (StringUtils.isEmpty(strId))
		{
			return null;
		}

		return strId;
	}

	private static UUID parseUuid(String str)
	{
		try
		{
			return UUID.fromString(str);
		}
		catch (IllegalArgumentException e)
		{
			return null;
		}
	}

	private final String useCaseId;
	private final String appId;
	private final String service;
	private final UUID entityId;
	private final Long sessionId;

	private RequestHeader(
		String useCaseId, String appId, String service, UUID entityId, Long sessionId
	)
	{
		this.useCaseId = useCaseId;
		this.appId = appId;
		this.service = service;
		this.entityId = entityId;
		this.sessionId = sessionId;
	}

	public String getUseCaseId()
	{
		return useCaseId;
	}

	public String getAppId()
	{
		return appId;
	}

	public String getService()
	{
		return service;
	}

	public UUID getEntityId()
	{
		return entityId;
	}

	public Long getSessionId()
	{
		return sessionId;
	}
}
