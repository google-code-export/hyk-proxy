/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: GoogleAppEngineApplicationConfig.java 
 *
 * @author yinqiwen [ 2010-5-22 | 10:53:00 AM ]
 *
 */
package com.hyk.proxy.client.application.gae;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.common.Version;

/**
 *
 */
public class GoogleAppEngineApplicationConfig
{
	static Logger		logger	= LoggerFactory.getLogger(GoogleAppEngineApplicationConfig.class);
	static Properties	config	= new Properties();

	static
	{
		try
		{
			config.load(GoogleAppEngineApplicationConfig.class.getResourceAsStream("/app.gae.conf"));
		}
		catch(IOException e)
		{
			logger.error("Failed to load application-GAE's config.", e);
		}
	}
	
	public static String getSimulateUserAgent()
	{
		String defaultUserAgent = "hyk-proxy-client V" + Version.value;  
		String selection = config.getProperty("UserAgent");
		if(null == selection)
		{
			return defaultUserAgent;
		}
		String ret = config.getProperty(selection);
		if(null == ret)
		{
			return defaultUserAgent;
		}
		return ret;
	}
}
