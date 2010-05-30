/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: SeattleProxyEventServiceFactory.java 
 *
 * @author qiying.wang [ May 21, 2010 | 10:13:49 AM ]
 *
 */
package com.hyk.proxy.client.application.seattle.event;

import java.util.concurrent.ExecutorService;

import org.jboss.netty.channel.socket.ClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import com.hyk.proxy.client.application.seattle.SeattleApplicationConfig;
import com.hyk.proxy.client.config.Config;
import com.hyk.proxy.client.config.Config.SimpleSocketAddress;
import com.hyk.proxy.client.framework.event.HttpProxyEventService;
import com.hyk.proxy.client.framework.event.HttpProxyEventServiceFactory;
import com.hyk.proxy.client.framework.status.StatusMonitor;
import com.hyk.proxy.client.util.ListSelector;

/**
 *
 */
public class SeattleProxyEventServiceFactory implements HttpProxyEventServiceFactory
{
	private ClientSocketChannelFactory			factory;
	private ListSelector<SimpleSocketAddress>	selector;

	public SeattleProxyEventServiceFactory(Config config, ExecutorService workerExecutor, StatusMonitor monitor)
	{
		factory = new NioClientSocketChannelFactory(workerExecutor, workerExecutor);
		selector = new ListSelector<SimpleSocketAddress>(SeattleApplicationConfig.getSeattleServerAddress());
	}

	@Override
	public HttpProxyEventService createHttpProxyEventService()
	{
		// TODO Auto-generated method stub
		return new SeattleProxyEventService(factory, selector);
	}

	@Override
	public void close()
	{

	}

}
