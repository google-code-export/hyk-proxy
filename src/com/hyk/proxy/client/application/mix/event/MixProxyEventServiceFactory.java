/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: SeattleProxyEventServiceFactory.java 
 *
 * @author qiying.wang [ May 21, 2010 | 10:13:49 AM ]
 *
 */
package com.hyk.proxy.client.application.mix.event;

import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.client.application.gae.event.GoogleAppEngineHttpProxyEventServiceFactory;
import com.hyk.proxy.client.application.mix.MixApplicationConfig;
import com.hyk.proxy.client.config.Config;
import com.hyk.proxy.client.framework.event.HttpProxyEventService;
import com.hyk.proxy.client.framework.event.HttpProxyEventServiceFactory;
import com.hyk.proxy.client.framework.status.StatusMonitor;

/**
 *
 */
public class MixProxyEventServiceFactory implements HttpProxyEventServiceFactory
{
	private Logger		logger	= LoggerFactory.getLogger(getClass());
	private HttpProxyEventServiceFactory delegatefactory;
	private GoogleAppEngineHttpProxyEventServiceFactory mainEventFactory;
	public MixProxyEventServiceFactory(Config config, ExecutorService workerExecutor, StatusMonitor monitor)
	{
		try
		{
			delegatefactory = (HttpProxyEventServiceFactory)Class.forName(MixApplicationConfig.getDelegateEventServiceFactoryClass().trim()).getConstructor(Config.class,
					ExecutorService.class, StatusMonitor.class).newInstance(config, workerExecutor, monitor);
			mainEventFactory = new GoogleAppEngineHttpProxyEventServiceFactory(config, workerExecutor, monitor);
		}
		catch(Exception e)
		{
			logger.error("Failed to create delegate event service factory.", e);
		}
	}

	@Override
	public HttpProxyEventService createHttpProxyEventService()
	{
		return new MixProxyEventService(delegatefactory, mainEventFactory);
	}

	@Override
	public void close()
	{
		// TODO Auto-generated method stub

	}

}
