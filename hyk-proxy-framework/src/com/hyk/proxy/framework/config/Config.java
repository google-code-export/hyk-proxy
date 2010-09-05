/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: Config.java 
 *
 * @author yinqiwen [ 2010-5-14 | 08:49:33 PM]
 *
 */
package com.hyk.proxy.framework.config;


import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.framework.appdata.AppData;
import com.hyk.proxy.framework.common.Constants;
import com.hyk.proxy.framework.util.SimpleSocketAddress;




/**
 *
 */
@XmlRootElement(name = "Configure")
public class Config
{
	protected static Logger logger = LoggerFactory.getLogger(Config.class);

	private static Config instance = null;

	static
	{
		try
		{
			JAXBContext context = JAXBContext.newInstance(Config.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			if(AppData.getUserFrameworkConf().exists())
			{
				instance = (Config) unmarshaller.unmarshal(AppData.getUserFrameworkConf());
			}
			else
			{
				instance = (Config) unmarshaller.unmarshal(Config.class
				        .getResource("/" + Constants.CONF_FILE));
			}
			
			instance.init();
		}
		catch (Exception e)
		{
			logger.error("Failed to load default config file!", e);
		}
	}

	@XmlElement(name = "localserver")
	private SimpleSocketAddress localProxyServerAddress = new SimpleSocketAddress(
	        "localhost", 48100);

	private int threadPoolSize = 30;

	@XmlElement
	public void setThreadPoolSize(int threadPoolSize)
	{
		this.threadPoolSize = threadPoolSize;
	}

	private String proxyEventServiceFactory = "GAE";

	public String getProxyEventServiceFactory()
	{
		return proxyEventServiceFactory;
	}

	@XmlElement
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
