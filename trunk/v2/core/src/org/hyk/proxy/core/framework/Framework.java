/**
 * This file is part of the hyk-proxy-framework project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: Framework.java 
 *
 * @author yinqiwen [ 2010-8-12 | 09:28:05 PM]
 *
 */
package org.hyk.proxy.core.framework;

import java.util.concurrent.ThreadPoolExecutor;

import org.arch.event.EventDispatcher;
import org.hyk.proxy.core.config.CoreConfiguration;
import org.hyk.proxy.core.event.Events;
import org.hyk.proxy.core.httpserver.HttpLocalProxyServer;
import org.hyk.proxy.core.plugin.PluginManager;
import org.hyk.proxy.core.trace.Trace;
import org.hyk.proxy.core.util.SharedObjectHelper;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class Framework 
{
	protected Logger logger = LoggerFactory.getLogger(getClass());

	private HttpLocalProxyServer server;

	private boolean isStarted = false;
	private CoreConfiguration config;
	
	public Framework(CoreConfiguration config, PluginManager pm, Trace trace)
	{
		this.config = config;
		SharedObjectHelper.setTrace(trace);
		ThreadPoolExecutor workerExecutor = new OrderedMemoryAwareThreadPoolExecutor(
		        config.getThreadPoolSize(), 0, 0);
		SharedObjectHelper.setGlobalThreadPool(workerExecutor);
		
		Events.init(config);
		
		pm.loadPlugins();
		pm.activatePlugins();
		
	}

	public void stop()
	{
		try
		{
			if (null != server)
			{
				server.close();
				server = null;
			}

			isStarted = false;
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

	public boolean start()
	{
		return restart(config);
	}

	public boolean restart(CoreConfiguration cfg)
	{
		try
		{
			stop();
			this.config = cfg;
			//CoreConfiguration config= CoreConfiguration.getInstance();
			server = new HttpLocalProxyServer(
					cfg.getLocalProxyServerAddress(),
			        SharedObjectHelper.getGlobalThreadPool());

			SharedObjectHelper.getTrace().info("Local HTTP(S) Server Running...\nat "
			        + cfg.getLocalProxyServerAddress());
			isStarted = true;
			return true;
		}
		catch (Exception e)
		{
			logger.error("Failed to launch local proxy server.", e);
		}
		return false;
	}
}
