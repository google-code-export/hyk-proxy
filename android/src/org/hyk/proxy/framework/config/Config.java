/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: Config.java 
 *
 * @author yinqiwen [ 2010-5-14 | 08:49:33 PM]
 *
 */
package org.hyk.proxy.framework.config;

import org.hyk.proxy.framework.appdata.AppData;
import org.hyk.proxy.framework.common.Constants;
import org.hyk.proxy.framework.util.SimpleSocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */

public class Config
{
	protected static Logger logger = LoggerFactory.getLogger(Config.class);

	private static Config instance = null;

	static
	{
		try
		{

			instance.init();
		}
		catch (Exception e)
		{
			logger.error("Failed to load default config file!", e);
		}
	}

	private SimpleSocketAddress localProxyServerAddress = new SimpleSocketAddress(
	        "localhost", 48100);

	private int threadPoolSize = 30;

	public void setThreadPoolSize(int threadPoolSize)
	{
		this.threadPoolSize = threadPoolSize;
	}

	private String proxyEventServiceFactory = "GAE";

	public String getProxyEventServiceFactory()
	{
		return proxyEventServiceFactory;
	}

	public void setProxyEventServiceFactory(String proxyEventServiceFactory)
	{
		this.proxyEventServiceFactory = proxyEventServiceFactory;
	}

	public void init() throws Exception
	{

	}

	public SimpleSocketAddress getLocalProxyServerAddress()
	{
		return localProxyServerAddress;
	}

	public int getThreadPoolSize()
	{
		return threadPoolSize;
	}

	private Config()
	{
		// nothing
	}

	public static Config loadConfig()
	{
		return ConfigService.getFrameworkConfig();
	}

	public void saveConfig()
	{
		ConfigService.saveFrameworkConfig();
	}
}
