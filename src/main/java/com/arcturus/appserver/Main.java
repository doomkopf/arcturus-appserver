package com.arcturus.appserver;

import com.arcturus.appserver.api.Startup;

/**
 * Entry point for the non-embedded version of arcturus-appserver.
 *
 * @author doomkopf
 */
public enum Main
{
	;

	public static void main(String[] args)
	{
		Startup.start();
	}
}