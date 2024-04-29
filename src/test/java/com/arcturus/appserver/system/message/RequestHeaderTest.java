package com.arcturus.appserver.system.message;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class RequestHeaderTest
{
	private static final String USE_CASE_ID = "theUseCase";
	private static final String APP_ID = "theApp";
	private static final String SERVICE = "theService";
	private static final UUID ENTITY_ID = new UUID(1234, 5678);
	private static final Long SESSION_ID = Long.valueOf(Long.MAX_VALUE / 2);
	private static final String ENCODED_SESSION_ID = Long.toString(SESSION_ID.longValue(), 36);

	private static final String NO_UUID = "noUUID";
	private static final String NO_RADIX_36_ENCODED_NUMBER = "no{Radix}36*Number";

	private static final String VALID_PAYLOAD = USE_CASE_ID
		+ RequestHeader.DELIMITER
		+ APP_ID
		+ RequestHeader.DELIMITER
		+ SERVICE
		+ RequestHeader.DELIMITER
		+ ENTITY_ID
		+ RequestHeader.DELIMITER
		+ ENCODED_SESSION_ID;

	private static final String VALID_PAYLOAD_WITHOUT_SERVICE = USE_CASE_ID
		+ RequestHeader.DELIMITER
		+ APP_ID
		+ RequestHeader.DELIMITER
		+ RequestHeader.DELIMITER
		+ ENTITY_ID
		+ RequestHeader.DELIMITER
		+ ENCODED_SESSION_ID;

	private static final String VALID_PAYLOAD_WITHOUT_SESSION_ID = USE_CASE_ID
		+ RequestHeader.DELIMITER
		+ APP_ID
		+ RequestHeader.DELIMITER
		+ SERVICE
		+ RequestHeader.DELIMITER
		+ ENTITY_ID
		+ RequestHeader.DELIMITER;

	private static final String VALID_PAYLOAD_WITHOUT_ENTITY_ID_AND_SESSION_ID = USE_CASE_ID
		+ RequestHeader.DELIMITER
		+ APP_ID
		+ RequestHeader.DELIMITER
		+ SERVICE
		+ RequestHeader.DELIMITER
		+ RequestHeader.DELIMITER;

	private static final String VALID_PAYLOAD_WITH_ONLY_APP_ID_AND_USE_CASE_ID = USE_CASE_ID
		+ RequestHeader.DELIMITER
		+ APP_ID;

	private static final String VALID_PAYLOAD_WITHOUT_APP_ID = USE_CASE_ID
		+ RequestHeader.DELIMITER
		+ RequestHeader.DELIMITER
		+ SERVICE
		+ RequestHeader.DELIMITER
		+ ENTITY_ID
		+ RequestHeader.DELIMITER
		+ ENCODED_SESSION_ID;

	private static final String INVALID_PAYLOAD_TOO_MANY_ELEMENTS = USE_CASE_ID
		+ RequestHeader.DELIMITER
		+ APP_ID
		+ RequestHeader.DELIMITER
		+ SERVICE
		+ RequestHeader.DELIMITER
		+ ENTITY_ID
		+ RequestHeader.DELIMITER
		+ ENCODED_SESSION_ID
		+ RequestHeader.DELIMITER
		+ " ";

	private static final String INVALID_PAYLOAD_ENTITY_ID_NOT_UUID = USE_CASE_ID
		+ RequestHeader.DELIMITER
		+ APP_ID
		+ RequestHeader.DELIMITER
		+ SERVICE
		+ RequestHeader.DELIMITER
		+ NO_UUID
		+ RequestHeader.DELIMITER
		+ ENCODED_SESSION_ID;

	private static final String INVALID_PAYLOAD_SESSION_ID_NOT_RADIX_36_ENCODED_NUMBER = USE_CASE_ID
		+ RequestHeader.DELIMITER
		+ APP_ID
		+ RequestHeader.DELIMITER
		+ SERVICE
		+ RequestHeader.DELIMITER
		+ ENTITY_ID
		+ RequestHeader.DELIMITER
		+ NO_RADIX_36_ENCODED_NUMBER;

	private static final String INVALID_PAYLOAD_NO_USE_CASE_ID = RequestHeader.DELIMITER
		+ APP_ID
		+ RequestHeader.DELIMITER
		+ SERVICE
		+ RequestHeader.DELIMITER
		+ ENTITY_ID
		+ RequestHeader.DELIMITER
		+ ENCODED_SESSION_ID;

	@Test
	void testParseFromValidRequestPayload()
	{
		var requestHeader = RequestHeader.parse(VALID_PAYLOAD);
		Assertions.assertEquals(USE_CASE_ID, requestHeader.getUseCaseId());
		Assertions.assertEquals(APP_ID, requestHeader.getAppId());
		Assertions.assertEquals(SERVICE, requestHeader.getService());
		Assertions.assertEquals(ENTITY_ID, requestHeader.getEntityId());
		Assertions.assertEquals(SESSION_ID, requestHeader.getSessionId());
	}

	@Test
	void testEmptyValues()
	{
		RequestHeader requestHeader;

		requestHeader = RequestHeader.parse(VALID_PAYLOAD_WITHOUT_SERVICE);
		Assertions.assertEquals(USE_CASE_ID, requestHeader.getUseCaseId());
		Assertions.assertEquals(APP_ID, requestHeader.getAppId());
		Assertions.assertNull(requestHeader.getService());
		Assertions.assertEquals(ENTITY_ID, requestHeader.getEntityId());
		Assertions.assertEquals(SESSION_ID, requestHeader.getSessionId());

		requestHeader = RequestHeader.parse(VALID_PAYLOAD_WITHOUT_SESSION_ID);
		Assertions.assertEquals(USE_CASE_ID, requestHeader.getUseCaseId());
		Assertions.assertEquals(APP_ID, requestHeader.getAppId());
		Assertions.assertEquals(SERVICE, requestHeader.getService());
		Assertions.assertEquals(ENTITY_ID, requestHeader.getEntityId());
		Assertions.assertNull(requestHeader.getSessionId());

		requestHeader = RequestHeader.parse(VALID_PAYLOAD_WITHOUT_APP_ID);
		Assertions.assertEquals(USE_CASE_ID, requestHeader.getUseCaseId());
		Assertions.assertNull(requestHeader.getAppId());
		Assertions.assertEquals(SERVICE, requestHeader.getService());
		Assertions.assertEquals(ENTITY_ID, requestHeader.getEntityId());
		Assertions.assertEquals(SESSION_ID, requestHeader.getSessionId());

		requestHeader = RequestHeader.parse(VALID_PAYLOAD_WITHOUT_ENTITY_ID_AND_SESSION_ID);
		Assertions.assertEquals(USE_CASE_ID, requestHeader.getUseCaseId());
		Assertions.assertEquals(APP_ID, requestHeader.getAppId());
		Assertions.assertEquals(SERVICE, requestHeader.getService());
		Assertions.assertNull(requestHeader.getEntityId());
		Assertions.assertNull(requestHeader.getSessionId());

		requestHeader = RequestHeader.parse(VALID_PAYLOAD_WITH_ONLY_APP_ID_AND_USE_CASE_ID);
		Assertions.assertEquals(USE_CASE_ID, requestHeader.getUseCaseId());
		Assertions.assertEquals(APP_ID, requestHeader.getAppId());
		Assertions.assertNull(requestHeader.getService());
		Assertions.assertNull(requestHeader.getEntityId());
		Assertions.assertNull(requestHeader.getSessionId());
	}

	@Test
	void testReturnNullIfInvalid()
	{
		Assertions.assertNull(RequestHeader.parse(""));
		Assertions.assertNull(RequestHeader.parse(INVALID_PAYLOAD_NO_USE_CASE_ID));
		Assertions.assertNull(RequestHeader.parse(INVALID_PAYLOAD_TOO_MANY_ELEMENTS));
		Assertions.assertNull(RequestHeader.parse(INVALID_PAYLOAD_ENTITY_ID_NOT_UUID));
		Assertions.assertNull(RequestHeader.parse(
			INVALID_PAYLOAD_SESSION_ID_NOT_RADIX_36_ENCODED_NUMBER));
	}
}