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
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.hyk.proxy.android.config.Config;
import org.hyk.proxy.android.config.Config.ConnectionMode;
import org.hyk.proxy.android.config.Config.HykProxyServerAuth;
import org.hyk.proxy.android.config.Config.XmppAccount;
import org.hyk.proxy.framework.common.Misc;
import org.hyk.proxy.framework.event.HttpProxyEventService;
import org.hyk.proxy.framework.event.HttpProxyEventServiceFactory;
import org.hyk.proxy.framework.util.ListSelector;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jivesoftware.smack.XMPPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.client.util.ClientUtils;
import com.hyk.proxy.common.Constants;
import com.hyk.proxy.common.Version;
import com.hyk.proxy.common.gae.auth.User;
import com.hyk.proxy.common.http.message.HttpRequestExchange;
import com.hyk.proxy.common.rpc.service.AsyncFetchService;
import com.hyk.proxy.common.rpc.service.AsyncRemoteServiceManager;
import com.hyk.proxy.common.rpc.service.FetchService;
import com.hyk.proxy.common.rpc.service.MasterNodeService;
import com.hyk.proxy.common.rpc.service.RemoteServiceManager;
import com.hyk.rpc.core.RPC;
import com.hyk.rpc.core.RpcCallback;
import com.hyk.rpc.core.RpcCallbackResult;
import com.hyk.rpc.core.RpcException;
import com.hyk.rpc.core.address.Address;
import com.hyk.rpc.core.util.RpcUtil;

/**
 *
 */
