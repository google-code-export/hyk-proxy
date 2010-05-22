/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: SeattleApplicationConfig.java 
 *
 * @author yinqiwen [ 2010-5-22 | 10:53:57 AM ]
 *
 */
package com.hyk.proxy.client.application.seattle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.client.application.gae.GoogleAppEngineApplicationConfig;
import com.hyk.proxy.client.config.Config.SimpleSocketAddress;

/**
 *
 */
public class SeattleApplicationConfig
{
	static Logger		logger	= LoggerFactory.getLogger(SeattleApplicationConfig.class);
	static List<SimpleSocketAddress> serverAddrs;
	private static void loadConfig()
	{
		try
		{
			serverAddrs = new LinkedList<SimpleSocketAddress>();
			BufferedReader reader  = new BufferedReader(new InputStreamReader(SeattleApplicationConfig.class.getResourceAsStream("/app.seattle.conf")));
			String line = reader.readLine();
			while(line != null)
			{
				line = line.trim();
				if(!line.startsWith("#"))
				{
					int index = line.indexOf(":");
					if(index > 0)
					{
						String host = line.substring(0, index).trim();
						int port = Integer.parseInt(line.substring(index + 1).trim());
						SimpleSocketAddress addr = new SimpleSocketAddress();
						addr.host = host;
						addr.port = port;
						serverAddrs.add(addr);
					}
				}
				line = reader.readLine();
			}
		}
		catch(Exception e)
		{
			logger.error("Failed to load application-GAE's config.", e);
			serverAddrs = null;
		}
	}
	
	public static List<SimpleSocketAddress> getSeattleServerAddress()
	{
		if(null == serverAddrs)
		{
			loadConfig();
		}
		return serverAddrs;
	}
}
