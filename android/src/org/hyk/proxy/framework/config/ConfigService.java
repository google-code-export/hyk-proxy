/**
 * This file is part of the hyk-proxy-framework project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: ConfigService.java 
 *
 * @author yinqiwen [ 2010-8-29 | 03:10:31 PM ]
 *
 */
package org.hyk.proxy.framework.config;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class ConfigService
{
	protected static Logger logger = LoggerFactory.getLogger(ConfigService.class);
	private static Config frameworkCfg = null;
	private static Map<String, Object> pluginCfgs = new HashMap<String, Object>();
	
	public static Config getFrameworkConfig()
	{
		if(null == frameworkCfg)
		{
		
		}
		return frameworkCfg;
	}
	
	public static void saveFrameworkConfig()
	{
		try
		{
			
		}
		catch (Exception e)
		{
			logger.error("Failed to store framework config file!", e);
		}
	}
	
	public static <T> T getXmlPluginConfig(Class<T> type, String pluginName, String cfgName)
	{
		return null;
	}
	
	public static void saveXmlPluginCfg(Object cfg, String pluginName, String cfgName)
	{
		
	}
	
	public static InputStream getPluginConfigInputStream(String pluginName, String cfgName)
	{
		return null;
	}
	
	
}
