package com.arcturus.appserver.system;

import java.nio.charset.Charset;
import java.time.ZoneId;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

public interface Constants // NOSONAR
{
	String HTTP_PATH_HTTP_API = "arcapi";
	String HTTP_PATH_WEBSOCKET_API = "arcwsapi";

	String JSONKEY_USECASE = "uc";

	int SLOW_TASK_THRESHOLD_MILLIS_DEFAULT = 100;
	int APP_SHUTDOWN_WAIT_FOR_NODE_CALLBACKS_TIMEOUT_MILLIS = 10000;

	UUID ZERO_UUID = new UUID(0, 0);
	String[] EMPTY_STRING_ARRAY = new String[0];

	Charset CHARSET_UTF8 = Charset.forName("UTF-8");

	Locale DEFAULT_LOCALE = Locale.ENGLISH;

	long DAY_MILLIS = 24 * 3600 * 1000;

	ZoneId GMT = ZoneId.of("GMT");

	// Half a year
	long LOGIN_TOKEN_VALIDITY_DURATION_MILLIS = 182 * 24 * 3600 * 1000L;

	int MAX_EMAIL_LENGTH = 128;

	Pattern EMAIL_REGEX = Pattern.compile(
		"^[a-zA-Z0-9&+_-]+(\\.[a-zA-Z0-9&+_-]+)*@((?!-)[a-zA-Z0-9-]*[a-zA-Z0-9]+\\.)+[a-zA-Z]{2,}$");
}