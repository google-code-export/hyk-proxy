/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: StartClient.java 
 *
 * @author yinqiwen [ 2010-1-31 | 04:33:16 PM ]
 *
 */
package com.hyk.proxy.client.launch.tui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.client.framework.status.StatusMonitor;
import com.hyk.proxy.client.launch.LocalProxyServer;
import com.hyk.proxy.common.ExtensionsLauncher;
import com.hyk.proxy.common.update.ProductReleaseDetail.ReleaseDetail;

/**
 *
 */
public class StartProxyLocalServer
{
	protected static Logger	logger	= LoggerFactory.getLogger(StartProxyLocalServer.class);

	public static void main(String[] args)
	{
		try
		{
			ExtensionsLauncher.init();
			LocalProxyServer localProxyServer = new LocalProxyServer();
			localProxyServer.launch(new StatusMonitor()
			{
				@Override
				public void notifyStatus(String status)
				{
					System.out.println(status);
				}

				@Override
				public void clearStatusHistory()
				{
				}

				@Override
				public void notifyRunDetail(String detail)
				{
					System.out.println(detail);
					
				}
			});
			// ReleaseDetail detail =
			// localProxyServer.checkForUpdates().getNewerRelease();
			// if(null != detail)
			// {
			//				
			// }
		}
		catch(Exception e)
		{
			logger.error("Failed to start local server.", e);
			System.exit(-1);
		}

	}

}
