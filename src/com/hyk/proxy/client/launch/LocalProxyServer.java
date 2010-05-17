/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: Launcher.java 
 *
 * @author yinqiwen [ 2010-5-14 | 08:54:55 PM ]
 *
 */
package com.hyk.proxy.client.launch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
import com.hyk.proxy.client.framework.mx.ManageResource;
import com.hyk.proxy.client.framework.mx.UDPCommandServer;
import com.hyk.proxy.client.gae.event.GoogleAppEngineHttpProxyEventServiceFactory;
import com.hyk.proxy.client.util.ClientUtils;
import com.hyk.proxy.common.Constants;
import com.hyk.proxy.common.ExtensionsLauncher;
import com.hyk.proxy.common.Version;
import com.hyk.proxy.common.gae.auth.User;
import com.hyk.proxy.common.http.message.HttpServerAddress;
import com.hyk.proxy.common.rpc.service.FetchService;
import com.hyk.proxy.common.rpc.service.MasterNodeService;
import com.hyk.proxy.common.rpc.service.RemoteServiceManager;
import com.hyk.proxy.common.update.UpdateCheck;
import com.hyk.proxy.common.update.UpdateCheckResults;
import com.hyk.rpc.core.RPC;
import com.hyk.rpc.core.RpcException;

/**
 *
 */
public class LocalProxyServer implements ManageResource
{
	protected Logger				logger	= LoggerFactory.getLogger(getClass());

	private ThreadPoolExecutor		workerExecutor;
	private List<RPC>				rpcs	= new ArrayList<RPC>();
	private HttpLocalProxyServer	server;
	private UDPCommandServer		commandServer;
	private UpdateCheck				updateChecker;

	public LocalProxyServer()
	{
		ExtensionsLauncher.init();
	}

	protected FetchService initXmppFetchService(HykProxyServerAuth appid, RPC rpc) throws XMPPException
	{
		RemoteServiceManager remoteServiceManager = rpc.getRemoteService(RemoteServiceManager.class, RemoteServiceManager.NAME,
				ClientUtils.createXmppAddress(appid.appid));
		checkVersionCompatability(remoteServiceManager);
		User info = new User();
		info.setEmail(appid.user);
		info.setPasswd(appid.passwd);
		return remoteServiceManager.getFetchService(info);
	}

	protected FetchService initHttpFetchService(HykProxyServerAuth appid, RPC rpc)
	{
		HttpServerAddress remoteAddress = ClientUtils.createHttpServerAddress(appid.appid);
		// HttpServerAddress remoteAddress = new HttpServerAddress("localhost",
		// 8888, Constants.HTTP_INVOKE_PATH);
		RemoteServiceManager remoteServiceManager = rpc.getRemoteService(RemoteServiceManager.class, RemoteServiceManager.NAME, remoteAddress);
		checkVersionCompatability(remoteServiceManager);
		User info = new User();
		info.setEmail(appid.user);
		info.setPasswd(appid.passwd);
		return remoteServiceManager.getFetchService(info);
	}

	private void checkVersionCompatability(RemoteServiceManager remoteServiceManager)
	{
		String serverVersion = remoteServiceManager.getServerVersion();
		if(!Version.value.equals(serverVersion))
		{
			String cause = String.format("Client's version:%s is not compatible with Server's version:%s.", Version.value, serverVersion);
			throw new IllegalArgumentException(cause);
		}
	}

	protected List<HykProxyServerAuth> retrieveShareAppIds(Config config) throws IOException, RpcException, XMPPException
	{
		MasterNodeService master = null;
		RPC rpc;
		if(config.getClient2ServerConnectionMode().equals(ConnectionMode.HTTP2GAE))
		{
			rpc = ClientUtils.createHttpRPC(Constants.MASTER_APPID, workerExecutor);
			master = rpc.getRemoteService(MasterNodeService.class, MasterNodeService.NAME,
					ClientUtils.createHttpServerAddress(Constants.MASTER_APPID));

		}
		else
		{
			if(null != config.getXmppAccounts())
			{
				XmppAccount account = config.getXmppAccounts().get(0);
				rpc = ClientUtils.createXmppRPC(account, workerExecutor);
				master = rpc.getRemoteService(MasterNodeService.class, MasterNodeService.NAME,
						ClientUtils.createXmppAddress(Constants.MASTER_APPID));
			}
		}
		try
		{
			if(null != master)
			{
				List<String> appids = master.randomRetrieveAppIds();
				if(null != appids)
				{
					List<HykProxyServerAuth> auths = new ArrayList<HykProxyServerAuth>();
					for(String appid : appids)
					{
						HykProxyServerAuth auth = new HykProxyServerAuth();
						auth.appid = appid;
						auth.user = Constants.ANONYMOUSE_NAME;
						auth.passwd = Constants.ANONYMOUSE_NAME;
						auths.add(auth);
					}
					return auths;
				}
			}
			return Collections.EMPTY_LIST;
		}
		finally
		{
			//rpc.close();
		}
	}

	protected List<FetchService> retriveFetchServices(Config config, ThreadPoolExecutor workerExecutor) throws IOException, RpcException, XMPPException
	{
		List<FetchService> ret = new ArrayList<FetchService>();
		List<HykProxyServerAuth> auths = config.getHykProxyServerAuths();
		if(null == auths || auths.isEmpty())
		{
			//If no config hyk-proxy-server, try retrieve share appids from master node
			auths = retrieveShareAppIds(config);
		}
		for(HykProxyServerAuth auth : auths)
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
			if(null != commandServer)
			{
				commandServer.stop();
				commandServer = null;
			}
			Config config = ConfigService.getDefaultConfig();
			workerExecutor = new OrderedMemoryAwareThreadPoolExecutor(config.getThreadPoolSize(), 0, 0);
			commandServer = new UDPCommandServer(config.getLocalProxyServerAddress());
			commandServer.addManageResource(this);
			workerExecutor.execute(commandServer);
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
			if(fetchServices.isEmpty())
			{
				return "No fetch service found, please check you configuration.";
			}
			server = new HttpLocalProxyServer(config.getLocalProxyServerAddress(), workerExecutor, esf);
			updateChecker = new UpdateCheck(fetchServices.get(0));
			return fetchServices.size() + " fetch service is working.";
		}
		catch(Exception e)
		{
			logger.error("Failed to launch local proxy server.", e);
			return e.getMessage();
		}
	}

	public UpdateCheckResults checkForUpdates()
	{
		return updateChecker.checkForUpdates();
	}

	public void stop()
	{
		if(null != server)
		{
			server.close();
			server = null;
		}
		for(RPC rpc : rpcs)
		{
			// rpc.close();
		}
		rpcs.clear();
		workerExecutor.shutdownNow();
	}

}
