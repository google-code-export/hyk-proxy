/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: SeattleApplicationConfig.java 
 *
 * @author yinqiwen [ 2010-5-22 | 10:53:57 AM ]
 *
 */
package com.hyk.proxy.plugin.phptunnel.config;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class PhpTunnelApplicationConfig
{
	static Logger		logger	= LoggerFactory.getLogger(PhpTunnelApplicationConfig.class);
	static List<URL> serverAddrs;
	static int tunnelPort = -1;
	private static void loadConfig()
	{
		try
		{
			serverAddrs = new LinkedList<URL>();
			BufferedReader reader  = new BufferedReader(new InputStreamReader(PhpTunnelApplicationConfig.class.getResourceAsStream("/phptunnel.conf")));
			String line = reader.readLine();
			while(line != null)
			{
				line = line.trim();
				if(!line.startsWith("#") && !line.isEmpty())
				{
					if(line.startsWith("LocalTunnelPort"))
					{
						String portStr = line.substring(line.indexOf("=")+1).trim();
						tunnelPort = Integer.parseInt(portStr);
					}
					else
					{
						serverAddrs.add(new URL(line.trim()));
					}
				}
				line = reader.readLine();
			}
		}
		catch(Exception e)
		{
			logger.error("Failed to load application-phptunnel's config.", e);
			serverAddrs = null;
		}
	}
	
	public static List<URL> getPhpTunnelServerAddress()
	{
		if(null == serverAddrs)
		{
			loadConfig();
		}
		return serverAddrs;
	}
	
	public static int gettunnelPort()
	{
		if(tunnelPort == -1)
		{
			loadConfig();
		}
		return tunnelPort;
	}
}
