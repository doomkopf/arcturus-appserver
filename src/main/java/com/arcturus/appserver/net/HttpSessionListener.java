package com.arcturus.appserver.net;

/**
 * For listening to HTTP requests.
 *
 * @author doomkopf
 */
public interface HttpSessionListener
{
	void onReceived(RequestContext requestContext, String payload);

	void onReceived(
		RequestContext requestContext,
		HttpHeaders httpHeaders,
		HttpMethod method,
		String path,
		String queryString,
		String requestBody
	);
}