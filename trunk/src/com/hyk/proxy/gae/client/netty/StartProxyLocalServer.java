/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: StartClient.java 
 *
 * @author yinqiwen [ 2010-1-31 | 04:33:16 PM ]
 *
 */
package com.hyk.proxy.gae.client.netty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
			new HttpServer().start();
		} 
		catch (Exception e) 
		{
			logger.error("Failed to start local server.", e);
			System.exit(-1);
		}
		
	}

}
