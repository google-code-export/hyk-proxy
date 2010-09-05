/**
 * This file is part of the hyk-proxy-framework project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: Updater.java 
 *
 * @author yinqiwen [ 2010-8-20 | 08:49:29 PM ]
 *
 */
package com.hyk.proxy.framework.update;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.Proxy.Type;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.framework.Framework;
import com.hyk.proxy.framework.appdata.AppData;
import com.hyk.proxy.framework.common.Constants;
import com.hyk.proxy.framework.common.Misc;
import com.hyk.proxy.framework.config.Config;
import com.hyk.proxy.framework.launch.Upgrade;
import com.hyk.proxy.framework.plugin.PluginManager;
import com.hyk.proxy.framework.plugin.PluginManager.InstalledPlugin;
import com.hyk.proxy.framework.trace.Trace;
import com.hyk.proxy.framework.update.ProductReleaseDetail.FrameworkReleaseDetail;
import com.hyk.proxy.framework.update.ProductReleaseDetail.PluginReleaseDetail;
import com.hyk.proxy.framework.update.ProductUpdateDetail.CommonUpgradeDetail;
import com.hyk.proxy.framework.update.ProductUpdateDetail.UpgradeFileset;
import com.hyk.proxy.framework.util.CommonUtil;
import com.hyk.proxy.framework.util.SimpleSocketAddress;
import com.hyk.util.net.NetUtil;

/**
 *
 */
public class Updater
{
	private static final long INITIAL_DELAY = 10 ; // 1s
	private static final long FIX_RATE = 3600; // 1h

	private static final int FRAMEWORK = 0;
	private static final int GLOBAL_PLUGIN = 1;
	private static final int USER_PLUGIN = 2;

	protected Logger logger = LoggerFactory.getLogger(getClass());
	private ScheduledExecutorService timer = new ScheduledThreadPoolExecutor(2);
	private Trace trace;
	private Framework fm;

	public Updater(Framework fm)
	{

		timer.scheduleAtFixedRate(new UpdateTask(), INITIAL_DELAY, FIX_RATE,
		        TimeUnit.SECONDS);
		this.trace = Misc.getTrace();
		this.fm = fm;
	}

	private File downloadUpdates(String url)
	{
		return CommonUtil.downloadFile(url, AppData.getUserUpdateHome());
	}

	private void appendUpgradeDetail(String zipName, UpgradeFileset fileset,
	        int type, String version)
	{
		switch (fileset.action)
		{
			case ADD:
			case REPLACE:
			{
				for (String file : fileset.fileset)
				{
					if (type == GLOBAL_PLUGIN)
					{
						file = "plugins/" + file;
					}
					else if (type == USER_PLUGIN)
					{
						file = ".plugins/" + file;
					}
					Upgrade.appendReplaceDetail(zipName, file);
				}
				break;
			}
			case REMOVE:
			{
				for (String file : fileset.fileset)
				{
					if (type == GLOBAL_PLUGIN)
					{
						file = "plugins/" + file;
					}
					else if (type == USER_PLUGIN)
					{
						file = ".plugins/" + file;
					}
					Upgrade.appendRemoveDetail(file);
				}
				break;
			}
			default:
				break;
		}
	}

	public void doUpdateCheck()
	{
		UpdateCheck check = new UpdateCheck();
		UpdateCheckResults result = check.checkForUpdates();
		if (null != result)
		{
			FrameworkReleaseDetail newFm = result.getNewerFrameworkRelease();
			List<PluginReleaseDetail> newPlugins = result
			        .getNewerPluginReleases();
			if(logger.isDebugEnabled())
			{
				logger.debug("Found updates for framework:" + newFm);
			}
			if(logger.isDebugEnabled())
			{
				logger.debug("Found updates for plugins:" + newPlugins);
			}
			if (null != newFm || !newPlugins.isEmpty())
			{
				trace.notice("Found updates.");
				if (null != newFm)
				{
					File zipFile = downloadUpdates(newFm.url);
					if (null != zipFile)
					{
						CommonUpgradeDetail frameworkUpgradeDetail = result
						        .getFrameworkUpdateDetail();
						if (null != frameworkUpgradeDetail)
						{
							if(logger.isDebugEnabled())
							{
								logger.debug("" + frameworkUpgradeDetail);
							}
							String zipName = zipFile.getName();
							for (UpgradeFileset fileset : frameworkUpgradeDetail.filesets)
							{
								appendUpgradeDetail(zipName, fileset, FRAMEWORK, newFm.version);
							}
						}
						else
						{
							logger.error("No update detail found for framework!");
						}
					}
				}
				for (PluginReleaseDetail np : newPlugins)
				{
					PluginManager pm = PluginManager.getInstance();
					InstalledPlugin p = pm.getInstalledPlugin(np.name);
					File zipFile = downloadUpdates(np.url);
					if (null != zipFile)
					{
						CommonUpgradeDetail pluginUpgradeDetail = result
						        .getPluginUpdateDetail(np);
						if(logger.isDebugEnabled())
						{
							logger.debug("" + pluginUpgradeDetail);
						}
						String zipName = zipFile.getName();
						for (UpgradeFileset fileset : pluginUpgradeDetail.filesets)
						{
							
							if (p.isGlobal)
							{
								appendUpgradeDetail(zipName, fileset,
								        GLOBAL_PLUGIN, np.version);
							}
							else
							{
								appendUpgradeDetail(zipName, fileset,
								        USER_PLUGIN, np.version);
							}
						}
					}
				}
				try
				{
					Upgrade.flushUpgradeDetails();
				}
				catch (IOException e)
				{
					logger.error("Failed to write upgrade info.", e);
				}
			}
		}
	}

	class UpdateTask implements Runnable
	{
		@Override
		public void run()
		{
			if(logger.isDebugEnabled())
			{
				logger.debug("Start update check.");
			}
			try
            {
				doUpdateCheck();
            }
            catch (Exception e)
            {
            	logger.error("Failed to do update check.", e);
            }
			
		}
	}
}
