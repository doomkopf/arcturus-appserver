package com.arcturus.appserver.net;

import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;
import com.arcturus.appserver.config.Config;
import com.arcturus.appserver.config.ServerConfigPropery;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

/**
 * Collects statistical data about whether sessions are doing DDOS behavior or
 * not and closes or even bans those sessions based on IP.
 *
 * @author doomkopf
 */
public class DdosManager
{
	private final Logger log;
	private final int ddosMinAllowedMessageFrequencyMillis;
	private final int ddosMessageThreshold;

	private final Cache<String, Boolean> blockedIps;

	DdosManager(
		LoggerFactory loggerFactory,
		int ddosMinAllowedMessageFrequencyMillis,
		int ddosMessageThreshold,
		int ddosIpBlockDurationMinutes
	)
	{
		log = loggerFactory.create(getClass());

		this.ddosMinAllowedMessageFrequencyMillis = ddosMinAllowedMessageFrequencyMillis;
		this.ddosMessageThreshold = ddosMessageThreshold;

		blockedIps = CacheBuilder.newBuilder()
			.expireAfterWrite(ddosIpBlockDurationMinutes, TimeUnit.MINUTES)
			.build();
	}

	@Autowired
	public DdosManager(LoggerFactory loggerFactory, Config config)
	{
		this(loggerFactory,
			config.getInt(ServerConfigPropery.ddosMinAllowedMessageFrequencyMillis),
			config.getInt(ServerConfigPropery.ddosMessageThreshold),
			config.getInt(ServerConfigPropery.ddosIpBlockDurationMinutes)
		);
	}

	boolean checkAndHandleDDOS(PersistentLocalSession localSession, long now)
	{
		if (blockedIps.getIfPresent(localSession.getIp()) != null)
		{
			return true;
		}

		var stats = localSession.getStats();
		if ((now - stats.getDdosLastMessage()) < ddosMinAllowedMessageFrequencyMillis)
		{
			stats.ddosHighFrequencyMessageCount.increment();
			if (stats.ddosHighFrequencyMessageCount.sum() >= ddosMessageThreshold)
			{
				blockedIps.put(localSession.getIp(), Boolean.TRUE);
				log.log(LogLevel.warn,
					"Blocked potential DDOS session. IP: " + localSession.getIp()
				);
				return true;
			}
		}
		else
		{
			stats.ddosHighFrequencyMessageCount.reset();
		}

		stats.setDdosLastMessage(now);

		return false;
	}

	public boolean checkAndHandleDDOS(PersistentLocalSession localSession)
	{
		return checkAndHandleDDOS(localSession, System.currentTimeMillis());
	}
}