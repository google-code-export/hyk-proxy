/**
 * 
 */
package org.hyk.proxy.gae.server.servlet;

import java.util.List;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.hyk.proxy.gae.common.event.GAEEvents;
import org.hyk.proxy.gae.server.handler.ServerEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yinqiwen
 * 
 */
public class Launcher extends HttpServlet
{

	protected static Logger logger = LoggerFactory.getLogger(Launcher.class);

	public static void initServer() throws ServletException
	{
		try
		{
			ServerEventHandler handler = new ServerEventHandler();
			GAEEvents.init(handler, true);
			if (logger.isInfoEnabled())
			{
				logger.info("hyk-proxy v2 GAE Server init success!");
			}
		}
		catch (Exception e)
		{
			logger.error("Error occured when init launch servlet!", e);
			throw new ServletException(e);
		}
	}

	@Override
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		initServer();
	}
}
