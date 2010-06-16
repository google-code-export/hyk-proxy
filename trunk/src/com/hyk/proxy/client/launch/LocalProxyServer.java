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

import java.util.concurrent.ThreadPoolExecutor;

import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.client.application.gae.event.GoogleAppEngineHttpProxyEventServiceFactory;
import com.hyk.proxy.client.application.seattle.event.SeattleProxyEventServiceFactory;
import com.hyk.proxy.client.config.Config;
import com.hyk.proxy.client.config.ConfigService;
import com.hyk.proxy.client.framework.event.HttpProxyEventServiceFactory;
import com.hyk.proxy.client.framework.httpserver.HttpLocalProxyServer;
import com.hyk.proxy.client.framework.mx.ManageResource;
import com.hyk.proxy.client.framework.mx.UDPCommandServer;
import com.hyk.proxy.client.framework.status.StatusMonitor;
import com.hyk.proxy.client.plugin.PluginManager;

/**
 *
 */
public class LocalProxyServer implements ManageResource
{
	protected Logger						logger	= LoggerFactory.getLogger(getClass());

	private ThreadPoolExecutor				workerExecutor;

	private HttpLocalProxyServer			server;
	private UDPCommandServer				commandServer;
	// private UpdateCheck updateChecker;
	private HttpProxyEventServiceFactory	esf		= null;
	
	private static boolean isInited = false;

	protected static synchronized void init() throws Exception
	{
		if(isInited) return;
		PluginManager.getInstance().loadPlugins();
		PluginManager.getInstance().activatePlugins();
		HttpProxyEventServiceFactory.Registry.register(new GoogleAppEngineHttpProxyEventServiceFactory());
		HttpProxyEventServiceFactory.Registry.register(new SeattleProxyEventServiceFactory());
		isInited = true;
	}
	public void launch(StatusMonitor monitor)
	{
		
		try
		{
			init();
			if(null != commandServer)
			{
				commandServer.stop();
				commandServer = null;
			}
			Config config = ConfigService.getDefaultConfig();
			workerExecutor = new OrderedMemoryAwareThreadPoolExecutor(config.getThreadPoolSize(), 0, 0);
//			workerExecutor =  new ThreadPoolExecutor(config.getThreadPoolSize(), Integer.MAX_VALUE,
//                    5000, TimeUnit.MILLISECONDS,
//                    new SynchronousQueue<Runnable>());
			switch(config.getClient2ServerConnectionMode())
			{
				default:
				{
					esf = HttpProxyEventServiceFactory.Registry.getHttpProxyEventServiceFactory(config.getProxyEventServiceFactory());
					esf.init(config, workerExecutor, monitor);
					break;
				}
			}
			server = new HttpLocalProxyServer(config.getLocalProxyServerAddress(), workerExecutor, esf, monitor);
			commandServer = new UDPCommandServer(config.getLocalProxyServerAddress());
			commandServer.addManageResource(this);
			workerExecutor.execute(commandServer);
			// return fetchServices.size() + " fetch service is working.";
		}
		catch(Exception e)
		{
			logger.error("Failed to launch local proxy server.", e);
			// return e.getMessage();
			monitor.clearStatusHistory();
			monitor.notifyStatus("Failed to launch local proxy server.");
		}
	}

	public void stop()
	{
		if(null != server)
		{
			server.close();
			server = null;
		}
		if(null != esf)
		{
			esf.close();
			esf = null;
		}
		if(null != workerExecutor)
		{
			workerExecutor.shutdown();
			workerExecutor = null;
		}
		
	}

}
