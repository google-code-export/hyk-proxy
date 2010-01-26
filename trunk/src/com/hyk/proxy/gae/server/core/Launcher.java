/**
 * 
 */
package com.hyk.proxy.gae.server.core;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.gae.server.core.rpc.XmppServletRpcChannel;
import com.hyk.proxy.gae.server.core.service.FetchServiceImpl;
import com.hyk.rpc.core.RPC;

/**
 * @author Administrator
 *
 */
public class Launcher extends HttpServlet{

	protected Logger								logger			= LoggerFactory.getLogger(getClass());
	
	private static XmppServletRpcChannel channel = null;
	
	
	public static XmppServletRpcChannel getXmppServletRpcChannel()
	{
		return channel;
	}
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		if(logger.isInfoEnabled())
		{
			logger.info("Launcher init!");
		}
		XmppServletRpcChannel transport = new XmppServletRpcChannel("hykserver@appspot.com");
		channel = transport;
		//TCPRpcChannel transport = new TCPRpcChannel(Executors.newFixedThreadPool(10), 48101);
		RPC rpc = new RPC(transport);
		rpc.getLocalNaming().bind("fetch", new FetchServiceImpl());
	}
}
