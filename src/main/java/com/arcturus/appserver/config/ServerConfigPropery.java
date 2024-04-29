package com.arcturus.appserver.config;

import com.arcturus.api.LogLevel;

public enum ServerConfigPropery implements ConfigProperty
{
	logger(LoggerType.sysout),
	keyValueStoreDatabaseType(KeyValueStoreDatabaseType.file),
	httpSessionServiceType(HttpSessionServiceType.jetty),
	fileDatabasePath("data/"),
	fileDatabaseExtension("json"),
	fileDatabaseThreads(4),
	appsPath("apps"),
	javaAppConfigs(""),
	requestIdCachesEvictionSeconds(60),
	userSessionCachesEvictionSeconds(3600),
	entityInMemoryEvictionSeconds(60 * 10),

	nanoProcessScheduledIntervalMillis(400),

	httpPort(8080),
	webSocketIdleTimeoutMillis(600000),
	ddosMinAllowedMessageFrequencyMillis(100),
	ddosMessageThreshold(8),
	ddosIpBlockDurationMinutes(60),

	hazelcastMemberSenderThreads(1),
	hazelcastPort(0),
	hazelcastMembers(""),
	hazelcastGroupName("arcturus"),
	hazelcastMaxUsedHeapSpacePercentage(80),

	couchbaseNodes("127.0.0.1"),
	couchbaseBucket("default"),
	couchbaseBucketPassword(""),

	awsCloudwatchLogsAccessKey(""),
	awsCloudwatchLogsSecretKey(""),
	awsCloudwatchLogsRegion(""),
	awsCloudwatchLogsLogGroupName(""),
	awsCloudwatchLogsTransferDelayMillis(10000),
	awsCloudwatchLogsLogLevel(LogLevel.debug),

	awsDynamoDbAccessKey(""),
	awsDynamoDbSecretKey(""),
	awsDynamoDbRegion(""),
	awsDynamoDbTableName(""),
	awsDynamoDbKeyName("arckey"),
	awsDynamoDbValueName("v"),

	awsSesAccessKey(""),
	awsSesSecretKey(""),
	awsSesRegion("");

	private final String defaultValue;

	ServerConfigPropery(String defaultValue)
	{
		this.defaultValue = defaultValue;
	}

	ServerConfigPropery(int defaultValue)
	{
		this(String.valueOf(defaultValue));
	}

	ServerConfigPropery(boolean defaultValue)
	{
		this(String.valueOf(defaultValue));
	}

	ServerConfigPropery(Enum<?> defaultValue)
	{
		this(defaultValue.name());
	}

	@Override
	public String defaultValue()
	{
		return defaultValue;
	}
}