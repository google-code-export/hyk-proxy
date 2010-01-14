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
		Server server = new Server(48100);
		ServletHandler handler = new ServletHandler();
		//handler.addServlet(new ServletHolder(ProxyServlet.class));
		handler.addServletWithMapping(new ServletHolder(GaeProxyClientServlet.class), "/*");
		server.setHandler(handler);
		server.start();
	}

}
