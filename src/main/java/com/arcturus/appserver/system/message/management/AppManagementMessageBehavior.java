package com.arcturus.appserver.system.message.management;

import com.arcturus.appserver.system.AppManager;
import com.arcturus.appserver.system.message.AppManagementMessage;

public enum AppManagementMessageBehavior
{
	shutdown
		{
			@Override
			public void execute(AppManagementMessage message, AppManager appManager)
				throws InterruptedException
			{
				appManager.shutdownApp(message.getAppId(), message.getSourceNodeId());
			}
		},
	shutdownConfirmationCallback
		{
			@Override
			public void execute(AppManagementMessage message, AppManager appManager)
			{
				appManager.shutdownAppConfirmationCallback(message.getAppId());
			}
		},
	disableMaintenance
		{
			@Override
			public void execute(AppManagementMessage message, AppManager appManager)
			{
				appManager.disableMaintenance(message.getAppId(), false);
			}
		};

	public abstract void execute(AppManagementMessage message, AppManager appManager)
		throws InterruptedException;
}