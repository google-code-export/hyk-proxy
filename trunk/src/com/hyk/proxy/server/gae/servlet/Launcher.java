/**
 * 
 */
package com.hyk.proxy.server.gae.servlet;

import java.util.List;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.common.Constants;
import com.hyk.proxy.common.ExtensionsLauncher;
import com.hyk.proxy.common.http.message.HttpServerAddress;
import com.hyk.proxy.common.rpc.service.RemoteServiceManager;
import com.hyk.proxy.server.gae.config.XmlConfig;
import com.hyk.proxy.server.gae.rpc.channel.HttpServletRpcChannel;
import com.hyk.proxy.server.gae.rpc.channel.XmppServletRpcChannel;
import com.hyk.proxy.server.gae.rpc.remote.AppEngineRemoteObjectStorage;
import com.hyk.proxy.server.gae.rpc.remote.AppEngineTimer;
import com.hyk.proxy.server.gae.rpc.remote.AppengineRemoteObjectIdGenerator;
import com.hyk.proxy.server.gae.rpc.remote.RemoteObject;
import com.hyk.proxy.server.gae.rpc.service.AccountServiceImpl;
import com.hyk.proxy.server.gae.rpc.service.RemoteServiceManagerImpl;
import com.hyk.proxy.server.gae.util.HttpMessageCompressPreference;
import com.hyk.proxy.server.gae.util.ServerUtils;
import com.hyk.rpc.core.RPC;
import com.hyk.rpc.core.constant.RpcConstants;
import com.hyk.rpc.core.remote.RemoteObjectReference;

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
	
	protected void createRemoteServiceManagerIfNotExist(RPC rpc)
	{
		List<RemoteObject> ros = ServerUtils.loadRemoteObjects();
		//logger.error("####Found " + ros.size() + " remote objects!");
		RemoteObjectReference rsmf = null;
		for(RemoteObject ro:ros)
		{
			RemoteObjectReference rf = ro.getRemoteRef();
			if(null == rf)
			{
				continue;
			}
			
			//logger.error("####Remote Type is " + rf.getImpl().getClass().getName());
			if(rf.getImpl() instanceof RemoteServiceManager)
			{
				rsmf = rf;
				break;
			}
		}
		if(null == rsmf)
		{
			RemoteServiceManagerImpl rsm = new RemoteServiceManagerImpl(rpc);
			rpc.getLocalNaming().bind(RemoteServiceManager.NAME, rsm);
		}
		else
		{
			RemoteServiceManagerImpl rsm = (RemoteServiceManagerImpl)rsmf.getImpl();
			rsm.init(rpc);
			rpc.getLocalNaming().bind(RemoteServiceManager.NAME, rpc.exportRemoteObject(rsm, rsmf.getObjID()));
		}
	}
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		ExtensionsLauncher.init();
		try
		{	
			XmlConfig hykConfig = XmlConfig.init(config);
			
			Properties initProps = new Properties();
			initProps.setProperty(RpcConstants.TIMER_CLASS, AppEngineTimer.class.getName());
			initProps.setProperty(RpcConstants.COMPRESS_PREFER, HttpMessageCompressPreference.class.getName());
			initProps.setProperty(RpcConstants.REMOTE_OBJECTID_GEN, AppengineRemoteObjectIdGenerator.class.getName());
			initProps.setProperty(RpcConstants.REMOTE_OBJECT_STORAGE, AppEngineRemoteObjectStorage.class.getName());
			HttpMessageCompressPreference.init(hykConfig.getCompressor(), hykConfig.getIgnorePatterns());
			
			XmppServletRpcChannel transport = new XmppServletRpcChannel(hykConfig.getAppId() + "@appspot.com");
			xmppServletRpcChannel = transport;	
			xmppServletRpcChannel.setMaxMessageSize(hykConfig.getMaxXmppMessageSize());
			RPC xmppRpc = new RPC(transport, initProps);
			createRemoteServiceManagerIfNotExist(xmppRpc);
			
			httpServletRpcChannel = new HttpServletRpcChannel(new HttpServerAddress(hykConfig.getAppId() + ".appspot.com",  Constants.HTTP_INVOKE_PATH));
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
