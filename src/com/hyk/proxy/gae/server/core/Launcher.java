/**
 * 
 */
package com.hyk.proxy.gae.server.core;

import java.util.List;
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
import com.hyk.proxy.gae.server.remote.AppengineRemoteObjectIdGenerator;
import com.hyk.proxy.gae.server.remote.RemoteObject;
import com.hyk.proxy.gae.server.remote.RemoteObjectType;
import com.hyk.proxy.gae.server.util.AppEngineTimer;
import com.hyk.proxy.gae.server.util.HttpMessageCompressPreference;
import com.hyk.proxy.gae.server.util.KeepJVMWarmTaskHandler;
import com.hyk.proxy.gae.server.util.ServerUtils;
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
	
	protected boolean createRemoteServiceManagerIfNotExist(RPC rpc)
	{
		List<RemoteObject> ros = ServerUtils.loadRemoteObjects();
		if(null != ros && !ros.isEmpty())
		{
			for(RemoteObject ro : ros)
			{
				switch(ro.getType())
				{
					case SERVICE_MANAGER:
					{
						Object obj = rpc.exportRemoteObject(new RemoteServiceManagerImpl(rpc), ro.getObjid());
						rpc.getLocalNaming().bind(RemoteServiceManager.NAME, obj);
						return true;
					}
				}
			}
		}
		rpc.getLocalNaming().bind(RemoteServiceManager.NAME, new RemoteServiceManagerImpl(rpc));	
		RemoteObject ro = new RemoteObject();
		long objid = rpc.getRemoteObjectId(rpc.getLocalNaming().lookup(RemoteServiceManager.NAME));
		ro.setObjid(objid);
		ro.setType(RemoteObjectType.SERVICE_MANAGER);
		ServerUtils.storeObject(ro);
		return false;
	}
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		try
		{	
			Config hykConfig = Config.init(config);
			
			Properties initProps = new Properties();
			initProps.setProperty(RpcConstants.TIMER_CLASS, AppEngineTimer.class.getName());
			initProps.setProperty(RpcConstants.COMPRESS_PREFER, HttpMessageCompressPreference.class.getName());
			initProps.setProperty(RpcConstants.REMOTE_OBJECTID_GEN, AppengineRemoteObjectIdGenerator.class.getName());
			HttpMessageCompressPreference.init(hykConfig.getCompressor(), hykConfig.getCompressTrigger(), hykConfig.getIgnorePatterns());
			
			XmppServletRpcChannel transport = new XmppServletRpcChannel(hykConfig.getAppId() + "@appspot.com");
			xmppServletRpcChannel = transport;	
			xmppServletRpcChannel.setMaxMessageSize(hykConfig.getMaxXmppMessageSize());
			RPC xmppRpc = new RPC(transport, initProps);
			createRemoteServiceManagerIfNotExist(xmppRpc);
			
			httpServletRpcChannel = new HttpServletRpcChannel(new HttpServerAddress(hykConfig.getAppId() + ".appspot.com", "/fetchproxy"));
			RPC httpRpc = new RPC(httpServletRpcChannel, initProps);
			httpServletRpcChannel.setMaxMessageSize(10240000);
			createRemoteServiceManagerIfNotExist(httpRpc);
			
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
