package com.arcturus.appserver.system;

public interface InternalUseCases
{
	String GENERATE_LOGIN_DATA = "_gld";
	String SIMPLE_REGISTRATION = "_srg";
	String EMAIL_REGISTRATION = "_erg";
	String FACEBOOK_LOGIN = "_fbl";
	String GOOGLE_LOGIN = "_gol";
	String IS_EMAIL_REGISTERED = "_ier";
	String GET_EMAIL = "_gem";
	String LOGIN = "_log";
	String EMAIL_LOGIN = "_elg";
	String TOKEN_LOGIN = "_tlg";
	String RECONNECT_STATEFUL_SESSION = "_rss";
	String CHANGE_PASSWORD = "_cpw";
	String SEND_PASSWORD_RESET_LINK = "_spr";
	String RESET_PASSWORD = "_rpa";

	String MAINTAINER_REGISTRATION = "_mrg";
	String MAINTAINER_LOGIN = "_mlg";

	String CREATE_APP = "_cra";
	String GET_APPS = "_gap";

	String DEPLOY_APPSCRIPT = "_das";
	String GET_APPSCRIPT = "_gas";

	String GET_SWAGGER_INFO = "_gsi";

	String OPEN_APP_LOG = "_oal";
	String NEW_APP_LOG_MESSAGE = "_nlm";

	String AGGREGATE_ENTITY = "_agg";
}
