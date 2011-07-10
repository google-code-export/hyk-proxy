/**
 * This file is part of the hyk-proxy-framework project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: Framework.java 
 *
 * @author yinqiwen [ 2010-8-12 | 09:28:05 PM]
 *
 */
package org.hyk.proxy.framework;

import java.util.concurrent.ThreadPoolExecutor;

import org.hyk.proxy.android.config.Config;
import org.hyk.proxy.framework.common.Misc;
import org.hyk.proxy.framework.event.HttpProxyEventServiceFactory;
import org.hyk.proxy.framework.httpserver.HttpLocalProxyServer;
import org.hyk.proxy.framework.management.UDPManagementServer;
import org.hyk.proxy.framework.trace.Trace;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.client.application.gae.event.GoogleAppEngineHttpProxyEventServiceFactory;
import com.hyk.proxy.client.util.GoogleAvailableService;
import com.hyk.proxy.common.ExtensionsLauncher;



/**
 *
 */
public class Framework 
{
	protected Logger logger = LoggerFactory.getLogger(getClass());

	private HttpLocalProxyServer server;
	private UDPManagementServer commandServer;
	private HttpProxyEventServiceFactory esf = null;
//	private PluginManager pm ;
//	private Updater updater;

	private boolean isStarted = false;
	private boolean isStarting = false;
	
	private Trace trace;

	static
	{
		GoogleAvailableService.getInstance();
		ExtensionsLauncher.init();
	}
	
	private static Framework instance = null;
	
	private Framework(Trace trace)
	{
		this.trace = trace;
		//Preferences.init();
//		pm = PluginManager.getInstance();
		Config config = Config.getInstance();
		ThreadPoolExecutor workerExecutor = new OrderedMemoryAwareThreadPoolExecutor(
				config.getThreadPoolSize(), 0, 0);
		//ThreadPoolExecutor workerExecutor = new ScheduledThreadPoolExecutor(config.getThreadPoolSize());
		Misc.setGlobalThreadPool(workerExecutor);
		Misc.setTrace(trace);
		init();
	}
	
	public static Framework getInstance(Trace trace)
	{
		if(null == instance)
		{
			 instance = new Framework(trace);
		}
		instance.trace = trace;
		return instance;
	}
	
	private void init()
	{
		HttpProxyEventServiceFactory.Registry.register(new GoogleAppEngineHttpProxyEventServiceFactory());
	}

	public void stop()
	{
		try
		{
			if (null != commandServer)
			{
				commandServer.stop();
				commandServer = null;
			}
			if (null != server)
			{
				server.close();
				server = null;
			}
			if (null != esf)
			{
				esf.destroy();
			}
			isStarted = false;
			isStarting = false;
		}
		catch (Exception e)
		{
			logger.error("Failed to stop framework.", e);
		}

	}
	
	public boolean isStarted()
	{
		return isStarted;
	}
	
	public boolean isStarting()
	{
		return isStarting;
	}

	public boolean start()
	{
		return restart();
	}

	public boolean restart()
	{
		try
		{
			stop();
			Config config = Config.reloadConfig();
			esf = HttpProxyEventServiceFactory.Registry
			        .getHttpProxyEventServiceFactory(config
			                .getProxyEventServiceFactory());
			if (esf == null)
			{
				logger.error("No event service factory found with name:"
				        + config.getProxyEventServiceFactory());
				trace.error("No event service factory found with name:"
				        + config.getProxyEventServiceFactory());
				return false;
			}
			isStarting = true;
			esf.init();
			server = new HttpLocalProxyServer(
			        config.getLocalProxyServerAddress(),
			        Misc.getGlobalThreadPool(), esf);
			//Misc.getGlobalThreadPool().execute(commandServer);
			trace.notice("Local HTTP(s) Proxy Server Running at "
			        + config.getLocalProxyServerAddress());
			isStarted = true;
			return true;
		}
		catch (Exception e)
		{
			trace.error("Failed to launch local proxy server for reason:" + e.getMessage());
			logger.error("Failed to launch local proxy server.", e);
			isStarted = false;
			isStarting = false;
		}
		return false;
	}

}