public class GoogleAppEngineHttpProxyEventServiceFactory implements
		HttpProxyEventServiceFactory {
	public static final String NAME = "GAE";

	protected Logger logger = LoggerFactory.getLogger(getClass());
	private FetchServiceSelector selector;
	// private SSLContext sslContext;
	private ExecutorService workerExecutor;
	// private UpdateCheck updateChecker;
	private List<RPC> rpcs = new ArrayList<RPC>();

	public void init() throws Exception {
		// this.sslContext = ClientUtils.initSSLContext();
		this.workerExecutor = Misc.getGlobalThreadPool();

		Misc.getGlobalThreadPool().submit(new Runnable() {
			@Override
			public void run() {
				ClientUtils.checkRemoteServer();
			}
		});
		Misc.getTrace().info("GAE connection mode:" + Config.getInstance().getClient2ServerConnectionMode());
		int ret = ClientUtils.selectDefaultGoogleProxy();
		List<FetchService> fetchServices = retriveFetchServices(Config
				.getInstance());
		if (fetchServices.isEmpty()) {
			if (Config.getInstance().selectDefaultHttpProxy()) {
				if (ret != ClientUtils.DIRECT) {
					Config.getInstance().clearProxy();
				}
				fetchServices = retriveFetchServices(Config.getInstance());
				if (fetchServices.isEmpty()) {
					if (Config.getInstance().selectDefaultHttpsProxy()) {
						fetchServices = retriveFetchServices(Config
								.getInstance());
					}
				}
			}
		}

		if (fetchServices.isEmpty()) {
			throw new IllegalArgumentException(
					"No fetch service found, please check you configuration.");
		}

		selector = new FetchServiceSelector(fetchServices);
		if (fetchServices.size() > 1) {
			Misc.getTrace().info(
					fetchServices.size() + " fetch services are working.");
		} else {
			Misc.getTrace().info(
					fetchServices.size() + " fetch service is working.");
		}

	}

	protected List<HykProxyServerAuth> retrieveShareAppIds(Config config)
			throws IOException, RpcException, XMPPException {
		MasterNodeService master = ClientUtils.getMasterNodeService(config);
		if (null != master) {
			List<String> appids = master.randomRetrieveAppIds();
			if (null != appids) {
				List<HykProxyServerAuth> auths = new ArrayList<HykProxyServerAuth>();
				for (String appid : appids) {
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

	protected List<FetchService> retriveFetchServices(final Config config)
			throws IOException, RpcException, XMPPException {
		if (logger.isDebugEnabled()) {
			logger.debug("Start retrive remote fetch services.");
		}
		List<HykProxyServerAuth> auths = config.getHykProxyServerAuths();
		if (null == auths || auths.isEmpty()) {
			// If no config hyk-proxy-server, try retrieve share appids from
			// master node
			auths = retrieveShareAppIds(config);
		}
		List<Callable<FetchService>> invokeTasks = new LinkedList<Callable<FetchService>>();
		// Init RPC channels
		switch (config.getClient2ServerConnectionMode()) {
		case HTTPS2GAE:
		case HTTP2GAE: {
			rpcs.add(ClientUtils.createHttpRPC(workerExecutor));
			break;
		}
		case XMPP2GAE: {
			for (XmppAccount account : config.getXmppAccounts()) {
				rpcs.add(ClientUtils.createXmppRPC(account, workerExecutor));
			}
			break;
		}
		}
		// Retrieve remote fetch services
		for (final HykProxyServerAuth auth : auths) {
			for (final RPC rpc : rpcs) {
				invokeTasks.add(new Callable<FetchService>() {
					@Override
					public FetchService call() throws Exception {
						return initFetchService(auth, rpc,
								config.getClient2ServerConnectionMode());
					}
				});
			}
		}
		List<FetchService> ret = new ArrayList<FetchService>();
		try {
			List<Future<FetchService>> invokeResults = workerExecutor
					.invokeAll(invokeTasks);

			for (Future<FetchService> result : invokeResults) {
				FetchService serv = result.get();
				if (null != serv) {
					ret.add(result.get());
				}
			}
		} catch (Exception e) {
			logger.error("Failed to execute retrieve fetch service task!", e);
		}

		return ret;
	}

	protected FetchService initFetchService(final HykProxyServerAuth appid,
			RPC rpc, ConnectionMode mode) {
		try {
			Address remoteAddress = null;
			switch (mode) {
			case HTTPS2GAE:
			case HTTP2GAE: {
				remoteAddress = ClientUtils
						.createHttpServerAddress(appid.appid);
				break;
			}
			case XMPP2GAE: {
				remoteAddress = ClientUtils.createXmppAddress(appid.appid);
				break;
			}
			}
			int oldtimeout = rpc.getSessionManager().getSessionTimeout();
			if (!mode.equals(ConnectionMode.XMPP2GAE)) {
				rpc.getSessionManager().setSessionTimeout(20000);
			}
			final RemoteServiceManager remoteServiceManager = rpc
					.getRemoteService(RemoteServiceManager.class,
							RemoteServiceManager.NAME, remoteAddress);
			if (!mode.equals(ConnectionMode.XMPP2GAE)) {
				rpc.getSessionManager().setSessionTimeout(oldtimeout);
			}
			//AsyncRemoteServiceManager asyncRemoteServiceManager = RpcUtil
			//		.asyncWrapper(remoteServiceManager,
			//				AsyncRemoteServiceManager.class);
			//checkVersionCompatability(asyncRemoteServiceManager, appid.appid);
			User info = new User();
			info.setEmail(appid.user);
			info.setPasswd(appid.passwd);
			return remoteServiceManager.getFetchService(info);
		} catch (Exception e) {
			logger.error("Failed to init fetch service.", e);
			return null;
		}

	}

	private void checkVersionCompatability(
			AsyncRemoteServiceManager remoteServiceManager, final String appid) {
		remoteServiceManager.getServerVersion(new RpcCallback<String>() {
			@Override
			public void callBack(RpcCallbackResult<String> result) {
				try {
					String serverVersion = result.get();
					if (Version.value.contains(serverVersion)
							|| serverVersion.contains(Version.value)) {
						return;
					}
					//just return without warning for specific version
					if(serverVersion.equalsIgnoreCase("0.9.1"))
					{
						return;
					}
					String cause = String
							.format("Client's version:%s may be not compatible with Server's version:%s .",
									Version.value, serverVersion);
					logger.warn(cause);
				} catch (Throwable e) {
					logger.error("Failed to get version from server.", e);
				}
			}
		});

	}

	@Override
	public HttpProxyEventService createHttpProxyEventService() {
		return new GoogleAppEngineHttpProxyEventService(selector,
				workerExecutor);
	}

	static class FetchServiceSelector extends ListSelector<FetchService> {
		// private List<FetchService> fetchServices;
		private List<AsyncFetchService> asyncFetchServices;
		private List<String> appidList;
		private int cursor;

		public FetchServiceSelector(List<FetchService> fetchServices)
				throws RpcException {
			super(fetchServices);
			// Collections.shuffle(fetchServices);
			// this.fetchServices = fetchServices;
			asyncFetchServices = new ArrayList<AsyncFetchService>();
			appidList = new ArrayList<String>();
			for (FetchService service : fetchServices) {
				asyncFetchServices.add(RpcUtil.asyncWrapper(service,
						AsyncFetchService.class));
				Address addr = RPC.getRemoteObjectAddress(service);
				appidList.add(ClientUtils.extractAppId(addr));
			}
		}

		private int getBindingServiceIndex(HttpRequestExchange req) {
			String binding = Config.getInstance().getBindingAppId(
					req.getHeaderValue(HttpHeaders.Names.HOST));
			if (null != binding) {
				for (int i = cursor; i < appidList.size(); i++) {
					if (appidList.get(i).endsWith(binding)) {
						return i;
					}
				}
				for (int i = 0; i < cursor; i++) {
					if (appidList.get(i).endsWith(binding)) {
						return i;
					}
				}
			}
			return -1;
		}

		public synchronized AsyncFetchService selectAsync(
				HttpRequestExchange req) {
			int bindIndex = getBindingServiceIndex(req);
			if (bindIndex >= 0) {
				return asyncFetchServices.get(bindIndex);
			}
			if (cursor >= asyncFetchServices.size()) {
				cursor = 0;
			}
			return asyncFetchServices.get(cursor++);
		}

		public FetchService select(HttpRequestExchange req) {
			int bindIndex = getBindingServiceIndex(req);
			if (bindIndex >= 0) {
				return list.get(bindIndex);
			}
			return select();
		}

	}

	@Override
	public void destroy() throws Exception {
		for (RPC rpc : rpcs) {
			rpc.close();
		}
		rpcs.clear();
	}

	@Override
	public String getName() {
		return NAME;
	}

}
