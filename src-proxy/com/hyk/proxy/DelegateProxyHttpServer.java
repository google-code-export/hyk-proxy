/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010, BigBand Networks Inc. All rights reserved.
 *
 * Description: DelegateProxyHttpServer.java 
 *
 * @author qiying.wang [ Jan 12, 2010 | 1:48:42 PM ]
 *
 */
package com.hyk.proxy;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 *
 */
public class DelegateProxyHttpServer
{

	public void start() throws Exception
	{
		Server server = new Server(48100);
		ServletHandler handler = new ServletHandler();
		//handler.addServlet(new ServletHolder(ProxyServlet.class));
		handler.addServletWithMapping(new ServletHolder(ProxyServlet.class), "/*");
		server.setHandler(handler);
		server.start();
	}
	
	public static void main(String[] args) throws Exception
	{
		new DelegateProxyHttpServer().start();
	}
}
