/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: StopClient.java 
 *
 * @author yinqiwen [ 2010-1-31 | pm04:13:31 ]
 *
 */
package com.hyk.proxy.client.launch.tui;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

import com.hyk.proxy.client.config.Config;
import com.hyk.proxy.client.config.Config.SimpleSocketAddress;
import com.hyk.proxy.common.Constants;

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
		Config config = Config.getInstance();
		
		DatagramSocket socket = new DatagramSocket();
		SimpleSocketAddress addr = config.getLocalProxyServerAddress();
		byte[] data = Constants.STOP_CLIENT_COMMAND.getBytes();
		DatagramPacket packet = new DatagramPacket(data, data.length, new InetSocketAddress(addr.host, addr.port));
		socket.send(packet);
		socket.close();
		System.exit(1);
	}

}
