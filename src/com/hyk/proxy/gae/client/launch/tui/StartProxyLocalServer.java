/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: StartClient.java 
 *
 * @author yinqiwen [ 2010-1-31 | 04:33:16 PM ]
 *
 */
package com.hyk.proxy.gae.client.launch.tui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.gae.client.httpserver.HttpServer;

/**
 *
 */
public class StartProxyLocalServer
{
	protected static Logger				logger			= LoggerFactory.getLogger(StartProxyLocalServer.class);

	public static void main(String[] args)
	{
		try 
		{
			System.out.println(new HttpServer().start());
		} 
		catch (Exception e) 
		{
			logger.error("Failed to start local server.", e);
			System.exit(-1);
		}
		
	}

}
