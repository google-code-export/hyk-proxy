/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: UDPCommandServer.java 
 *
 * @author qiying.wang [ May 17, 2010 | 11:32:56 AM ]
 *
 */
package com.hyk.proxy.client.framework.mx;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.client.config.Config.SimpleSocketAddress;
import com.hyk.proxy.common.Constants;

/**
 *
 */
public class UDPCommandServer implements Runnable
{
	protected Logger	logger	= LoggerFactory.getLogger(getClass());
	private List<ManageResource> resources = new ArrayList<ManageResource>();
	private DatagramSocket udpSock;
	private byte[] buffer = new byte[65536];
	private boolean running = true;
	private SimpleSocketAddress address;
	
	public UDPCommandServer(SimpleSocketAddress address) throws SocketException, UnknownHostException
	{
		this.address = address;
		udpSock = new DatagramSocket(address.port, InetAddress.getByName(address.host));
		//udpSock.bind(new InetSocketAddress(address.host, address.port));
	}
	
	public void addManageResource(ManageResource resource)
	{
		resources.add(resource);
	}

	private void handleCommand(String command)
	{
		if(command.equals(Constants.STOP_CLIENT_COMMAND))
		{
			for(ManageResource resource:resources)
			{
				resource.stop();
			}
			udpSock.close();
			System.exit(1);
		}
		else
		{
			//
		}
	}
	
	public void stop() throws IOException
	{
		running = false;
		DatagramSocket socket = new DatagramSocket();
		byte[] data = "JustStopMe".getBytes();
		DatagramPacket packet = new DatagramPacket(data, data.length, new InetSocketAddress(address.host, address.port));
		socket.send(packet);
		socket.close();
	}
	
	@Override
	public void run()
	{
		while(running)
		{
			try
			{
				DatagramPacket buf = new DatagramPacket(buffer, buffer.length);
				udpSock.receive(buf);
				handleCommand(new String(buffer, 0, buf.getLength()).trim());
			}
			catch(Throwable e)
			{
				logger.error("Failed to execute UDP command.", e);
			}
		}
		
	}
	
	
}
