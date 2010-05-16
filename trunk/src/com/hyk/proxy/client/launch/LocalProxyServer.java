/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: Launcher.java 
 *
 * @author yinqiwen [ 2010-5-14 | ÏÂÎç08:54:55 ]
 *
 */
package com.hyk.proxy.client.launch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;
import org.jivesoftware.smack.XMPPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.client.config.Config;
import com.hyk.proxy.client.config.ConfigService;
import com.hyk.proxy.client.config.Config.ConnectionMode;
import com.hyk.proxy.client.config.Config.HykProxyServerAuth;
import com.hyk.proxy.client.config.Config.XmppAccount;
import com.hyk.proxy.client.framework.event.HttpProxyEventServiceFactory;
import com.hyk.proxy.client.framework.httpserver.HttpLocalProxyServer;
import com.hyk.proxy.client.gae.event.GoogleAppEngineHttpProxyEventServiceFactory;
import com.hyk.proxy.client.util.ClientUtils;
import com.hyk.proxy.common.Constants;
import com.hyk.proxy.common.ExtensionsLauncher;
import com.hyk.proxy.common.gae.auth.User;
import com.hyk.proxy.common.http.message.HttpServerAddress;
import com.hyk.proxy.common.rpc.service.FetchService;
import com.hyk.proxy.common.rpc.service.RemoteServiceManager;
import com.hyk.proxy.common.xmpp.XmppAddress;
import com.hyk.rpc.core.RPC;
import com.hyk.rpc.core.RpcException;

/**
 *
 */
public class LocalProxyServer
{
	protected Logger	logger	= LoggerFactory.getLogger(getClass());

	private ThreadPoolExecutor workerExecutor;
	private List<RPC> rpcs = new ArrayList<RPC>();
	private HttpLocalProxyServer server;
	
	public LocalProxyServer()
	{
		ExtensionsLauncher.init();
	}
	
	protected FetchService initXmppFetchService(HykProxyServerAuth appid, RPC rpc) throws XMPPException
	{
		RemoteServiceManager remoteServiceManager = rpc.getRemoteService(RemoteServiceManager.class, RemoteServiceManager.NAME, new XmppAddress(
				appid.appid + "@appspot.com"));
		User info = new User();
		info.setEmail(appid.user);
		info.setPasswd(appid.passwd);
		return remoteServiceManager.getFetchService(info);
	}

	protected FetchService initHttpFetchService(HykProxyServerAuth appid, RPC rpc) throws XMPPException
	{
		HttpServerAddress remoteAddress = new HttpServerAddress(appid.appid + ".appspot.com", Constants.HTTP_INVOKE_PATH);
		//HttpServerAddress remoteAddress = new HttpServerAddress("localhost", 8888, Constants.HTTP_INVOKE_PATH);
		RemoteServiceManager remoteServiceManager = rpc.getRemoteService(RemoteServiceManager.class, RemoteServiceManager.NAME, remoteAddress);
		User info = new User();
		info.setEmail(appid.user);
		info.setPasswd(appid.passwd);
		return remoteServiceManager.getFetchService(info);
	}

	protected List<FetchService> retriveFetchServices(Config config, ThreadPoolExecutor workerExecutor) throws IOException, RpcException, XMPPException
	{
		List<FetchService> ret = new ArrayList<FetchService>();
		for(HykProxyServerAuth auth : config.getHykProxyServerAuths())
		{
			if(config.getClient2ServerConnectionMode().equals(ConnectionMode.HTTP2GAE))
			{
				RPC rpc = ClientUtils.createHttpRPC(auth.appid, workerExecutor);
				ret.add(initHttpFetchService(auth, rpc));
				rpcs.add(rpc);
			}
			else
			{
				if(null != config.getXmppAccounts())
				{
					for(XmppAccount account : config.getXmppAccounts())
					{
						RPC rpc = ClientUtils.createXmppRPC(account, workerExecutor);
						ret.add(initXmppFetchService(auth, rpc));
						rpcs.add(rpc);
					}
				}
			}
		}
		return ret;
	}

	public String launch()
	{
		try
		{
			Config config = ConfigService.getDefaultConfig();
			workerExecutor = new OrderedMemoryAwareThreadPoolExecutor(config.getThreadPoolSize(), 0, 0);
			HttpProxyEventServiceFactory esf = null;
			List<FetchService> fetchServices = null;
			switch(config.getClient2ServerConnectionMode())
			{
				default:
				{
					fetchServices = retriveFetchServices(config, workerExecutor);
					esf = new GoogleAppEngineHttpProxyEventServiceFactory(fetchServices, ClientUtils.initSSLContext(), workerExecutor);
					break;
				}
			}
			server = new HttpLocalProxyServer(config.getLocalProxyServerAddress(), workerExecutor, esf);
//			String url = "http://hyk-proxy.googlecode.com/svn/trunk/build.xml";
//			HttpRequestExchange req = new HttpRequestExchange();
//			req.url = url;
//			req.method = "GET";
//			System.out.println(new String(fetchServices.get(0).fetch(req).getBody()));
			
			return fetchServices.size() + " fetch service is working.";
		}
		catch(Exception e)
		{
			logger.error("Failed to launch local proxy server.", e);
			return e.getMessage();
		}

	}

	public void stop()
	{
		if(null != server)
		{
			server.close();
			server = null;
		}
		for(RPC rpc:rpcs)
		{
			rpc.close();
		}
		rpcs.clear();
		workerExecutor.shutdownNow();
	}

}
