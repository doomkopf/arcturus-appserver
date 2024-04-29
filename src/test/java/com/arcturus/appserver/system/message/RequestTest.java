package com.arcturus.appserver.system.message;

import com.arcturus.appserver.system.Constants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RequestTest
{
	private static final String HEADER = "uc&";
	private static final String JSON_BODY = "{&}";

	private static final String PAYLOAD = HEADER + JSON_BODY;

	@Test
	void testSeparateHeaderAndBody()
	{
		var request = Request.parseFromPayload(PAYLOAD);
		Assertions.assertNotNull(request.getRequestHeader());
		Assertions.assertEquals(JSON_BODY, request.getBody());
	}

	@Test
	void testHeaderWithoutBody()
	{
		var request = Request.parseFromPayload(HEADER);
		Assertions.assertNotNull(request.getRequestHeader());
		Assertions.assertNull(request.getBody());
	}

	@Test
	void testInvalidJsonOnly()
	{
		var request = Request.parseFromPayload(JSON_BODY);
		Assertions.assertNull(request);
	}

	@Test
	void testZeroBytes()
	{
		var payload = new byte[] {0, 0, 0, 0};
		var request = Request.parseFromPayload(new String(payload, Constants.CHARSET_UTF8));
		Assertions.assertNull(request);
	}
}