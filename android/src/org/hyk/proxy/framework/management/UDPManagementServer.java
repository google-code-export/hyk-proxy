/**
 * This file is part of the hyk-proxy-framework project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: UDPManagementServer.java 
 *
 * @author yinqiwen [ 2010-8-12 | 08:04:11 PM ]
 *
 */
package org.hyk.proxy.framework.management;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hyk.proxy.android.config.Config;
import org.hyk.proxy.framework.util.SimpleSocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 */
public class UDPManagementServer implements Runnable
{
	protected Logger	logger	= LoggerFactory.getLogger(getClass());
	private Map<String, ManageResource> resources = new ConcurrentHashMap<String, ManageResource>();
	private DatagramSocket udpSock;
	private byte[] buffer = new byte[65536];
	private boolean running = true;
	private SimpleSocketAddress address;
	
	public UDPManagementServer(SimpleSocketAddress address) throws SocketException, UnknownHostException
	{
		this.address = address;
		udpSock = new DatagramSocket(address.port, InetAddress.getByName(address.host));
	}
	
	public void addManageResource(ManageResource resource)
	{
		resources.put(resource.getName(), resource);
	}

	private void handleCommand(String command)
	{
		String[] cmds = command.split("\\s+");
		ManageResource resource = resources.get(cmds[0]);
		resource.handleManagementCommand(command.substring(cmds[0].length()).trim());
	}
	
	
	public static void sendUDPCommand(String resourceName, String cmd) throws IOException
	{
		DatagramSocket socket = new DatagramSocket();
		byte[] data = (resourceName + " " + cmd).getBytes();
		Config config = Config.getInstance();
		SimpleSocketAddress addr = config.getLocalProxyServerAddress();
		DatagramPacket packet = new DatagramPacket(data, data.length, new InetSocketAddress(addr.host, addr.port));
		socket.send(packet);
		socket.close();
	}
	
	public void stop()
	{
		running = false;
		udpSock.close();
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
		udpSock.close();
	}
}
