/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: HttpProxyServlet.java 
 *
 * @author yinqiwen [ 2010-1-29 | pm09:58:01 ]
 *
 */
package com.hyk.proxy.gae.server.core.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.gae.server.core.Launcher;

/**
 *
 */
public class HttpInvokeServlet extends HttpServlet
{
	protected Logger	logger	= LoggerFactory.getLogger(getClass());
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		if(logger.isDebugEnabled())
		{
			logger.debug("Process message");
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
	
}
