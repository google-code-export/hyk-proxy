/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: StartClient.java 
 *
 * @author Administrator [ 2010-1-31 | pm04:33:16 ]
 *
 */
package com.hyk.proxy.gae.client.netty;

import java.io.IOException;
import java.net.UnknownHostException;

/**
 *
 */
public class StartProxyLocalServer
{


	public static void main(String[] args) throws IOException
	{
		new HttpServer().start();
	}

}
