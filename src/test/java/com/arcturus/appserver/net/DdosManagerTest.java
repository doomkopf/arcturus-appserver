package com.arcturus.appserver.net;

import com.arcturus.api.LogLevel;
import com.arcturus.api.Logger;
import com.arcturus.api.LoggerFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DdosManagerTest
{
	private static class NoOpLogger implements Logger
	{
		@Override
		public boolean isLogLevel(LogLevel logLevel)
		{
			return false;
		}

		@Override
		public String getName()
		{
			return null;
		}

		@Override
		public void log(LogLevel logLevel, String s)
		{

		}

		@Override
		public void log(LogLevel logLevel, String s, Object... objects)
		{

		}

		@Override
		public void log(LogLevel logLevel, Throwable throwable)
		{

		}

		@Override
		public void log(LogLevel logLevel, String s, Throwable throwable)
		{

		}
	}

	private static class TestLoggerFactory implements LoggerFactory
	{
		@Override
		public Logger create(Class<?> aClass)
		{
			return new NoOpLogger();
		}

		@Override
		public Logger create(String s)
		{
			return new NoOpLogger();
		}
	}

	private static class TestSession implements PersistentLocalSession
	{
		private final PersistentLocalSessionStats stats;

		TestSession(PersistentLocalSessionStats stats)
		{
			this.stats = stats;
		}

		@Override
		public void send(String payload)
		{
		}

		@Override
		public void close()
		{
		}

		@Override
		public boolean isOpen()
		{
			return false;
		}

		@Override
		public String getIp()
		{
			return "";
		}

		@Override
		public PersistentLocalSessionStats getStats()
		{
			return stats;
		}

		@Override
		public PersistentLocalSessionInfo getInfo()
		{
			return null;
		}

		@Override
		public void setInfo(PersistentLocalSessionInfo info)
		{
		}

		@Override
		public void respond(String jsonString)
		{
		}
	}

	@Test
	void shouldNotBeDdosWhenLowFrequencyIsNotInARow()
	{
		var ddos = new DdosManager(new TestLoggerFactory(), 100, 4, 1);
		var stats = new PersistentLocalSessionStats();

		Assertions.assertFalse(ddos.checkAndHandleDDOS(new TestSession(stats), 100));
		Assertions.assertFalse(ddos.checkAndHandleDDOS(new TestSession(stats), 101));
		Assertions.assertFalse(ddos.checkAndHandleDDOS(new TestSession(stats), 300));
		Assertions.assertFalse(ddos.checkAndHandleDDOS(new TestSession(stats), 301));
		Assertions.assertFalse(ddos.checkAndHandleDDOS(new TestSession(stats), 302));
	}

	@Test
	void shouldBeDdosWhenLowFrequencyIsInARow()
	{
		var ddos = new DdosManager(new TestLoggerFactory(), 100, 4, 1);
		var stats = new PersistentLocalSessionStats();

		Assertions.assertFalse(ddos.checkAndHandleDDOS(new TestSession(stats), 100));
		Assertions.assertFalse(ddos.checkAndHandleDDOS(new TestSession(stats), 101));
		Assertions.assertFalse(ddos.checkAndHandleDDOS(new TestSession(stats), 102));
		Assertions.assertFalse(ddos.checkAndHandleDDOS(new TestSession(stats), 103));
		Assertions.assertTrue(ddos.checkAndHandleDDOS(new TestSession(stats), 104));
	}
}