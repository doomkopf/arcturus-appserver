package com.arcturus.appserver.system.app.service;

import com.arcturus.appserver.system.Message;

/**
 * A service that is definitely running locally on this VM.
 * 
 * @author doomkopf
 */
public interface LocalService
{
	void send(Message msg);
}