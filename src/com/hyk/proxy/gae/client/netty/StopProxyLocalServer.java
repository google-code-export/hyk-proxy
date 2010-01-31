/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: StopClient.java 
 *
 * @author Administrator [ 2010-1-31 | pm04:13:31 ]
 *
 */
package com.hyk.proxy.gae.client.netty;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import com.hyk.proxy.gae.client.config.Config;

/**
 *
 */
public class StopProxyLocalServer
{

	/**
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException
	{
		Config config = new Config();
		config.loadConfig();
		
		Socket socket = new Socket();
		socket.connect(new InetSocketAddress(config.getLocalServerHost(), config.getLocalServerPort()));
		socket.getOutputStream().write(("OPTIONS " + Config.STOP_COMMAND + " HTTP/1.1\r\n\r\n").getBytes());
		socket.close();
		System.exit(1);
	}

}
