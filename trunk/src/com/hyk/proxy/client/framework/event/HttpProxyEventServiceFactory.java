/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: HttpProxyEventServiceFactory.java 
 *
 * @author yinqiwen [ 2010-5-13 | 08:47:48 PM ]
 *
 */
package com.hyk.proxy.client.framework.event;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.hyk.proxy.client.config.Config;
import com.hyk.proxy.client.framework.status.StatusMonitor;

/**
 *
 */
public interface HttpProxyEventServiceFactory
{
	public String getName();
	
	public void init(Config config, ExecutorService workerExecutor, StatusMonitor monitor)  throws Exception;

	public HttpProxyEventService createHttpProxyEventService();

	public void close();

	public static class Registry
	{
		private static Map<String, HttpProxyEventServiceFactory>	httpProxyEventServiceFactoryTable	= new HashMap<String, HttpProxyEventServiceFactory>();

		public static void register(HttpProxyEventServiceFactory factory)
		{
			httpProxyEventServiceFactoryTable.put(factory.getName(), factory);
		}
		
		public static HttpProxyEventServiceFactory getHttpProxyEventServiceFactory(String name)
		{
			return httpProxyEventServiceFactoryTable.get(name);
		}
		
		public static Collection<HttpProxyEventServiceFactory> getAllHttpProxyEventServiceFactorys()
		{
			return httpProxyEventServiceFactoryTable.values();
		}
	}
}
