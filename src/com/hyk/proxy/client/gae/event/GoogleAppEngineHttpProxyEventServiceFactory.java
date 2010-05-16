/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: GoogleAppEngineHttpProxyEventServiceFactory.java 
 *
 * @author yinqiwen [ 2010-5-13 | ÏÂÎç08:49:39 ]
 *
 */
package com.hyk.proxy.client.gae.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

import javax.net.ssl.SSLContext;

import com.hyk.proxy.client.framework.event.HttpProxyEventService;
import com.hyk.proxy.client.framework.event.HttpProxyEventServiceFactory;
import com.hyk.proxy.common.rpc.service.AsyncFetchService;
import com.hyk.proxy.common.rpc.service.FetchService;
import com.hyk.rpc.core.RpcException;
import com.hyk.rpc.core.util.RpcUtil;

/**
 *
 */
public class GoogleAppEngineHttpProxyEventServiceFactory implements HttpProxyEventServiceFactory
{
	private FetchServiceSelector	selector;
	private SSLContext				sslContext;
	private Executor	workerExecutor;

	public GoogleAppEngineHttpProxyEventServiceFactory(List<FetchService> fetchServices, SSLContext sslContext, Executor	workerExecutor) throws RpcException
	{
		selector = new FetchServiceSelector(fetchServices);
		this.sslContext = sslContext;
		this.workerExecutor = workerExecutor;
	}

	@Override
	public HttpProxyEventService createHttpProxyEventService()
	{
		return new GoogleAppEngineHttpProxyEventService(selector, sslContext, workerExecutor);
	}
	
	
	static class FetchServiceSelector
	{
		private List<FetchService>		fetchServices;
		private List<AsyncFetchService>	asyncFetchServices;
		private int						cursor;

		public FetchServiceSelector(List<FetchService> fetchServices) throws RpcException
		{
			Collections.shuffle(fetchServices);
			this.fetchServices = fetchServices;
			asyncFetchServices = new ArrayList<AsyncFetchService>();
			for(FetchService service : fetchServices)
			{
				asyncFetchServices.add(RpcUtil.asyncWrapper(service, AsyncFetchService.class));
			}
		}

		public synchronized FetchService select()
		{
			if(cursor >= fetchServices.size())
			{
				cursor = 0;
			}
			return fetchServices.get(cursor++);
		}

		public synchronized AsyncFetchService selectAsync()
		{
			if(cursor >= asyncFetchServices.size())
			{
				cursor = 0;
			}
			return asyncFetchServices.get(cursor++);
		}

	}
}
