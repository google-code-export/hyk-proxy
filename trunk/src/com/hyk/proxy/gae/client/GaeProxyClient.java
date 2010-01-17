/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010, BigBand Networks Inc. All rights reserved.
 *
 * Description: GaeProxyClient.java 
 *
 * @author qiying.wang [ Jan 14, 2010 | 3:23:06 PM ]
 *
 */
package com.hyk.proxy.gae.client;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslSocketConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 *
 */
public class GaeProxyClient
{

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception
	{
		new GaeProxyClient().start();
	}
	
	public void start() throws Exception
	{
		Server server = new Server();
		SelectChannelConnector selectChannelConnector = new SelectChannelConnector();
		selectChannelConnector.setPort(48100);
		SslSocketConnector sslSocketConnector = new SslSocketConnector();
		sslSocketConnector.setPort(48103);
		//sslSocketConnector.set
		server.addConnector(selectChannelConnector);
		//server.addConnector(sslSocketConnector);
		//System.out.println("####");
		//server.
		ServletHandler handler = new ServletHandler();
		//handler.addServlet(new ServletHolder(ProxyServlet.class));
		handler.addServletWithMapping(new ServletHolder(GaeProxyClientServlet.class), "/*");
		server.setHandler(handler);
		server.start();
	}

}
