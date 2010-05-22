/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: MixApplicationConfig.java 
 *
 * @author yinqiwen [ 2010-5-22 | 11:38:31 AM ]
 *
 */
package com.hyk.proxy.client.application.mix;

import java.io.IOException;
import java.util.Properties;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.client.application.seattle.event.SeattleProxyEventServiceFactory;
import com.hyk.proxy.client.framework.event.HttpProxyEventType;


/**
 *
 */
public class MixApplicationConfig
{
	static Logger		logger	= LoggerFactory.getLogger(MixApplicationConfig.class);
	static Properties	config	= new Properties();
	static String[] delegateURLPattern;
	static HttpProxyEventType[] delegateEvent;
	static String[] delegateMethods;
	
	static
	{
		try
		{
			config.load(MixApplicationConfig.class.getResourceAsStream("/app.mix.conf"));
		}
		catch(IOException e)
		{
			logger.error("Failed to load application-GAE's config.", e);
		}
	}
	
	public static String getDelegateEventServiceFactoryClass()
	{
		return config.getProperty("delegate.event.factory", SeattleProxyEventServiceFactory.class.getName());
	}
	
	public static String[] getDelegateURLPattern()
	{
		if(null == delegateURLPattern)
		{
			String value = config.getProperty("delegate.url", "").trim();
			if(!value.isEmpty())
			{
				delegateURLPattern = value.split(",");
			}
			else
			{
				delegateURLPattern = new String[0];
			}
		}
		return delegateURLPattern;
	}
	
	public static String[] getDelegateMethods()
	{
		if(null == delegateMethods)
		{
			String value = config.getProperty("delegate.method", "").trim();
			if(!value.isEmpty())
			{
				delegateMethods = value.split(",");
			}
			else
			{
				delegateMethods = new String[0];
			}
		}
		return delegateMethods;
	}
	
	public static HttpProxyEventType[] getHttpProxyEventTypes()
	{
		if(null == delegateEvent)
		{
			String value = config.getProperty("delegate.event", "").trim();
			if(!value.isEmpty())
			{
				String[] values = value.split(",");
				delegateEvent = new HttpProxyEventType[values.length];
				for(int i = 0; i<values.length; i++)
				{
					delegateEvent[i] = Enum.valueOf(HttpProxyEventType.class, values[i]);
				}
			}
			else
			{
				delegateEvent = new HttpProxyEventType[0];
			}
		}
		return delegateEvent;
	}
}
