/**
 * 
 */
package org.hyk.proxy.gae.server.servlet;

import java.util.List;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.arch.event.EventDispatcher;
import org.hyk.proxy.gae.server.handler.HTTPChannelProxyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author yinqiwen
 *
 */
public class Launcher extends HttpServlet{

	protected Logger								logger			= LoggerFactory.getLogger(getClass());
	
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		try
		{	
			EventDispatcher.getSingletonInstance().register(null, new HTTPChannelProxyHandler());
			if(logger.isInfoEnabled())
			{
				logger.info("hyk-proxy v2 GAE Server init success!");
			}
		}
		catch(Exception e)
		{
			logger.error("Error occured when init launch servlet!", e);
			throw new ServletException(e);
		}
		
		
	}
}
