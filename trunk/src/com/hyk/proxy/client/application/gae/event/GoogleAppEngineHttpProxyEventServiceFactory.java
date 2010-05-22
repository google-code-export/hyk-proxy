/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: GoogleAppEngineHttpProxyEventServiceFactory.java 
 *
 * @author yinqiwen [ 2010-5-13 | 08:49:39 PM]
 *
 */
package com.hyk.proxy.client.application.gae.event;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

import javax.net.ssl.SSLContext;

import org.jivesoftware.smack.XMPPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.client.config.Config;
import com.hyk.proxy.client.config.Config.ConnectionMode;
import com.hyk.proxy.client.config.Config.HykProxyServerAuth;
import com.hyk.proxy.client.config.Config.XmppAccount;
import com.hyk.proxy.client.framework.event.HttpProxyEventService;
import com.hyk.proxy.client.framework.event.HttpProxyEventServiceFactory;
import com.hyk.proxy.client.framework.status.StatusMonitor;
import com.hyk.proxy.client.util.ClientUtils;
import com.hyk.proxy.client.util.ListSelector;
import com.hyk.proxy.common.Constants;
import com.hyk.proxy.common.Version;
import com.hyk.proxy.common.gae.auth.User;
import com.hyk.proxy.common.http.message.HttpServerAddress;
import com.hyk.proxy.common.rpc.service.AsyncFetchService;
import com.hyk.proxy.common.rpc.service.FetchService;
import com.hyk.proxy.common.rpc.service.MasterNodeService;
import com.hyk.proxy.common.rpc.service.RemoteServiceManager;
import com.hyk.proxy.common.update.UpdateCheck.UpdateCheckFactory;
import com.hyk.rpc.core.RPC;
import com.hyk.rpc.core.RpcException;
import com.hyk.rpc.core.util.RpcUtil;

/**
 *
 */
public class GoogleAppEngineHttpProxyEventServiceFactory implements HttpProxyEventServiceFactory
{
	protected Logger					logger			= LoggerFactory.getLogger(getClass());
	private FetchServiceSelector	selector;
	private SSLContext				sslContext;
	private Executor	workerExecutor;
	//private UpdateCheck				updateChecker;
	private List<RPC>				rpcs	= new ArrayList<RPC>();

	public GoogleAppEngineHttpProxyEventServiceFactory(Config config, Executor	workerExecutor, StatusMonitor monitor) throws Exception
	{
		List<FetchService> fetchServices = retriveFetchServices(config, workerExecutor);
		if(fetchServices.isEmpty())
		{
			throw new IllegalArgumentException("No fetch service found, please check you configuration.");
		}
		UpdateCheckFactory.initSingletonInstance(fetchServices.get(0));
		selector = new FetchServiceSelector(fetchServices);
		this.sslContext = ClientUtils.initSSLContext();
		this.workerExecutor = workerExecutor;
		monitor.clearStatusHistory();
		monitor.notifyStatus(fetchServices.size() + " fetch service is working.");
	}
	
	protected List<HykProxyServerAuth> retrieveShareAppIds(Config config) throws IOException, RpcException, XMPPException
	{
		MasterNodeService master = ClientUtils.getMasterNodeService(config);
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

	protected List<FetchService> retriveFetchServices(Config config, Executor workerExecutor) throws IOException, RpcException, XMPPException
	{
		List<FetchService> ret = new ArrayList<FetchService>();
		List<HykProxyServerAuth> auths = config.getHykProxyServerAuths();
		if(null == auths || auths.isEmpty())
		{
			// If no config hyk-proxy-server, try retrieve share appids from
			// master node
			auths = retrieveShareAppIds(config);
		}
		for(HykProxyServerAuth auth : auths)
		{
			if(config.getClient2ServerConnectionMode().equals(ConnectionMode.HTTP2GAE))
			{
				try
				{
					RPC rpc = ClientUtils.createHttpRPC(auth.appid, workerExecutor);
					ret.add(initHttpFetchService(auth, rpc));
					rpcs.add(rpc);
				}
				catch(Exception e)
				{
					logger.warn("Failed to create fetch service.", e);
				}
				
			}
			else
			{
				if(null != config.getXmppAccounts())
				{
					for(XmppAccount account : config.getXmppAccounts())
					{
						try
						{
							RPC rpc = ClientUtils.createXmppRPC(account, workerExecutor);
							ret.add(initXmppFetchService(auth, rpc));
							rpcs.add(rpc);
						}
						catch(Exception e)
						{
							logger.warn("Failed to create fetch service.", e);
						}
					}
				}
			}
		}
		return ret;
	}
	
	protected FetchService initXmppFetchService(HykProxyServerAuth appid, RPC rpc) throws XMPPException
	{
		RemoteServiceManager remoteServiceManager = rpc.getRemoteService(RemoteServiceManager.class, RemoteServiceManager.NAME,
				ClientUtils.createXmppAddress(appid.appid));
		checkVersionCompatability(remoteServiceManager, appid.appid);
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
		checkVersionCompatability(remoteServiceManager, appid.appid);
		User info = new User();
		info.setEmail(appid.user);
		info.setPasswd(appid.passwd);
		return remoteServiceManager.getFetchService(info);
	}

	private void checkVersionCompatability(RemoteServiceManager remoteServiceManager, String appid)
	{
		String serverVersion = remoteServiceManager.getServerVersion();
		if(!Version.value.equals(serverVersion))
		{
			String cause = String.format("Client's version:%s is not compatible with Server's version:%s for appid:%s.", Version.value, serverVersion, appid);
			throw new IllegalArgumentException(cause);
		}
	}

	@Override
	public HttpProxyEventService createHttpProxyEventService()
	{
		return new GoogleAppEngineHttpProxyEventService(selector, sslContext, workerExecutor);
	}
	
	
	static class FetchServiceSelector extends ListSelector<FetchService>
	{
		//private List<FetchService>		fetchServices;
		private List<AsyncFetchService>	asyncFetchServices;
		private int						cursor;

		public FetchServiceSelector(List<FetchService> fetchServices) throws RpcException
		{
			super(fetchServices);
			//Collections.shuffle(fetchServices);
			//this.fetchServices = fetchServices;
			asyncFetchServices = new ArrayList<AsyncFetchService>();
			for(FetchService service : fetchServices)
			{
				asyncFetchServices.add(RpcUtil.asyncWrapper(service, AsyncFetchService.class));
			}
		}

//		public synchronized FetchService select()
//		{
//			if(cursor >= fetchServices.size())
//			{
//				cursor = 0;
//			}
//			return fetchServices.get(cursor++);
//		}

		public synchronized AsyncFetchService selectAsync()
		{
			if(cursor >= asyncFetchServices.size())
			{
				cursor = 0;
			}
			return asyncFetchServices.get(cursor++);
		}

	}

	@Override
	public void close()
	{
		for(RPC rpc : rpcs)
		{
			rpc.close();
		}
		rpcs.clear();
		
	}

}
