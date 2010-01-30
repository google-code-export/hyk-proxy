/**
 * 
 */
package com.hyk.proxy.gae.server.core;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.gae.common.HttpServerAddress;
import com.hyk.proxy.gae.server.core.rpc.HttpServletRpcChannel;
import com.hyk.proxy.gae.server.core.rpc.XmppServletRpcChannel;
import com.hyk.proxy.gae.server.core.service.FetchServiceImpl;
import com.hyk.rpc.core.RPC;

/**
 * @author Administrator
 *
 */
public class Launcher extends HttpServlet{

	protected Logger								logger			= LoggerFactory.getLogger(getClass());
	
	private static XmppServletRpcChannel xmppServletRpcChannel = null;
	private static HttpServletRpcChannel httpServletRpcChannel = null;
	
	
	public static XmppServletRpcChannel getXmppServletRpcChannel()
	{
		return xmppServletRpcChannel;
	}
	
	public static HttpServletRpcChannel getHttpServletRpcChannel()
	{
		return httpServletRpcChannel;
	}
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		if(logger.isInfoEnabled())
		{
			logger.info("Launcher init!");
		}
		XmppServletRpcChannel transport = new XmppServletRpcChannel("hykserver@appspot.com");
		xmppServletRpcChannel = transport;
		RPC xmppRpc = new RPC(transport);
		xmppRpc.getLocalNaming().bind("fetch", new FetchServiceImpl());
		
		httpServletRpcChannel = new HttpServletRpcChannel(new HttpServerAddress("localhost", 8888, "fetchproxy"));
		RPC httpRpc = new RPC(httpServletRpcChannel);
		httpServletRpcChannel.setMaxMessageSize(10240000);
		httpRpc.getLocalNaming().bind("fetch", new FetchServiceImpl());
		
	}
}
