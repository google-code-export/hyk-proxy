/**
 * 
 */
package com.hyk.proxy.gae.server.core;

import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.gae.common.http.message.HttpServerAddress;
import com.hyk.proxy.gae.common.service.RemoteServiceManager;
import com.hyk.proxy.gae.server.config.Config;
import com.hyk.proxy.gae.server.core.rpc.HttpServletRpcChannel;
import com.hyk.proxy.gae.server.core.rpc.XmppServletRpcChannel;
import com.hyk.proxy.gae.server.core.service.AccountServiceImpl;
import com.hyk.proxy.gae.server.core.service.FetchServiceImpl;
import com.hyk.proxy.gae.server.core.service.RemoteServiceManagerImpl;
import com.hyk.proxy.gae.server.util.HttpMessageCompressPreference;
import com.hyk.proxy.gae.server.util.KeepJVMWarmTaskHandler;
import com.hyk.rpc.core.RPC;
import com.hyk.rpc.core.constant.RpcConstants;

/**
 * @author yinqiwen
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
		try
		{	
			Config hykConfig = Config.init(config);
			
			Properties initProps = new Properties();
			initProps.setProperty(RpcConstants.TIMER_CLASS, "com.hyk.proxy.gae.server.util.AppEngineTimer");
			initProps.setProperty(RpcConstants.COMPRESS_PREFER, "com.hyk.proxy.gae.server.util.HttpMessageCompressPreference");
			HttpMessageCompressPreference.init(hykConfig.getCompressor(), hykConfig.getCompressTrigger(), hykConfig.getIgnorePatterns());
			
			XmppServletRpcChannel transport = new XmppServletRpcChannel(hykConfig.getAppId() + "@appspot.com");
			xmppServletRpcChannel = transport;	
			xmppServletRpcChannel.setMaxMessageSize(hykConfig.getMaxXmppMessageSize());
			RPC xmppRpc = new RPC(transport, initProps);
		
			//xmppRpc.getLocalNaming().bind("fetch", new FetchServiceImpl());
			xmppRpc.getLocalNaming().bind(RemoteServiceManager.NAME, new RemoteServiceManagerImpl(xmppRpc));
			
			httpServletRpcChannel = new HttpServletRpcChannel(new HttpServerAddress(hykConfig.getAppId() + ".appspot.com", "/fetchproxy"));
			RPC httpRpc = new RPC(httpServletRpcChannel, initProps);
			httpServletRpcChannel.setMaxMessageSize(10240000);
			//httpRpc.getLocalNaming().bind("fetch", new FetchServiceImpl());
			httpRpc.getLocalNaming().bind(RemoteServiceManager.NAME, new RemoteServiceManagerImpl(httpRpc));
			AccountServiceImpl.checkDefaultAccount();
			
			if(logger.isInfoEnabled())
			{
				logger.info("Launcher init!");
			}
		}
		catch(Exception e)
		{
			logger.error("Error occured when init launch servlet!", e);
		}
		
		
	}
}
