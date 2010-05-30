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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.client.framework.status.StatusMonitor;
import com.hyk.proxy.client.launch.LocalProxyServer;
import com.hyk.proxy.common.ExtensionsLauncher;
import com.hyk.proxy.common.update.UpdateCheckResults;
import com.hyk.proxy.common.update.ProductReleaseDetail.Link;
import com.hyk.proxy.common.update.ProductReleaseDetail.ReleaseDetail;
import com.hyk.proxy.common.update.UpdateCheck.UpdateCheckFactory;

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
			
		}
		catch(Exception e)
		{
			logger.error("Failed to start local server.", e);
			System.exit(-1);
		}
		
		if(null != UpdateCheckFactory.getUpdateChecker())
		{
			UpdateCheckResults result = UpdateCheckFactory.getUpdateChecker().checkForUpdates();
			if(null != result)
			{
				ReleaseDetail detail = result.getNewerRelease();
				if(null != detail)
				{
					String newVersion = detail.version;
			        List<Link> links = detail.links;
			        String notice = String.format(UpdateCheckResults.NOTICE_FORMATTER, newVersion, "", "");
			        System.out.println(notice);
			        System.out.println(UpdateCheckResults.NOTICE_DOWNLOAD);
			        for (Link link : links) 
			        {
			        	System.out.println(link.type + " " +  link.link);
			        }
				}
			}
		}

	}

}
