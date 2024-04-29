package com.arcturus.appserver.net;

import com.arcturus.api.service.RequestInfo;

/**
 * The context of a single request in order to send back a response.
 *
 * @author doomkopf
 */
public interface RequestContext extends RequestInfo
{
	void respond(String jsonString);

	String getIp();
}