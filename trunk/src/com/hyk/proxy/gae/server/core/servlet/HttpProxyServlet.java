/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: HttpProxyServlet.java 
 *
 * @author Administrator [ 2010-1-29 | ÏÂÎç09:58:01 ]
 *
 */
package com.hyk.proxy.gae.server.core.servlet;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.gae.server.core.Launcher;

/**
 *
 */
public class HttpProxyServlet extends HttpServlet
{
	protected Logger	logger	= LoggerFactory.getLogger(getClass());
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		if(logger.isInfoEnabled())
		{
			logger.info("Process message");
		}

		try
		{
			Launcher.getHttpServletRpcChannel().processHttpRequest(req, resp);
		}
		catch(Throwable e)
		{
			logger.warn("Failed to process message", e);
		}
	}
	
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		System.out.println("####" + req.getRequestURI());
		System.out.println("####" + req.getRequestURL());
		Enumeration<String> names = req.getHeaderNames();
		while(names.hasMoreElements())
		{
			String name = names.nextElement();
			System.out.println(name + ": " + req.getHeader(name));
		}
		resp.setStatus(200);
		resp.getWriter().println("Receive request");
	}
}
